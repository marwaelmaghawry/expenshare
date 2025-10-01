package xpenshare.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import xpenshare.model.entity.GroupEntity;

@Repository
public interface GroupRepository extends CrudRepository<GroupEntity, Long> {
}
