package xpenshare.repository.facade;


import java.util.List;

import jakarta.inject.Singleton;
import xpenshare.exception.NotFoundException;
import xpenshare.model.entity.UserEntity;
import xpenshare.repository.UserRepository;

import java.util.Optional;

@Singleton
public class UserRepositoryFacade {

    private final UserRepository userRepository;

    public UserRepositoryFacade(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity save(UserEntity entity) {
        return userRepository.save(entity);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    public UserEntity findByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

//    public Optional<UserEntity> findById(Long id) {
//        return userRepository.findById(id);
//    }
//
//    public List<UserEntity> findAll() {
//        return (List<UserEntity>) userRepository.findAll();
//    }
}
