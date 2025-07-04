package com.skishop.user.controller;

import com.skishop.user.dto.request.UserPreferenceUpdateRequest;
import com.skishop.user.dto.response.UserPreferenceResponse;
import com.skishop.user.dto.response.UserPreferencesListResponse;
import com.skishop.user.service.UserPreferencesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * User Preferences API
 */
@RestController
@RequestMapping("/users/{userId}/preferences")
@Tag(name = "User Preferences", description = "User preferences management API")
@RequiredArgsConstructor
@Slf4j
public class UserPreferencesController {

    private final UserPreferencesService userPreferencesService;

    /**
     * Get user preferences list
     */
    @GetMapping
    @Operation(summary = "Get preferences list", description = "Get the list of user preferences")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved preferences list"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
    public ResponseEntity<UserPreferencesListResponse> getUserPreferences(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Pagination") @ParameterObject Pageable pageable) {
        
        log.info("Getting preferences for user: {}", userId);
        UserPreferencesListResponse response = userPreferencesService.getUserPreferences(userId.toString(), pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get specific preference
     */
    @GetMapping("/{key}")
    @Operation(summary = "Get specific preference", description = "Get the value of the specified preference key")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved preference"),
        @ApiResponse(responseCode = "404", description = "Preference or user not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
    public ResponseEntity<UserPreferenceResponse> getUserPreference(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Preference key") @PathVariable String key) {
        
        log.info("Getting preference for user: {}, key: {}", userId, key);
        UserPreferenceResponse response = userPreferencesService.getUserPreference(userId.toString(), key);
        return ResponseEntity.ok(response);
    }

    /**
     * Update preference
     */
    @PutMapping("/{key}")
    @Operation(summary = "Update preference", description = "Update the value of the specified preference key")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully updated preference"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
    public ResponseEntity<UserPreferenceResponse> updateUserPreference(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Preference key") @PathVariable String key,
            @Parameter(description = "Preference update request") @Valid @RequestBody UserPreferenceUpdateRequest request) {
        
        log.info("Updating preference for user: {}, key: {}", userId, key);
        UserPreferenceResponse response = userPreferencesService.updateUserPreference(userId.toString(), key, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete preference
     */
    @DeleteMapping("/{key}")
    @Operation(summary = "Delete preference", description = "Delete the specified preference key")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Successfully deleted preference"),
        @ApiResponse(responseCode = "404", description = "Preference or user not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
    public ResponseEntity<Void> deleteUserPreference(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Preference key") @PathVariable String key) {
        
        log.info("Deleting preference for user: {}, key: {}", userId, key);
        userPreferencesService.deleteUserPreference(userId, key);
        return ResponseEntity.noContent().build();
    }
}
