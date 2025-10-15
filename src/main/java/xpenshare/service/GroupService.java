package xpenshare.service;

import java.util.ArrayList;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import xpenshare.event.KafkaProducer;
import xpenshare.exception.NotFoundException;
import xpenshare.model.dto.group.*;
import xpenshare.model.entity.*;
import xpenshare.model.mapper.GroupMapper;
import xpenshare.repository.facade.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class GroupService {

    private final GroupRepositoryFacade groupRepositoryFacade;
    private final UserRepositoryFacade userRepositoryFacade;
    private final GroupMapper groupMapper;
    private final KafkaProducer kafkaProducer;
    private final SettlementRepositoryFacade settlementRepositoryFacade; // ✅ added

    public GroupService(GroupRepositoryFacade groupRepositoryFacade,
                        UserRepositoryFacade userRepositoryFacade,
                        GroupMapper groupMapper,
                        KafkaProducer kafkaProducer,
                        SettlementRepositoryFacade settlementRepositoryFacade) { // ✅ added
        this.groupRepositoryFacade = groupRepositoryFacade;
        this.userRepositoryFacade = userRepositoryFacade;
        this.groupMapper = groupMapper;
        this.kafkaProducer = kafkaProducer;
        this.settlementRepositoryFacade = settlementRepositoryFacade; // ✅ added
    }

    // ------------------ CREATE GROUP ------------------
    @Transactional
    public GroupDto createGroup(CreateGroupRequest request) {
        var users = request.getMembers().stream()
                .map(userRepositoryFacade::findByIdOrThrow)
                .collect(Collectors.toSet());

        var group = GroupEntity.builder().name(request.getName()).build();

        var members = users.stream()
                .map(user -> GroupMemberEntity.builder()
                        .group(group)
                        .user(user)
                        .build())
                .collect(Collectors.toSet());

        group.setMembers(members);

        var savedGroup = groupRepositoryFacade.save(group);

        kafkaProducer.publishGroupCreated(
                "{\"groupId\":" + savedGroup.getGroupId() + ",\"name\":\"" + savedGroup.getName() + "\"}"
        );

        return groupMapper.toDto(savedGroup);
    }

    // ------------------ ADD MEMBERS ------------------
    @Transactional
    public Map<String, Object> addMembers(Long groupId, AddMembersRequest request) {
        if (request.getMembers() == null || request.getMembers().isEmpty()) {
            throw new IllegalArgumentException("Must provide at least one member to add");
        }

        var group = groupRepositoryFacade.findByIdOrThrow(groupId);
        var membersAdded = new ArrayList<Long>();

        var usersToAdd = request.getMembers().stream()
                .map(userRepositoryFacade::findByIdOrThrow)
                .toList();

        for (UserEntity user : usersToAdd) {
            boolean alreadyInGroup = group.getMembers().stream()
                    .anyMatch(m -> m.getUser().getUserId().equals(user.getUserId()));

            if (!alreadyInGroup) {
                var member = GroupMemberEntity.builder()
                        .group(group)
                        .user(user)
                        .build();
                group.getMembers().add(member);
                membersAdded.add(user.getUserId());

                kafkaProducer.publishNotificationWelcome(
                        "{\"targetType\":\"GROUP_MEMBER\",\"groupId\":" + group.getGroupId() +
                                ",\"userId\":" + user.getUserId() + "}"
                );
            }
        }

        var savedGroup = groupRepositoryFacade.save(group);
        int totalMembers = savedGroup.getMembers().size();

        Map<String, Object> response = new HashMap<>();
        response.put("groupId", savedGroup.getGroupId());
        response.put("membersAdded", membersAdded);
        response.put("totalMembers", totalMembers);

        return response;
    }

    // ------------------ GET GROUP ------------------
    @Transactional(readOnly = true)
    public GroupDto getGroup(Long groupId) {
        var group = groupRepositoryFacade.findByIdOrThrow(groupId);
        return groupMapper.toDto(group);
    }

    // ------------------ GET ALL GROUPS ------------------
    @Transactional(readOnly = true)
    public List<GroupDto> getAllGroups() {
        return groupRepositoryFacade.findAll().stream()
                .map(groupMapper::toDto)
                .collect(Collectors.toList());
    }

    // ------------------ GET GROUP BALANCES ------------------
    @Transactional(readOnly = true)
    public GroupBalanceResponse getGroupBalances(Long groupId) {
        GroupEntity group = groupRepositoryFacade.findByIdOrThrow(groupId);

        Map<Long, BigDecimal> balances = new HashMap<>();
        for (GroupMemberEntity member : group.getMembers()) {
            balances.put(member.getUser().getUserId(), BigDecimal.ZERO);
        }

        // --- EXPENSES ---
        for (ExpenseEntity expense : group.getExpenses()) {
            Long payerId = expense.getPaidBy().getUserId();

            for (ExpenseShareEntity share : expense.getShares()) {
                Long userId = share.getUser().getUserId();
                BigDecimal amount = share.getShareAmount();

                if (!userId.equals(payerId)) {
                    balances.put(userId, balances.get(userId).add(amount));     // user owes
                    balances.put(payerId, balances.get(payerId).subtract(amount)); // payer is owed
                }
            }
        }

        // --- SETTLEMENTS ---
        List<SettlementEntity> settlements = settlementRepositoryFacade.findByGroupId(groupId);
        for (SettlementEntity s : settlements) {
            if (s.getStatus() == SettlementEntity.Status.CONFIRMED) {
                Long fromUserId = s.getFromUser().getUserId();
                Long toUserId = s.getToUser().getUserId();
                BigDecimal amount = s.getAmount();

                // Payer's debt decreases
                balances.put(fromUserId, balances.get(fromUserId).subtract(amount));
                // Receiver's credit decreases
                balances.put(toUserId, balances.get(toUserId).add(amount));
            }
        }

        // --- Convert to response format ---
        var balanceList = balances.entrySet().stream()
                .map(e -> GroupBalanceResponse.UserBalance.builder()
                        .userId(e.getKey())
                        .balance(e.getValue())
                        .build())
                .toList();

        var response = GroupBalanceResponse.builder()
                .groupId(groupId)
                .balances(balanceList)
                .calculatedAt(java.time.Instant.now())
                .build();

        // ✅ Automatically trigger a balance reminder if anyone owes money
        List<GroupBalanceResponse.UserBalance> owingMembers = balanceList.stream()
                .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        if (!owingMembers.isEmpty()) {
            StringBuilder message = new StringBuilder();
            message.append("{\"groupId\":").append(groupId).append(",\"owingMembers\":[");

            for (int i = 0; i < owingMembers.size(); i++) {
                var member = owingMembers.get(i);
                message.append("{\"userId\":").append(member.getUserId())
                        .append(",\"balance\":").append(member.getBalance()).append("}");
                if (i < owingMembers.size() - 1) message.append(",");
            }

            message.append("]}");

            kafkaProducer.publishBalanceReminder(message.toString());
            System.out.println("📢 Balance reminder event sent for group " + groupId + ": " + message);
        } else {
            System.out.println("✅ No balances pending for group " + groupId);
        }

        return response;
    }


}
