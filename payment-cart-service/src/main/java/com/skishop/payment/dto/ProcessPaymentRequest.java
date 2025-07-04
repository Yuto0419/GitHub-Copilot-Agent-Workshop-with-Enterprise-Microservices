package com.skishop.payment.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Payment processing request
 * 
 * @param paymentMethodId Payment method ID
 * @param billingDetails Billing details
 * @param savePaymentMethod Whether to save the payment method
 */
public record ProcessPaymentRequest(
    @NotBlank(message = "Payment method ID is required")
    String paymentMethodId,
    
    BillingDetailsRequest billingDetails,
    
    boolean savePaymentMethod
) {}
