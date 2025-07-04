package com.skishop.user.dto;

import com.skishop.user.entity.User;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * User update request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Size(max = 100, message = "First name must be 100 characters or less")
    private String firstName;

    @Size(max = 100, message = "Last name must be 100 characters or less")
    private String lastName;

    @Size(max = 20, message = "Phone number must be 20 characters or less")
    private String phoneNumber;

    private LocalDate birthDate;

    private User.Gender gender;
}
