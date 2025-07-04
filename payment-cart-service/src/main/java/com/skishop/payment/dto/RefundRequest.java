package com.skishop.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Refund request
 *
 * @param amount Refund amount
 * @param reason Refund reason
 */
public record RefundRequest(
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    BigDecimal amount,
    
    String reason
) {}
