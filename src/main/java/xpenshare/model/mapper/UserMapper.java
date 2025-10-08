package xpenshare.model.mapper;

import jakarta.inject.Singleton;
import xpenshare.model.dto.user.AddressDto;
import xpenshare.model.dto.user.CreateUserRequest;
import xpenshare.model.dto.user.UserDto;
import xpenshare.model.entity.UserEntity;
import org.mapstruct.Mapper;

@Singleton
@Mapper(componentModel = "jsr330")
public interface UserMapper {

    UserEntity toEntity(CreateUserRequest dto);

    UserDto toDto(UserEntity entity);

    UserEntity.Address toEntity(AddressDto dto);

    AddressDto toDto(UserEntity.Address entity);
}
