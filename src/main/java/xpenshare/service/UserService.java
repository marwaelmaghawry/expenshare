package xpenshare.service;

import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import xpenshare.exception.ConflictException;
import xpenshare.exception.NotFoundException;
import xpenshare.exception.ValidationException;
import xpenshare.model.dto.user.*;
import xpenshare.model.entity.UserEntity;
import xpenshare.model.mapper.UserMapper;
import xpenshare.repository.UserRepository;
import xpenshare.event.KafkaProducer;

@Singleton
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KafkaProducer kafkaProducer;

    public UserService(UserRepository userRepository, UserMapper userMapper, KafkaProducer kafkaProducer) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.kafkaProducer = kafkaProducer;
    }

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        UserEntity entity = userMapper.toEntity(request);
        UserEntity saved = userRepository.save(entity);

        // Publish events
        kafkaProducer.publish("user.created", "{\"userId\":" + saved.getUserId() + ",\"email\":\"" + saved.getEmail() + "\"}");
        kafkaProducer.publish("notification.welcome", "{\"targetType\":\"USER\",\"userId\":" + saved.getUserId() + "}");

        return userMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public UserDto getUser(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}


