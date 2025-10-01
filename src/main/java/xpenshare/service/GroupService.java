package xpenshare.service;

import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import xpenshare.repository.facade.GroupRepositoryFacade;
import xpenshare.repository.facade.UserRepositoryFacade;
import xpenshare.model.dto.group.*;
import xpenshare.model.entity.GroupEntity;
import xpenshare.model.entity.GroupMemberEntity;
import xpenshare.model.entity.UserEntity;
import xpenshare.model.mapper.GroupMapper;
import xpenshare.repository.GroupMemberRepository;
import xpenshare.event.KafkaProducer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class GroupService {

    private final GroupRepositoryFacade groupRepositoryFacade;
    private final UserRepositoryFacade userRepositoryFacade;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMapper groupMapper;
    private final KafkaProducer kafkaProducer;

    public GroupService(GroupRepositoryFacade groupRepositoryFacade,
                        UserRepositoryFacade userRepositoryFacade,
                        GroupMemberRepository groupMemberRepository,
                        GroupMapper groupMapper,
                        KafkaProducer kafkaProducer) {
        this.groupRepositoryFacade = groupRepositoryFacade;
        this.userRepositoryFacade = userRepositoryFacade;
        this.groupMemberRepository = groupMemberRepository;
        this.groupMapper = groupMapper;
        this.kafkaProducer = kafkaProducer;
    }

    @Transactional
    public GroupDto createGroup(CreateGroupRequest request) {
        Set<UserEntity> users = request.getMembers().stream()
                .map(userRepositoryFacade::findByIdOrThrow)
                .collect(Collectors.toSet());

        GroupEntity group = GroupEntity.builder()
                .name(request.getName())
                .members(new HashSet<>())
                .build();

        GroupEntity savedGroup = groupRepositoryFacade.save(group);

        for (UserEntity user : users) {
            GroupMemberEntity member = GroupMemberEntity.builder()
                    .group(savedGroup)
                    .user(user)
                    .build();
            groupMemberRepository.save(member);
            savedGroup.getMembers().add(member);
        }

        // Publish events
        kafkaProducer.publish("group.created", "{\"groupId\":" + savedGroup.getGroupId() + ",\"name\":\"" + savedGroup.getName() + "\"}");
        kafkaProducer.publish("notification.welcome", "{\"targetType\":\"GROUP\",\"groupId\":" + savedGroup.getGroupId() + "}");

        return groupMapper.toDto(savedGroup); // use instance method
    }

    @Transactional(readOnly = true)
    public GroupDto getGroup(Long groupId) {
        GroupEntity group = groupRepositoryFacade.findByIdOrThrow(groupId);
        return groupMapper.toDto(group);
    }

    @Transactional
    public GroupDto addMembers(Long groupId, AddMembersRequest request) {
        GroupEntity group = groupRepositoryFacade.findByIdOrThrow(groupId);

        List<UserEntity> users = request.getMembers().stream()
                .map(userRepositoryFacade::findByIdOrThrow)
                .toList();

        for (UserEntity user : users) {
            GroupMemberEntity member = GroupMemberEntity.builder()
                    .group(group)
                    .user(user)
                    .build();
            groupMemberRepository.save(member);
            group.getMembers().add(member);
        }

        // Publish member added events
        for (UserEntity user : users) {
            kafkaProducer.publish("notification.welcome",
                    "{\"targetType\":\"GROUP_MEMBER\",\"groupId\":" + group.getGroupId() + ",\"userId\":" + user.getUserId() + "}");
        }

        return groupMapper.toDto(group);
    }
}
