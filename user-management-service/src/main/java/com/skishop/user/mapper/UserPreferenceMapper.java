package com.skishop.user.mapper;

import com.skishop.user.dto.request.UserPreferenceUpdateRequest;
import com.skishop.user.dto.response.UserPreferenceResponse;
import com.skishop.user.entity.UserPreference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapping between UserPreference entity and DTO
 */
@Mapper(componentModel = "spring")
public interface UserPreferenceMapper {
    
    /**
     * Convert UserPreference entity to UserPreferenceResponse
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "key", source = "prefKey")
    @Mapping(target = "value", source = "prefValue")
    @Mapping(target = "category", expression = "java(userPreference.getPrefType() != null ? userPreference.getPrefType().name().toLowerCase() : \"general\")")
    UserPreferenceResponse toResponse(UserPreference userPreference);
    
    /**
     * Update UserPreference entity with UserPreferenceUpdateRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "prefKey", ignore = true)
    @Mapping(target = "prefValue", source = "value")
    @Mapping(target = "prefType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget UserPreference userPreference, UserPreferenceUpdateRequest request);
}
