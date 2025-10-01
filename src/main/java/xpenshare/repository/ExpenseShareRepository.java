package xpenshare.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import xpenshare.model.entity.ExpenseShareEntity;

@Repository
public interface ExpenseShareRepository extends CrudRepository<ExpenseShareEntity, Long> {
}
