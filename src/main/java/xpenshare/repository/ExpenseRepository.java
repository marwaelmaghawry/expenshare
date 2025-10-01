package xpenshare.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import xpenshare.model.entity.ExpenseEntity;

@Repository
public interface ExpenseRepository extends CrudRepository<ExpenseEntity, Long> {
}
