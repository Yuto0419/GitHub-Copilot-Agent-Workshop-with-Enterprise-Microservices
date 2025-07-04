package com.skishop.sales.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Tracking Information Update Request
 */
public record TrackingUpdateRequest(
    @NotBlank(message = "Tracking number is required")
    String trackingNumber,
    
    @NotBlank(message = "Carrier is required")
    String carrier,
    
    String estimatedDeliveryDate
) {}
