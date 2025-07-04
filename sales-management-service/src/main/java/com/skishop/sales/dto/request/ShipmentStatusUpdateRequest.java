package com.skishop.sales.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Shipment Status Update Request
 */
public record ShipmentStatusUpdateRequest(
    @NotBlank(message = "Status is required")
    String status,
    
    String location,
    
    String comments
) {}
