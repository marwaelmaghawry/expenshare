package xpenshare.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import xpenshare.model.dto.user.*;
import xpenshare.model.entity.UserEntity;

@Mapper(componentModel = "jakarta")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // Request → Entity
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    UserEntity toEntity(CreateUserRequest dto);

    // Entity → DTO
    UserDto toDto(UserEntity entity);

    // Nested mapping: AddressDto ↔ Address
    UserEntity.Address toEntity(AddressDto dto);
    AddressDto toDto(UserEntity.Address entity);
}
