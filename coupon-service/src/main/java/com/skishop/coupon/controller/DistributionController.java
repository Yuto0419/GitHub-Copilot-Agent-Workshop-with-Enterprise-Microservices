package com.skishop.coupon.controller;

import com.skishop.coupon.dto.request.DistributionRuleCreateRequest;
import com.skishop.coupon.dto.request.DistributionRuleUpdateRequest;
import com.skishop.coupon.dto.response.DistributionHistoryResponse;
import com.skishop.coupon.dto.response.DistributionRuleResponse;
import com.skishop.coupon.dto.response.DistributionRuleListResponse;
import com.skishop.coupon.service.DistributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Coupon Distribution Management API Controller
 */
@RestController
@RequestMapping("/api/v1/distributions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Distribution API", description = "Coupon Distribution Management API")
@PreAuthorize("hasRole('ADMIN') or hasRole('CAMPAIGN_MANAGER')")
public class DistributionController {

    private final DistributionService distributionService;

    /**
     * Get distribution rules
     */
    @GetMapping("/rules/{campaignId}")
    @Operation(summary = "Get distribution rules", description = "Retrieves distribution rules for the specified campaign")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved distribution rules"),
        @ApiResponse(responseCode = "404", description = "Campaign not found"),
        @ApiResponse(responseCode = "403", description = "Administrator privileges required")
    })
    public ResponseEntity<DistributionRuleListResponse> getDistributionRules(
            @Parameter(description = "Campaign ID") @PathVariable String campaignId) {
        
        log.info("Getting distribution rules for campaign: {}", campaignId);
        DistributionRuleListResponse response = distributionService.getDistributionRules(campaignId);
        return ResponseEntity.ok(response);
    }

    /**
     * Create distribution rule
     */
    @PostMapping("/rules/{campaignId}")
    @Operation(summary = "Create distribution rule", description = "Creates a new distribution rule")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Distribution rule created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request content"),
        @ApiResponse(responseCode = "404", description = "Campaign not found"),
        @ApiResponse(responseCode = "403", description = "Administrator privileges required")
    })
    public ResponseEntity<DistributionRuleResponse> createDistributionRule(
            @Parameter(description = "Campaign ID") @PathVariable String campaignId,
            @Parameter(description = "Distribution rule creation request") @Valid @RequestBody DistributionRuleCreateRequest request) {
        
        log.info("Creating distribution rule for campaign: {}", campaignId);
        DistributionRuleResponse response = distributionService.createDistributionRule(campaignId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update distribution rule
     */
    @PutMapping("/rules/{ruleId}")
    @Operation(summary = "Update distribution rule", description = "Updates the content of a distribution rule")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Distribution rule updated successfully"),
        @ApiResponse(responseCode = "404", description = "Distribution rule not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request content"),
        @ApiResponse(responseCode = "403", description = "Administrator privileges required")
    })
    public ResponseEntity<DistributionRuleResponse> updateDistributionRule(
            @Parameter(description = "Distribution rule ID") @PathVariable String ruleId,
            @Parameter(description = "Distribution rule update request") @Valid @RequestBody DistributionRuleUpdateRequest request) {
        
        log.info("Updating distribution rule: {}", ruleId);
        DistributionRuleResponse response = distributionService.updateDistributionRule(ruleId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete distribution rule
     */
    @DeleteMapping("/rules/{ruleId}")
    @Operation(summary = "Delete distribution rule", description = "Deletes the specified distribution rule")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Distribution rule deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Distribution rule not found"),
        @ApiResponse(responseCode = "400", description = "Cannot delete rule in use"),
        @ApiResponse(responseCode = "403", description = "Administrator privileges required")
    })
    public ResponseEntity<Void> deleteDistributionRule(
            @Parameter(description = "Distribution rule ID") @PathVariable String ruleId) {
        
        log.info("Deleting distribution rule: {}", ruleId);
        distributionService.deleteDistributionRule(ruleId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get distribution history
     */
    @GetMapping("/history/{campaignId}")
    @Operation(summary = "Get distribution history", description = "Retrieves distribution history for the specified campaign")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved distribution history"),
        @ApiResponse(responseCode = "404", description = "Campaign not found"),
        @ApiResponse(responseCode = "403", description = "Administrator privileges required")
    })
    public ResponseEntity<DistributionHistoryResponse> getDistributionHistory(
            @Parameter(description = "Campaign ID") @PathVariable String campaignId,
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) String fromDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) String toDate,
            @Parameter(description = "Distribution status filter") @RequestParam(required = false) String status) {
        
        log.info("Getting distribution history for campaign: {}, from: {}, to: {}, status: {}", 
                campaignId, fromDate, toDate, status);
        
        DistributionHistoryResponse response = distributionService.getDistributionHistory(
                campaignId, pageable, fromDate, toDate, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Execute manual distribution
     */
    @PostMapping("/execute/{campaignId}")
    @Operation(summary = "Execute manual distribution", description = "Manually executes coupon distribution for the specified campaign")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Distribution process started"),
        @ApiResponse(responseCode = "404", description = "Campaign not found"),
        @ApiResponse(responseCode = "400", description = "Cannot execute distribution in current state"),
        @ApiResponse(responseCode = "403", description = "Administrator privileges required")
    })
    public ResponseEntity<Void> executeDistribution(
            @Parameter(description = "Campaign ID") @PathVariable String campaignId,
            @Parameter(description = "Target user ID list") @RequestParam(required = false) String[] targetUserIds) {
        
        log.info("Executing manual distribution for campaign: {}, targetUsers: {}", 
                campaignId, targetUserIds != null ? targetUserIds.length : "all");
        
        distributionService.executeDistribution(campaignId, targetUserIds);
        return ResponseEntity.accepted().build();
    }
}
