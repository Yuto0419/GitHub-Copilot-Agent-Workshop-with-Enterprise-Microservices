package com.skishop.sales.dto.request;

import jakarta.validation.constraints.*;

/**
 * Order Status Update Request DTO
 * Defines an immutable data structure using Java 21 Record
 */
public record OrderStatusUpdateRequest(
    @NotBlank(message = "Order status is required")
    @Pattern(regexp = "PENDING|CONFIRMED|PROCESSING|SHIPPED|DELIVERED|CANCELLED|RETURNED", 
             message = "Please specify a valid order status")
    String status,

    String notes) {
}

/**
 * Payment Status Update Request DTO
 * Uses Java 21 Record
 */
record PaymentStatusUpdateRequest(
    @NotBlank(message = "Payment status is required")
    @Pattern(regexp = "PENDING|PAID|FAILED|REFUNDED|PARTIALLY_REFUNDED", 
             message = "Please specify a valid payment status")
    String paymentStatus,

    String transactionId,

    String notes) {
}
