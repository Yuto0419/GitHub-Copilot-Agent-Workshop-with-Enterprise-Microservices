package com.skishop.user.controller;

import com.skishop.user.dto.request.ChangePasswordRequest;
import com.skishop.user.dto.request.UserCreateRequest;
import com.skishop.user.dto.request.UserUpdateRequest;
import com.skishop.user.dto.response.CheckEmailResponse;
import com.skishop.user.dto.response.UserResponse;
import com.skishop.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * User Management Controller
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User-related API")
public class UserController {

    private final UserService userService;

    /**
     * Register new user
     */
    @PostMapping
    @Operation(summary = "Register new user", description = "Create a new user account")
    @ApiResponse(responseCode = "201", description = "User registration successful")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "409", description = "Email address already registered")
    public ResponseEntity<UserResponse> createUser(
            @Parameter(description = "User create request") @Valid @RequestBody UserCreateRequest request) {
        
        log.info("Creating new user with email: {}", request.email());
        UserResponse userResponse = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    /**
     * Get user information
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user information", description = "Get user information by specified ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user information")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        
        log.info("Getting user information for id: {}", id);
        UserResponse userResponse = userService.getUserById(id);
        return ResponseEntity.ok(userResponse);
    }

    /**
     * Update user information
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user information", description = "Update user information")
    @ApiResponse(responseCode = "200", description = "User information updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @Parameter(description = "User update request") @Valid @RequestBody UserUpdateRequest request) {
        
        log.info("Updating user information for id: {}", id);
        UserResponse userResponse = userService.updateUser(id, request);
        return ResponseEntity.ok(userResponse);
    }

    /**
     * Check if email exists
     */
    @GetMapping("/check-email")
    @Operation(summary = "Check email existence", description = "Check if the email address is already registered")
    @ApiResponse(responseCode = "200", description = "Check completed")
    @ApiResponse(responseCode = "400", description = "Invalid email address")
    public ResponseEntity<CheckEmailResponse> checkEmailExists(
            @Parameter(description = "Email address") @RequestParam String email) {
        
        log.info("Checking email existence: {}", email);
        CheckEmailResponse response = userService.checkEmailExists(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Change password
     */
    @PostMapping("/{id}/change-password")
    @Operation(summary = "Change password", description = "Change the user's password")
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @ApiResponse(responseCode = "400", description = "Current password does not match")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @Parameter(description = "Change password request") @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Changing password for user id: {}", id);
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    /**
     * Get current user information
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user information", description = "Get information of the authenticated user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user information")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting current user information for: {}", userDetails.getUsername());
        UserResponse userResponse = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(userResponse);
    }
}