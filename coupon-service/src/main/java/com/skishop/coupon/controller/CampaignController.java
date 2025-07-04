package com.skishop.coupon.controller;

import com.skishop.coupon.dto.CampaignDto;
import com.skishop.coupon.service.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/campaigns")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Campaign Management", description = "Campaign Management API")
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create Campaign", description = "Creates a new campaign")
    public ResponseEntity<Map<String, Object>> createCampaign(
            @Valid @RequestBody CampaignDto.CampaignRequest request) {
        
        log.info("Creating campaign: {}", request.getName());
        CampaignDto.CampaignResponse response = campaignService.createCampaign(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "success", true,
            "data", response
        ));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Campaign List", description = "Retrieves the list of campaigns")
    public ResponseEntity<Map<String, Object>> getCampaigns(
            @Valid @ModelAttribute CampaignDto.CampaignListRequest request) {
        
        Page<CampaignDto.CampaignResponse> campaigns = campaignService.getCampaigns(request);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", campaigns.getContent(),
            "pagination", Map.of(
                "page", campaigns.getNumber(),
                "size", campaigns.getSize(),
                "totalElements", campaigns.getTotalElements(),
                "totalPages", campaigns.getTotalPages()
            )
        ));
    }

    @PutMapping("/{campaignId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Campaign", description = "Updates campaign information")
    public ResponseEntity<Map<String, Object>> updateCampaign(
            @Parameter(description = "Campaign ID") @PathVariable UUID campaignId,
            @Valid @RequestBody CampaignDto.CampaignUpdateRequest request) {
        
        log.info("Updating campaign: {}", campaignId);
        CampaignDto.CampaignResponse response = campaignService.updateCampaign(campaignId, request);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", response
        ));
    }

    @PostMapping("/{campaignId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate Campaign", description = "Activates the campaign")
    public ResponseEntity<Map<String, Object>> activateCampaign(
            @Parameter(description = "Campaign ID") @PathVariable UUID campaignId) {
        
        log.info("Activating campaign: {}", campaignId);
        CampaignDto.CampaignResponse response = campaignService.activateCampaign(campaignId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", response
        ));
    }

    @GetMapping("/{campaignId}/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Campaign Analytics", description = "Retrieves analytics data for the campaign")
    public ResponseEntity<Map<String, Object>> getCampaignAnalytics(
            @Parameter(description = "Campaign ID") @PathVariable UUID campaignId) {
        
        CampaignDto.CampaignAnalyticsResponse analytics = campaignService.getCampaignAnalytics(campaignId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", analytics
        ));
    }

    @GetMapping("/active")
    @Operation(summary = "Get Active Campaigns", description = "Retrieves the list of currently active campaigns")
    public ResponseEntity<Map<String, Object>> getActiveCampaigns() {
        
        List<CampaignDto.CampaignResponse> campaigns = campaignService.getActiveCampaigns();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", campaigns
        ));
    }

    @GetMapping("/{campaignId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Campaign Details", description = "Retrieves detailed information of the specified campaign")
    public ResponseEntity<Map<String, Object>> getCampaignById(
            @Parameter(description = "Campaign ID") @PathVariable UUID campaignId) {
        
        CampaignDto.CampaignResponse campaign = campaignService.getCampaignById(campaignId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", campaign
        ));
    }
}
