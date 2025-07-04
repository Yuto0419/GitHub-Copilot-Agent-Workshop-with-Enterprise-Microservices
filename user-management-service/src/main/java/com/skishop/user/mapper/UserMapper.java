package com.skishop.user.mapper;

import com.skishop.user.dto.UserRegistrationRequest;
import com.skishop.user.dto.response.UserResponse;
import com.skishop.user.dto.request.UserCreateRequest;
import com.skishop.user.dto.request.UserUpdateRequest;
import com.skishop.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for converting between user entities and DTOs
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    /**
     * Convert entity to response DTO
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "gender", target = "gender")
    @Mapping(target = "roles", expression = "java(user.getRole() != null ? java.util.Set.of(user.getRole().getName()) : java.util.Set.of())")
    @Mapping(target = "username", source = "email")
    UserResponse toResponse(User user);

    /**
     * Convert registration request DTO to entity
     * Password is excluded as it needs separate encryption
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "phoneVerified", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "preferences", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserRegistrationRequest request);

    /**
     * Convert user creation request DTO to entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "phoneVerified", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "preferences", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "birthDate", ignore = true)
    @Mapping(target = "gender", ignore = true)
    User toEntity(UserCreateRequest request);

    /**
     * Update existing entity with user update request DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "phoneVerified", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "preferences", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget User user, UserUpdateRequest request);
}
