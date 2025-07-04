package com.skishop.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * User status update request DTO
 */
public record UserStatusUpdateRequest(
    
    @NotBlank(message = "User ID is required")
    String userId,
    
    @NotNull(message = "Status is required")
    Boolean active,
    
    String reason
) {
}
