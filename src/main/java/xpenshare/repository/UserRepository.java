package xpenshare.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import xpenshare.model.entity.UserEntity;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByMobileNumber(String mobileNumber);

}
