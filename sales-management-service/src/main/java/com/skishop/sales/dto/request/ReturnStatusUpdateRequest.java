package com.skishop.sales.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Return status update request
 */
public record ReturnStatusUpdateRequest(
    @NotBlank(message = "Status is required")
    String status,
    
    String reason,
    
    String adminComments
) {}
