package xpenshare.service;

import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import xpenshare.exception.NotFoundException;
import xpenshare.exception.ValidationException;
import xpenshare.model.dto.settlement.CreateSettlementRequest;
import xpenshare.model.dto.settlement.SettlementDto;
import xpenshare.model.entity.GroupEntity;
import xpenshare.model.entity.SettlementEntity;
import xpenshare.model.entity.UserEntity;
import xpenshare.model.mapper.SettlementMapper;
import xpenshare.repository.facade.GroupRepositoryFacade;
import xpenshare.repository.facade.SettlementRepositoryFacade;
import xpenshare.repository.facade.UserRepositoryFacade;

import java.math.BigDecimal;
import java.time.Instant;

@Singleton
public class SettlementService {

    private final SettlementRepositoryFacade settlementRepositoryFacade;
    private final GroupRepositoryFacade groupRepositoryFacade;
    private final UserRepositoryFacade userRepositoryFacade;
    private final SettlementMapper settlementMapper;

    public SettlementService(SettlementRepositoryFacade settlementRepositoryFacade,
                             GroupRepositoryFacade groupRepositoryFacade,
                             UserRepositoryFacade userRepositoryFacade,
                             SettlementMapper settlementMapper) {
        this.settlementRepositoryFacade = settlementRepositoryFacade;
        this.groupRepositoryFacade = groupRepositoryFacade;
        this.userRepositoryFacade = userRepositoryFacade;
        this.settlementMapper = settlementMapper;
    }

    @Transactional
    public SettlementDto createSettlement(CreateSettlementRequest request) {
        // Validate amount
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than 0");
        }

        // Validate group and users
        GroupEntity group = groupRepositoryFacade.findByIdOrThrow(request.getGroupId());
        UserEntity fromUser = userRepositoryFacade.findByIdOrThrow(request.getFromUserId());
        UserEntity toUser = userRepositoryFacade.findByIdOrThrow(request.getToUserId());

        if (fromUser.getUserId().equals(toUser.getUserId())) {
            throw new ValidationException("fromUserId and toUserId must differ");
        }

        // Check membership
        boolean fromUserMember = group.getMembers().stream()
                .anyMatch(m -> m.getUser().getUserId().equals(fromUser.getUserId()));
        boolean toUserMember = group.getMembers().stream()
                .anyMatch(m -> m.getUser().getUserId().equals(toUser.getUserId()));

        if (!fromUserMember || !toUserMember) {
            throw new ValidationException("Both users must be members of the group");
        }

        // Enforce owed limit
        if (request.getEnforceOwedLimit() != null && request.getEnforceOwedLimit()) {
            BigDecimal owed = BigDecimal.valueOf(100); // TODO: replace with actual balance calculation
            if (request.getAmount().compareTo(owed) > 0) {
                throw new ValidationException("Cannot settle more than owed");
            }
        }

        // Convert method string to enum
        SettlementEntity.Method method;
        if (request.getMethod() == null) {
            method = SettlementEntity.Method.OTHER; // default
        } else {
            try {
                method = SettlementEntity.Method.valueOf(request.getMethod().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid settlement method: " + request.getMethod());
            }
        }

        // Build settlement entity
        SettlementEntity settlement = SettlementEntity.builder()
                .group(group)
                .fromUser(fromUser)
                .toUser(toUser)
                .amount(request.getAmount())
                .method(method)
                .note(request.getNote())
                .reference(request.getReference())
                .status(SettlementEntity.Status.CONFIRMED) // auto confirm
                .createdAt(Instant.now())
                .build();

        SettlementEntity saved = settlementRepositoryFacade.save(settlement);
        return settlementMapper.toDto(saved);
    }
}
