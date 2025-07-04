package com.skishop.coupon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Distribution rule update request
 */
public record DistributionRuleUpdateRequest(
    @NotBlank(message = "Rule name is required")
    String ruleName,
    
    @NotBlank(message = "Distribution type is required")
    String distributionType,
    
    LocalDateTime distributionStartTime,
    
    LocalDateTime distributionEndTime,
    
    @Min(value = 1, message = "Distribution count must be at least 1")
    Integer distributionCount,
    
    @Min(value = 0, message = "Priority must be at least 0")
    @Max(value = 100, message = "Priority must be at most 100")
    Integer priority,
    
    List<String> targetUserTypes,
    
    List<String> targetUserIds,
    
    String description,
    
    Boolean isActive
) {}
