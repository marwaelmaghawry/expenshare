package xpenshare.service;

import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import xpenshare.exception.NotFoundException;
import xpenshare.exception.ValidationException;
import xpenshare.exception.ConflictException;
import xpenshare.model.dto.settlement.CreateSettlementRequest;
import xpenshare.model.dto.settlement.SettlementDto;
import xpenshare.model.entity.GroupEntity;
import xpenshare.model.entity.SettlementEntity;
import xpenshare.model.entity.UserEntity;
import xpenshare.model.mapper.SettlementMapper;
import xpenshare.repository.SettlementRepository;
import xpenshare.repository.facade.GroupRepositoryFacade;
import xpenshare.repository.facade.SettlementRepositoryFacade;
import xpenshare.repository.facade.UserRepositoryFacade;
import xpenshare.model.dto.settlement.SuggestSettlementsResponse;
import xpenshare.model.dto.settlement.SuggestSettlementsRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class SettlementService {

    private final SettlementRepositoryFacade settlementRepositoryFacade;
    private final GroupRepositoryFacade groupRepositoryFacade;
    private final UserRepositoryFacade userRepositoryFacade;
    private final SettlementMapper settlementMapper;
    private final SettlementRepository settlementRepository;

    public SettlementService(SettlementRepositoryFacade settlementRepositoryFacade,
                             GroupRepositoryFacade groupRepositoryFacade,
                             UserRepositoryFacade userRepositoryFacade,
                             SettlementMapper settlementMapper,
                             SettlementRepository settlementRepository) {
        this.settlementRepositoryFacade = settlementRepositoryFacade;
        this.groupRepositoryFacade = groupRepositoryFacade;
        this.userRepositoryFacade = userRepositoryFacade;
        this.settlementMapper = settlementMapper;
        this.settlementRepository = settlementRepository;
    }

    // ✅ Create new settlement (default PENDING)
    @Transactional
    public SettlementDto createSettlement(CreateSettlementRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than 0");
        }

        GroupEntity group = groupRepositoryFacade.findByIdOrThrow(request.getGroupId());
        UserEntity fromUser = userRepositoryFacade.findByIdOrThrow(request.getFromUserId());
        UserEntity toUser = userRepositoryFacade.findByIdOrThrow(request.getToUserId());

        if (fromUser.getUserId().equals(toUser.getUserId())) {
            throw new ValidationException("fromUserId and toUserId must differ");
        }

        boolean fromUserMember = group.getMembers().stream()
                .anyMatch(m -> m.getUser().getUserId().equals(fromUser.getUserId()));
        boolean toUserMember = group.getMembers().stream()
                .anyMatch(m -> m.getUser().getUserId().equals(toUser.getUserId()));

        if (!fromUserMember || !toUserMember) {
            throw new ValidationException("Both users must be members of the group");
        }

        if (request.getEnforceOwedLimit() != null && request.getEnforceOwedLimit()) {
            BigDecimal owed = BigDecimal.valueOf(100); // placeholder for actual owed logic
            if (request.getAmount().compareTo(owed) > 0) {
                throw new ValidationException("Cannot settle more than owed");
            }
        }

        SettlementEntity.Method method;
        if (request.getMethod() == null) {
            method = SettlementEntity.Method.OTHER;
        } else {
            try {
                method = SettlementEntity.Method.valueOf(request.getMethod().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid settlement method: " + request.getMethod());
            }
        }

        SettlementEntity settlement = SettlementEntity.builder()
                .group(group)
                .fromUser(fromUser)
                .toUser(toUser)
                .amount(request.getAmount())
                .method(method)
                .note(request.getNote())
                .reference(request.getReference())
                .status(SettlementEntity.Status.PENDING) // ✅ default to pending
                .createdAt(Instant.now())
                .build();

        SettlementEntity saved = settlementRepositoryFacade.save(settlement);
        return settlementMapper.toDto(saved);
    }

    // ✅ Confirm settlement
    @Transactional
    public SettlementDto confirmSettlement(Long settlementId) {
        SettlementEntity settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new NotFoundException("Settlement not found"));

        if (SettlementEntity.Status.CONFIRMED.equals(settlement.getStatus())) {
            throw new ConflictException("Already confirmed");
        }

        settlement.setStatus(SettlementEntity.Status.CONFIRMED);
        settlement.setConfirmedAt(Instant.now());

        SettlementEntity saved = settlementRepository.save(settlement);

        return SettlementDto.builder()
                .settlementId(saved.getId())
                .groupId(saved.getGroup().getGroupId())
                .fromUserId(saved.getFromUser().getUserId())
                .toUserId(saved.getToUser().getUserId())
                .amount(saved.getAmount())
                .method(saved.getMethod() != null ? saved.getMethod().name() : null)
                .note(saved.getNote())
                .status(saved.getStatus().name())
                .createdAt(saved.getCreatedAt())
                .confirmedAt(saved.getConfirmedAt())
                .build();
    }

    // ✅ Cancel settlement
    @Transactional
    public SettlementDto cancelSettlement(Long settlementId) {
        SettlementEntity settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new NotFoundException("Settlement not found"));

        if (!SettlementEntity.Status.PENDING.equals(settlement.getStatus())) {
            throw new ConflictException("Only pending settlements can be canceled");
        }

        settlement.setStatus(SettlementEntity.Status.CANCELED);
        SettlementEntity saved = settlementRepository.save(settlement);

        return SettlementDto.builder()
                .settlementId(saved.getId())
                .groupId(saved.getGroup().getGroupId())
                .fromUserId(saved.getFromUser().getUserId())
                .toUserId(saved.getToUser().getUserId())
                .amount(saved.getAmount())
                .method(saved.getMethod().name())
                .note(saved.getNote())
                .status(saved.getStatus().name())
                .createdAt(saved.getCreatedAt())
                .confirmedAt(saved.getConfirmedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listGroupSettlements(Long groupId, Optional<String> status,
                                                    Optional<Long> fromUserId, Optional<Long> toUserId,
                                                    int page, int size) {

        // ✅ Use repository facade
        List<SettlementEntity> settlements = settlementRepositoryFacade.findByGroupId(groupId);

        // Apply optional filters
        List<SettlementEntity> filtered = settlements.stream()
                .filter(s -> status.map(val -> s.getStatus().name().equalsIgnoreCase(val)).orElse(true))
                .filter(s -> fromUserId.map(id -> s.getFromUser().getUserId().equals(id)).orElse(true))
                .filter(s -> toUserId.map(id -> s.getToUser().getUserId().equals(id)).orElse(true))
                .toList();

        int total = filtered.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);

        // ✅ Fix: build map manually to avoid type inference issues
        List<Map<String, Object>> items = filtered.subList(fromIndex, toIndex).stream()
                .map(item -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("settlementId", item.getId());
                    map.put("fromUserId", item.getFromUser().getUserId());
                    map.put("toUserId", item.getToUser().getUserId());
                    map.put("amount", item.getAmount());
                    map.put("status", item.getStatus().name());
                    return map;
                })
                .toList();

        // ✅ Build final response map safely
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("groupId", groupId);
        response.put("items", items);
        response.put("page", page);
        response.put("size", size);
        response.put("total", total);

        return response;
    }

    public SuggestSettlementsResponse suggestMinimalSettlements(
            Long groupId, SuggestSettlementsRequest request) {

        // For simplicity, we will return dummy data for now
        List<SuggestSettlementsResponse.Suggestion> suggestions = new ArrayList<>();

        // Example: suggest a single transfer from user 2 -> user 1
        suggestions.add(new SuggestSettlementsResponse.Suggestion(2L, 1L, new BigDecimal("100.00")));

        SuggestSettlementsResponse response = new SuggestSettlementsResponse();
        response.setGroupId(groupId);
        response.setSuggestions(suggestions);
        response.setTotalTransfers(suggestions.size());
        response.setStrategy(request.getStrategy());

        return response;
    }


}
