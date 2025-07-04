package com.skishop.coupon.service;

import com.skishop.coupon.dto.request.DistributionRuleCreateRequest;
import com.skishop.coupon.dto.request.DistributionRuleUpdateRequest;
import com.skishop.coupon.dto.response.DistributionHistoryResponse;
import com.skishop.coupon.dto.response.DistributionRuleListResponse;
import com.skishop.coupon.dto.response.DistributionRuleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Distribution Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DistributionService {

    /**
     * Get distribution rules
     */
    public DistributionRuleListResponse getDistributionRules(String campaignId) {
        log.info("Getting distribution rules for campaign: {}", campaignId);
        
        // Mock data
        List<DistributionRuleResponse> rules = List.of(
            new DistributionRuleResponse(
                UUID.randomUUID().toString(),
                campaignId,
                "New User Distribution",
                "NEW_USER",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(30),
                1000,
                1,
                List.of("NEW_USER"),
                null,
                "Coupon distribution for new users",
                true,
                "ACTIVE",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
            )
        );
        
        return new DistributionRuleListResponse(
            campaignId,
            "Sample Campaign",
            rules,
            rules.size()
        );
    }

    /**
     * Create distribution rule
     */
    public DistributionRuleResponse createDistributionRule(String campaignId, DistributionRuleCreateRequest request) {
        log.info("Creating distribution rule for campaign: {}", campaignId);
        
        String ruleId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        return new DistributionRuleResponse(
            ruleId,
            campaignId,
            request.ruleName(),
            request.distributionType(),
            request.distributionStartTime(),
            request.distributionEndTime(),
            request.distributionCount(),
            request.priority(),
            request.targetUserTypes(),
            request.targetUserIds(),
            request.description(),
            request.isActive() != null ? request.isActive() : true,
            "ACTIVE",
            now,
            now
        );
    }

    /**
     * Update distribution rule
     */
    public DistributionRuleResponse updateDistributionRule(String ruleId, DistributionRuleUpdateRequest request) {
        log.info("Updating distribution rule: {}", ruleId);
        
        LocalDateTime now = LocalDateTime.now();
        
        return new DistributionRuleResponse(
            ruleId,
            "sample-campaign-id",
            request.ruleName(),
            request.distributionType(),
            request.distributionStartTime(),
            request.distributionEndTime(),
            request.distributionCount(),
            request.priority(),
            request.targetUserTypes(),
            request.targetUserIds(),
            request.description(),
            request.isActive() != null ? request.isActive() : true,
            "ACTIVE",
            now.minusHours(1),
            now
        );
    }

    /**
     * Delete distribution rule
     */
    public void deleteDistributionRule(String ruleId) {
        log.info("Deleting distribution rule: {}", ruleId);
        // Mock implementation
    }

    /**
     * Get distribution history
     */
    public DistributionHistoryResponse getDistributionHistory(String campaignId, Pageable pageable, 
                                                              String fromDate, String toDate, String status) {
        log.info("Getting distribution history for campaign: {}", campaignId);
        
        // Mock data
        List<DistributionHistoryResponse.DistributionRecord> history = List.of(
            new DistributionHistoryResponse.DistributionRecord(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "New User Distribution",
                "user-123",
                "COUPON-001",
                "NEW_USER",
                "DISTRIBUTED",
                LocalDateTime.now().minusHours(2),
                null,
                null
            )
        );
        
        // Mock page info
        DistributionHistoryResponse.PageInfo pageInfo = new DistributionHistoryResponse.PageInfo(
            0, 20, 1, 1L, false, false
        );
        
        return new DistributionHistoryResponse(
            campaignId,
            "Sample Campaign",
            history,
            pageInfo
        );
    }

    /**
     * Execute distribution
     */
    public void executeDistribution(String campaignId, String[] targetUserIds) {
        log.info("Executing distribution for campaign: {}", campaignId);
        if (targetUserIds != null) {
            log.info("Target users: {}", List.of(targetUserIds));
        }
        // Mock implementation
    }
}
