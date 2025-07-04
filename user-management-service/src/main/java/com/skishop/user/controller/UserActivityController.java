package com.skishop.user.controller;

import com.skishop.user.dto.response.UserActivityListResponse;
import com.skishop.user.service.UserActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * User Activity Management API Controller
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Activity API", description = "User Activity Management API")
public class UserActivityController {

    private final UserActivityService userActivityService;

    /**
     * Get user activity list
     */
    @GetMapping("/{userId}/activities")
    @Operation(summary = "Get user activity list", description = "Retrieves activity history for the specified user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved activity list"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
    public ResponseEntity<UserActivityListResponse> getUserActivities(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(description = "Activity type filter") @RequestParam(required = false) String activityType,
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) String fromDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) String toDate) {
        
        log.info("Getting activities for user: {}, type: {}, from: {}, to: {}", 
                userId, activityType, fromDate, toDate);
        
        UserActivityListResponse response = userActivityService.getUserActivities(
                userId, pageable, activityType, fromDate, toDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user's activity list
     */
    @GetMapping("/me/activities")
    @Operation(summary = "Get current user's activity list", description = "Retrieves activity history for the authenticated user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved activity list"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserActivityListResponse> getCurrentUserActivities(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(description = "Activity type filter") @RequestParam(required = false) String activityType,
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) String fromDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) String toDate) {
        
        log.info("Getting activities for current user: {}, type: {}, from: {}, to: {}", 
                userDetails.getUsername(), activityType, fromDate, toDate);
        
        UserActivityListResponse response = userActivityService.getCurrentUserActivities(
                userDetails.getUsername(), pageable, activityType, fromDate, toDate);
        return ResponseEntity.ok(response);
    }
}
