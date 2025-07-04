package com.skishop.coupon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Distribution rule creation request
 */
public record DistributionRuleCreateRequest(
    @NotBlank(message = "Rule name is required")
    String ruleName,
    
    @NotBlank(message = "Distribution type is required")
    String distributionType,
    
    @NotNull(message = "Distribution start time is required")
    LocalDateTime distributionStartTime,
    
    LocalDateTime distributionEndTime,
    
    @Min(value = 1, message = "Distribution count must be 1 or greater")
    Integer distributionCount,
    
    @Min(value = 0, message = "Priority must be 0 or greater")
    @Max(value = 100, message = "Priority must be 100 or less")
    Integer priority,
    
    List<String> targetUserTypes,
    
    List<String> targetUserIds,
    
    String description,
    
    Boolean isActive
) {}
