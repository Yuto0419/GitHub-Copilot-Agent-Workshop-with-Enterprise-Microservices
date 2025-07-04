package com.skishop.user.mapper;

import com.skishop.user.dto.request.RoleCreateRequest;
import com.skishop.user.dto.request.RoleUpdateRequest;
import com.skishop.user.dto.response.RoleResponse;
import com.skishop.user.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapping between Role entity and DTOs
 */
@Mapper(componentModel = "spring")
public interface RoleMapper {
    
    /**
     * Convert Role entity to RoleResponse
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "permissions", expression = "java(role.getPermissions() != null ? role.getPermissions().stream().map(com.skishop.user.entity.Permission::getName).collect(java.util.stream.Collectors.toSet()) : java.util.Set.of())")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    RoleResponse toResponse(Role role);
    
    /**
     * Convert RoleCreateRequest to Role entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    Role toEntity(RoleCreateRequest request);
    
    /**
     * Update Role entity with RoleUpdateRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    void updateEntity(@MappingTarget Role role, RoleUpdateRequest request);
}
