package xpenshare.service;

import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import xpenshare.exception.ValidationException;
import xpenshare.model.dto.expense.*;
import xpenshare.model.entity.*;
import xpenshare.model.mapper.ExpenseMapper;
import xpenshare.repository.facade.*;
import xpenshare.event.KafkaProducer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class ExpenseService {

    private final ExpenseRepositoryFacade expenseRepositoryFacade;
    private final ExpenseShareRepositoryFacade expenseShareRepositoryFacade;
    private final GroupRepositoryFacade groupRepositoryFacade;
    private final UserRepositoryFacade userRepositoryFacade;
    private final ExpenseMapper expenseMapper;
    private final KafkaProducer kafkaProducer;

    public ExpenseService(ExpenseRepositoryFacade expenseRepositoryFacade,
                          ExpenseShareRepositoryFacade expenseShareRepositoryFacade,
                          GroupRepositoryFacade groupRepositoryFacade,
                          UserRepositoryFacade userRepositoryFacade,
                          ExpenseMapper expenseMapper,
                          KafkaProducer kafkaProducer) {
        this.expenseRepositoryFacade = expenseRepositoryFacade;
        this.expenseShareRepositoryFacade = expenseShareRepositoryFacade;
        this.groupRepositoryFacade = groupRepositoryFacade;
        this.userRepositoryFacade = userRepositoryFacade;
        this.expenseMapper = expenseMapper;
        this.kafkaProducer = kafkaProducer;
    }

    @Transactional
    public ExpenseDto addExpense(CreateExpenseRequest request) {
        // Validate basic fields
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than 0");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new ValidationException("Description cannot be empty");
        }

        GroupEntity group = groupRepositoryFacade.findByIdOrThrow(request.getGroupId());
        UserEntity payer = userRepositoryFacade.findByIdOrThrow(request.getPaidBy());

        // Check that payer is a group member
        boolean isMember = group.getMembers().stream()
                .anyMatch(m -> m.getUser().getUserId().equals(payer.getUserId()));
        if (!isMember) {
            throw new ValidationException("Payer must be a member of the group");
        }

        ExpenseEntity expense = ExpenseEntity.builder()
                .group(group)
                .paidBy(payer)
                .amount(request.getAmount())
                .description(request.getDescription())
                .createdAt(Instant.now())
                .shares(new HashSet<>())
                .build();

        ExpenseEntity savedExpense = expenseRepositoryFacade.save(expense);

        List<ExpenseShareEntity> shares = calculateShares(savedExpense, request);

        for (ExpenseShareEntity share : shares) {
            expenseShareRepositoryFacade.save(share);
            savedExpense.getShares().add(share);
        }

// New, works
        kafkaProducer.publishExpenseAdded(
                "{\"expenseId\":" + savedExpense.getId() +
                        ",\"groupId\":" + group.getGroupId() + "}");

        return expenseMapper.toDto(savedExpense);
    }

    private List<ExpenseShareEntity> calculateShares(ExpenseEntity expense, CreateExpenseRequest request) {

        switch (request.getSplitType()) {
            case EQUAL -> {
                final List<UserEntity> participants = request.getParticipants() != null && !request.getParticipants().isEmpty()
                        ? request.getParticipants().stream()
                        .map(userRepositoryFacade::findByIdOrThrow)
                        .toList()
                        : expense.getGroup().getMembers().stream()
                        .map(GroupMemberEntity::getUser)
                        .toList();

                if (participants.isEmpty()) {
                    throw new ValidationException("No participants found for equal split");
                }

                BigDecimal share = expense.getAmount()
                        .divide(BigDecimal.valueOf(participants.size()), 2, BigDecimal.ROUND_HALF_UP);

                return participants.stream()
                        .map(user -> ExpenseShareEntity.builder()
                                .expense(expense)
                                .user(user)
                                .shareAmount(user.getUserId().equals(expense.getPaidBy().getUserId())
                                        ? share.negate().multiply(BigDecimal.valueOf(participants.size() - 1))
                                        : share)
                                .build())
                        .toList();
            }

            case EXACT -> {
                if (request.getShares() == null || request.getShares().isEmpty()) {
                    throw new ValidationException("Shares must be provided for EXACT split type");
                }

                BigDecimal total = request.getShares().stream()
                        .map(CreateExpenseRequest.ShareRequest::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (total.compareTo(expense.getAmount()) != 0) {
                    throw new ValidationException("Exact shares must sum up to expense amount");
                }

                return request.getShares().stream()
                        .map(s -> ExpenseShareEntity.builder()
                                .expense(expense)
                                .user(userRepositoryFacade.findByIdOrThrow(s.getUserId()))
                                .shareAmount(s.getUserId().equals(expense.getPaidBy().getUserId())
                                        ? s.getAmount().negate()
                                        : s.getAmount())
                                .build())
                        .toList();
            }

            case PERCENT -> {
                if (request.getShares() == null || request.getShares().isEmpty()) {
                    throw new ValidationException("Shares must be provided for PERCENT split type");
                }

                int totalPercent = request.getShares().stream()
                        .mapToInt(CreateExpenseRequest.ShareRequest::getPercent)
                        .sum();

                if (totalPercent != 100) {
                    throw new ValidationException("Split percentages must total 100");
                }

                return request.getShares().stream()
                        .map(s -> {
                            BigDecimal amount = expense.getAmount()
                                    .multiply(BigDecimal.valueOf(s.getPercent()))
                                    .divide(BigDecimal.valueOf(100));
                            return ExpenseShareEntity.builder()
                                    .expense(expense)
                                    .user(userRepositoryFacade.findByIdOrThrow(s.getUserId()))
                                    .shareAmount(s.getUserId().equals(expense.getPaidBy().getUserId())
                                            ? amount.negate()
                                            : amount)
                                    .build();
                        })
                        .toList();
            }

            default -> throw new ValidationException("Unsupported split type");
        }
    }
}
