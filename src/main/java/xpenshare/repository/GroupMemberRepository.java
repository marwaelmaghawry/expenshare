package xpenshare.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import xpenshare.model.entity.GroupMemberEntity;

@Repository
public interface GroupMemberRepository extends CrudRepository<GroupMemberEntity, Long> {
}

