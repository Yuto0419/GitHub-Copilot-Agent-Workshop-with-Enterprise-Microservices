package com.skishop.sales.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Order creation request DTO
 * Using Java 21 Record to define immutable data structure
 */
public record OrderCreateRequest(
    @NotBlank(message = "Customer ID is required")
    String customerId,

    @Valid
    @NotEmpty(message = "At least one order item is required")
    List<OrderItemRequest> items,

    @Valid
    @NotNull(message = "Shipping address is required")
    ShippingAddressRequest shippingAddress,

    @NotBlank(message = "Payment method is required")
    String paymentMethod,

    String couponCode,

    @Min(value = 0, message = "Used points must be greater than or equal to 0")
    Integer usedPoints,

    String notes) {

    /**
     * Order item request
     * Using Java 21 Record
     */
    public record OrderItemRequest(
        @NotBlank(message = "Product ID is required")
        String productId,

        @NotBlank(message = "Product name is required")
        String productName,

        @NotBlank(message = "SKU is required")
        String sku,

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.00", inclusive = false, message = "Unit price must be greater than 0")
        BigDecimal unitPrice,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity) {
    }

    /**
     * Shipping address request
     * Using Java 21 Record
     */
    public record ShippingAddressRequest(
        @NotBlank(message = "Postal code is required")
        @Pattern(regexp = "\\d{3}-\\d{4}", message = "Postal code must be in format 000-0000")
        String postalCode,

        @NotBlank(message = "Prefecture is required")
        String prefecture,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "Address line 1 is required")
        String addressLine1,

        String addressLine2,

        @NotBlank(message = "Recipient name is required")
        String recipientName,

        @Pattern(regexp = "\\d{2,4}-\\d{2,4}-\\d{4}", message = "Phone number format is invalid")
        String phoneNumber) {
    }
}
