package xpenshare.service;
import java.util.ArrayList;

import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import xpenshare.event.KafkaProducer;
import xpenshare.exception.NotFoundException;
import xpenshare.model.dto.group.*;
import xpenshare.model.entity.GroupEntity;
import xpenshare.model.entity.GroupMemberEntity;
import xpenshare.model.entity.UserEntity;
import xpenshare.model.mapper.GroupMapper;
import xpenshare.repository.facade.GroupRepositoryFacade;
import xpenshare.repository.facade.UserRepositoryFacade;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import xpenshare.model.entity.ExpenseEntity;
import xpenshare.model.entity.ExpenseShareEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class GroupService {

    private final GroupRepositoryFacade groupRepositoryFacade;
    private final UserRepositoryFacade userRepositoryFacade;
    private final GroupMapper groupMapper;
    private final KafkaProducer kafkaProducer;

    public GroupService(GroupRepositoryFacade groupRepositoryFacade,
                        UserRepositoryFacade userRepositoryFacade,
                        GroupMapper groupMapper,
                        KafkaProducer kafkaProducer) {
        this.groupRepositoryFacade = groupRepositoryFacade;
        this.userRepositoryFacade = userRepositoryFacade;
        this.groupMapper = groupMapper;
        this.kafkaProducer = kafkaProducer;
    }

    // ------------------ CREATE GROUP ------------------
    @Transactional
    public GroupDto createGroup(CreateGroupRequest request) {
        Set<UserEntity> users = request.getMembers().stream()
                .map(userRepositoryFacade::findByIdOrThrow)
                .collect(Collectors.toSet());

        GroupEntity group = GroupEntity.builder()
                .name(request.getName())
                .build();

        Set<GroupMemberEntity> members = users.stream()
                .map(user -> GroupMemberEntity.builder()
                        .group(group)
                        .user(user)
                        .build())
                .collect(Collectors.toSet());

        group.setMembers(members);

        GroupEntity savedGroup = groupRepositoryFacade.save(group);

        // Kafka events
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

        GroupEntity group = groupRepositoryFacade.findByIdOrThrow(groupId);

        List<Long> membersAdded = new ArrayList<>();

        // Fetch users to add
        List<UserEntity> usersToAdd = request.getMembers().stream()
                .map(userRepositoryFacade::findByIdOrThrow)
                .toList();

        for (UserEntity user : usersToAdd) {
            boolean alreadyInGroup = group.getMembers().stream()
                    .anyMatch(m -> m.getUser().getUserId().equals(user.getUserId()));

            if (!alreadyInGroup) {
                GroupMemberEntity member = GroupMemberEntity.builder()
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

        // Save group and count total members
        GroupEntity savedGroup = groupRepositoryFacade.save(group);
        int totalMembers = savedGroup.getMembers().size();

        // Custom response
        Map<String, Object> response = new HashMap<>();
        response.put("groupId", savedGroup.getGroupId());
        response.put("membersAdded", membersAdded);
        response.put("totalMembers", totalMembers);

        return response;
    }

    // ------------------ GET GROUP ------------------
    @Transactional(readOnly = true)
    public GroupDto getGroup(Long groupId) {
        GroupEntity group = groupRepositoryFacade.findByIdOrThrow(groupId);
        return groupMapper.toDto(group);
    }

    // ------------------ GET ALL GROUPS ------------------
    @Transactional(readOnly = true)
    public List<GroupDto> getAllGroups() {
        return groupRepositoryFacade.findAll().stream()
                .map(groupMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupBalanceResponse getGroupBalances(Long groupId) {
        GroupEntity group = groupRepositoryFacade.findByIdOrThrow(groupId);

        Map<Long, BigDecimal> balances = new HashMap<>();
        for (GroupMemberEntity member : group.getMembers()) {
            balances.put(member.getUser().getUserId(), BigDecimal.ZERO);
        }

        for (ExpenseEntity expense : group.getExpenses()) {
            Long payerId = expense.getPaidBy().getUserId();

            // Add each user's share
            for (ExpenseShareEntity share : expense.getShares()) {
                Long userId = share.getUser().getUserId();
                BigDecimal amount = share.getShareAmount();

                if (userId.equals(payerId)) {
                    // Skip payer's own share here
                    continue;
                }

                // Other members owe this amount → positive
                balances.put(userId, balances.get(userId).add(amount));

                // Payer receives this amount → negative
                balances.put(payerId, balances.get(payerId).subtract(amount));
            }
        }

        List<GroupBalanceResponse.UserBalance> balanceList = balances.entrySet().stream()
                .map(e -> GroupBalanceResponse.UserBalance.builder()
                        .userId(e.getKey())
                        .balance(e.getValue())
                        .build())
                .toList();

        return GroupBalanceResponse.builder()
                .groupId(groupId)
                .balances(balanceList)
                .calculatedAt(java.time.Instant.now())
                .build();
    }





}
