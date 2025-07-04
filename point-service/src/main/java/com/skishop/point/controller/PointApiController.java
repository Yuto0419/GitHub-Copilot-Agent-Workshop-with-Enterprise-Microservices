package com.skishop.point.controller;

import com.skishop.point.dto.*;
import com.skishop.point.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Point Management API v1", description = "Point management REST API v1")
public class PointApiController {
    
    private final PointService pointService;
    
    /**
     * Get point balance
     */
    @GetMapping("/balance")
    @Operation(summary = "Get point balance", description = "Get point balance and tier information for authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getPointBalance(Authentication authentication) {
        log.info("Getting point balance for user: {}", authentication.getName());
        
        UUID userId = UUID.fromString(authentication.getName());
        PointBalanceResponse balance = pointService.getPointBalance(userId);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", balance
        ));
    }
    
    /**
     * Award points (internal API)
     */
    @PostMapping("/award")
    @Operation(summary = "Award points", description = "Award points to user (for inter-service communication)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SERVICE')")
    public ResponseEntity<Map<String, Object>> awardPoints(@Valid @RequestBody PointAwardRequest request) {
        log.info("Awarding {} points to user {} for reason: {}", 
                request.getAmount(), request.getUserId(), request.getReason());
        
        PointTransactionDto transaction = pointService.awardPoints(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "success", true,
                        "data", transaction
                ));
    }
    
    /**
     * Redeem points
     */
    @PostMapping("/redeem")
    @Operation(summary = "Redeem points", description = "Use points to get discounts or exchange for products")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> redeemPoints(
            @Valid @RequestBody PointRedemptionRequest request,
            Authentication authentication) {
        
        log.info("Redeeming {} points for user: {}", request.getPointsToRedeem(), authentication.getName());
        
        UUID userId = UUID.fromString(authentication.getName());
        request.setUserId(userId);
        
        PointRedemptionResponse redemption = pointService.redeemPointsV2(request);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", redemption
        ));
    }
    
    /**
     * Get point history
     */
    @GetMapping("/history")
    @Operation(summary = "Get point history", description = "Get user's point transaction history")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getPointHistory(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "all") String type,
            Authentication authentication) {
        
        log.debug("Getting point history for user: {}", authentication.getName());
        
        UUID userId = UUID.fromString(authentication.getName());
        List<PointTransactionDto> history = pointService.getPointHistory(userId);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                        "transactions", history,
                        "pagination", Map.of(
                                "limit", limit,
                                "offset", offset,
                                "total", history.size()
                        )
                )
        ));
    }
    
    /**
     * Get points expiring soon
     */
    @GetMapping("/expiring")
    @Operation(summary = "Get expiring points", description = "Get points that will expire within specified days")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getExpiringPoints(
            @RequestParam(defaultValue = "30") int days,
            Authentication authentication) {
        
        log.debug("Getting expiring points for user: {}", authentication.getName());
        
        UUID userId = UUID.fromString(authentication.getName());
        List<PointTransactionDto> expiringPoints = pointService.getExpiringPoints(userId, days);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                        "expiringPoints", expiringPoints,
                        "daysUntilExpiry", days
                )
        ));
    }
    
    /**
     * Get point redemption options
     */
    @GetMapping("/redemption-options")
    @Operation(summary = "Get point redemption options", description = "Get available point redemption options")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getRedemptionOptions(Authentication authentication) {
        log.debug("Getting redemption options for user: {}", authentication.getName());
        
        UUID userId = UUID.fromString(authentication.getName());
        List<RedemptionOptionDto> options = pointService.getRedemptionOptions(userId);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of("options", options)
        ));
    }
    
    /**
     * Transfer points
     */
    @PostMapping("/transfer")
    @Operation(summary = "Transfer points", description = "Transfer points to another user")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> transferPoints(
            @Valid @RequestBody PointTransferRequest request,
            Authentication authentication) {
        
        log.info("Transferring {} points from user: {} to user: {}", 
                request.getAmount(), authentication.getName(), request.getToUserId());
        
        UUID fromUserId = UUID.fromString(authentication.getName());
        pointService.transferPoints(fromUserId, request.getToUserId(), 
                request.getAmount(), request.getReason());
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Points transferred successfully"
        ));
    }
}
