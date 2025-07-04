package com.skishop.user.dto;

import com.skishop.user.entity.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * User Registration Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Size(max = 255, message = "Email must be 255 characters or less")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]",
        message = "Password must include uppercase, lowercase, number, and special character"
    )
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be 100 characters or less")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be 100 characters or less")
    private String lastName;

    @Pattern(
        regexp = "^[0-9\\-()+ ]*$",
        message = "Please enter a valid phone number"
    )
    @Size(max = 20, message = "Phone number must be 20 characters or less")
    private String phoneNumber;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    private User.Gender gender;

    /**
     * For password confirmation
     */
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    /**
     * Check if password and confirmation match
     */
    public boolean isPasswordMatching() {
        return password != null && confirmPassword != null && password.equals(confirmPassword);
    }
}
