package com.skishop.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Create payment intent request
 * 
 * @param cartId Cart ID
 * @param paymentMethod Payment method
 * @param currency Currency code
 * @param billingDetails Billing details
 */
public record CreatePaymentIntentRequest(
    @NotNull(message = "Cart ID is required")
    UUID cartId,
    
    @NotBlank(message = "Payment method is required")
    String paymentMethod,
    
    String currency,
    
    BillingDetailsRequest billingDetails
) {
    public CreatePaymentIntentRequest {
        // Set default value
        if (currency == null || currency.isBlank()) {
            currency = "JPY";
        }
    }
}
