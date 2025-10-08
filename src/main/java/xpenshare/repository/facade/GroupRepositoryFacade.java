package xpenshare.repository.facade;

import java.util.List;


import jakarta.inject.Singleton;
import xpenshare.exception.NotFoundException;
import xpenshare.model.entity.GroupEntity;
import xpenshare.repository.GroupRepository;

@Singleton
public class GroupRepositoryFacade {

    private final GroupRepository groupRepository;

    public GroupRepositoryFacade(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public GroupEntity save(GroupEntity entity) {
        return groupRepository.save(entity);
    }

    public GroupEntity findByIdOrThrow(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Group not found"));
    }

    public List<GroupEntity> findAll() {
        return groupRepository.findAll();
    }


}
