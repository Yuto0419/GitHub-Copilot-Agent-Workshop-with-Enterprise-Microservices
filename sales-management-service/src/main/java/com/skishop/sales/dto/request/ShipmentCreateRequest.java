package com.skishop.sales.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Shipment creation request DTO
 */
@Data
public class ShipmentCreateRequest {

    @NotNull(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Carrier is required")
    private String carrier;

    private String trackingNumber;

    @Valid
    @NotNull(message = "Shipping address is required")
    private ShippingAddressRequest shippingAddress;

    private LocalDateTime estimatedDeliveryAt;

    private String notes;

    /**
     * Shipping address request
     */
    @Data
    public static class ShippingAddressRequest {

        @NotBlank(message = "Postal code is required")
        @Pattern(regexp = "\\d{3}-\\d{4}", message = "Postal code must be in the format 000-0000")
        private String postalCode;

        @NotBlank(message = "Prefecture is required")
        private String prefecture;

        @NotBlank(message = "City is required")
        private String city;

        @NotBlank(message = "Address line 1 is required")
        private String addressLine1;

        private String addressLine2;

        @NotBlank(message = "Recipient name is required")
        private String recipientName;

        @Pattern(regexp = "\\d{2,4}-\\d{2,4}-\\d{4}", message = "Phone number format is invalid")
        private String phoneNumber;
    }
}


