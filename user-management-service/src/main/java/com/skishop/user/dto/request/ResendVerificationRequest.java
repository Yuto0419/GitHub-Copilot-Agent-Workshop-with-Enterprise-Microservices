package com.skishop.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Email verification resend request DTO
 */
public record ResendVerificationRequest(
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email
) {
}
