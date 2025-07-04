package com.skishop.point.controller;

import com.skishop.point.dto.UserTierDto;
import com.skishop.point.entity.TierDefinition;
import com.skishop.point.service.TierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tiers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tier Management API v1", description = "Tier Management REST API v1")
public class TierApiController {
    
    private final TierService tierService;
    
    /**
     * Get user tier information
     */
    @GetMapping("/user")
    @Operation(summary = "Get user tier information", description = "Retrieves current tier information for the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getUserTier(Authentication authentication) {
        log.debug("Getting tier information for user: {}", authentication.getName());
        
        UUID userId = UUID.fromString(authentication.getName());
        UserTierDto userTier = tierService.getUserTier(userId);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", userTier
        ));
    }
    
    /**
     * Get tier benefits
     */
    @GetMapping("/benefits")
    @Operation(summary = "Get tier benefits", description = "Retrieves benefit information for the specified tier")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getTierBenefits(
            @RequestParam(required = false) String tierLevel,
            Authentication authentication) {
        
        log.debug("Getting tier benefits for tier: {}", tierLevel);
        
        if (tierLevel == null) {
            // Get the user's current tier benefits
            UUID userId = UUID.fromString(authentication.getName());
            UserTierDto userTier = tierService.getUserTier(userId);
            tierLevel = userTier.getTierLevel();
        }
        
        TierDefinition tierDefinition = tierService.getTierDefinition(tierLevel);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                        "tier", tierDefinition,
                        "benefits", tierDefinition.getBenefits()
                )
        ));
    }
    
    /**
     * Get tier progress
     */
    @GetMapping("/progress")
    @Operation(summary = "Get tier progress", description = "Retrieves progress information towards the next tier")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getTierProgress(Authentication authentication) {
        log.debug("Getting tier progress for user: {}", authentication.getName());
        
        UUID userId = UUID.fromString(authentication.getName());
        UserTierDto eligibility = tierService.checkUpgradeEligibility(userId);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", eligibility
        ));
    }
    
    /**
     * Get all tier definitions
     */
    @GetMapping
    @Operation(summary = "Get all tier definitions", description = "Retrieves definitions for all available tiers")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getAllTiers() {
        log.debug("Getting all tier definitions");
        
        List<TierDefinition> tiers = tierService.getAllTiers();
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of("tiers", tiers)
        ));
    }
    
    /**
     * Get tier details
     */
    @GetMapping("/{tierLevel}")
    @Operation(summary = "Get tier details", description = "Retrieves detailed information for the specified tier")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getTierDetails(@PathVariable String tierLevel) {
        log.debug("Getting tier details for tier: {}", tierLevel);
        
        TierDefinition tierDefinition = tierService.getTierDefinition(tierLevel);
        
        if (tierDefinition == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", tierDefinition
        ));
    }
}
