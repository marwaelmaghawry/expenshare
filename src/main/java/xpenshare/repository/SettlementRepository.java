package xpenshare.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import xpenshare.model.entity.SettlementEntity;

import java.util.List;

@Repository
public interface SettlementRepository extends CrudRepository<SettlementEntity, Long> {
    List<SettlementEntity> findByGroupGroupId(Long groupId);
}
