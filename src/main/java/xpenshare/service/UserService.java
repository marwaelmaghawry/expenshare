package xpenshare.service;

import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import xpenshare.exception.ConflictException;
import xpenshare.exception.NotFoundException;
import xpenshare.exception.ValidationException;
import xpenshare.model.dto.user.CreateUserRequest;
import xpenshare.model.dto.user.UserDto;
import xpenshare.model.entity.UserEntity;
import xpenshare.model.mapper.UserMapper;
import xpenshare.repository.UserRepository;
import xpenshare.event.KafkaProducer;
import xpenshare.repository.facade.UserRepositoryFacade;
import java.util.List;

@Singleton
public class UserService {

    private final UserRepository userRepository;
    private final UserRepositoryFacade userRepositoryFacade;

    private final UserMapper userMapper;
    private final KafkaProducer kafkaProducer;

    public UserService(UserRepository userRepository, UserMapper
            userMapper, KafkaProducer kafkaProducer,UserRepositoryFacade userRepositoryFacade) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.kafkaProducer = kafkaProducer;
        this.userRepositoryFacade = userRepositoryFacade;

    }
//    public List<UserEntity> getAllUsers() {
//        return userRepositoryFacade.findAll();
//    }

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        // ✅ Independent validations
        boolean invalidEmail = request.getEmail() == null ||
                !request.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.com$");

        boolean invalidMobile = request.getMobileNumber() == null ||
                !request.getMobileNumber().matches("^\\+20[0-9]{10}$");


        if (invalidEmail && invalidMobile) {
            throw new ValidationException("Invalid email and mobile number");
        } else if (invalidEmail) {
            throw new ValidationException("Invalid email");
        } else if (invalidMobile) {
            throw new ValidationException("Invalid mobile number");
        }

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new ConflictException("Mobile number already exists");
        }

        UserEntity entity = userMapper.toEntity(request);
        UserEntity saved = userRepository.save(entity);

        kafkaProducer.publishUserCreated(
                "{\"userId\":" + saved.getUserId() + ",\"email\":\"" + saved.getEmail() + "\"}"
        );
        kafkaProducer.publishNotificationWelcome(
                "{\"targetType\":\"USER\",\"userId\":" + saved.getUserId() + "}"
        );

        return userMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public UserDto getUser(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found with id " + id);
        }
        userRepository.deleteById(id);
    }

}
