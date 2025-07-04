package com.skishop.user.controller;

import com.skishop.user.dto.request.UserRoleUpdateRequest;
import com.skishop.user.dto.response.UserListResponse;
import com.skishop.user.dto.response.UserResponse;
import com.skishop.user.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin Controller
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@Tag(name = "Admin Functions", description = "Admin-only API")
public class AdminController {

    private final AdminService adminService;

    /**
     * Get user list
     */
    @GetMapping("/users")
    @Operation(summary = "Get user list", description = "Get the user list for administrators")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user list")
    @ApiResponse(responseCode = "403", description = "Admin privileges required")
    public ResponseEntity<UserListResponse> getUsers(
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(description = "Search keyword") @RequestParam(required = false) String search,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Role filter") @RequestParam(required = false) String role) {
        
        log.info("Getting user list with search: {}, status: {}, role: {}", search, status, role);
        UserListResponse response = adminService.getUsersWithAdvancedFilters(pageable, search, status, role);
        return ResponseEntity.ok(response);
    }

    /**
     * Update user roles
     */
    @PostMapping("/users/{userId}/roles")
    @Operation(summary = "Update user roles", description = "Update the roles of the specified user")
    @ApiResponse(responseCode = "200", description = "Roles updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "403", description = "Admin privileges required")
    public ResponseEntity<UserResponse> updateUserRoles(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Role update request") @Valid @RequestBody UserRoleUpdateRequest request) {
        
        log.info("Updating roles for user: {}, roles: {}", userId, request.roleIds());
        UserResponse response = adminService.updateUserRoles(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Activate user account
     */
    @PostMapping("/users/{userId}/activate")
    @Operation(summary = "Activate user account", description = "Activate the specified user's account")
    @ApiResponse(responseCode = "200", description = "Account activated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "403", description = "Admin privileges required")
    public ResponseEntity<UserResponse> activateUser(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        
        log.info("Activating user account: {}", userId);
        UserResponse response = adminService.activateUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate user account
     */
    @PostMapping("/users/{userId}/deactivate")
    @Operation(summary = "Deactivate user account", description = "Deactivate the specified user's account")
    @ApiResponse(responseCode = "200", description = "Account deactivated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "403", description = "Admin privileges required")
    public ResponseEntity<UserResponse> deactivateUser(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        
        log.info("Deactivating user account: {}", userId);
        UserResponse response = adminService.deactivateUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Force delete user
     */
    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Force delete user", description = "Forcefully delete the specified user")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "403", description = "Admin privileges required")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        
        log.info("Force deleting user: {}", userId);
        adminService.hardDeleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get system statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get system statistics", description = "Get system statistics")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics")
    @ApiResponse(responseCode = "403", description = "Admin privileges required")
    public ResponseEntity<Object> getSystemStats() {
        
        log.info("Getting system statistics");
        Object stats = adminService.getSystemStats();
        return ResponseEntity.ok(stats);
    }
}
