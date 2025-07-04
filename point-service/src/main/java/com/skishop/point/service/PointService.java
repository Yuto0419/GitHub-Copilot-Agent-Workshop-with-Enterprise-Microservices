package com.skishop.point.service;

import com.skishop.point.dto.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PointService {
    
    PointTransactionDto awardPoints(PointAwardRequest request);
    
    PointBalanceResponse getPointBalance(UUID userId);
    
    List<PointTransactionDto> getPointHistory(UUID userId);
    
    List<PointTransactionDto> getPointHistoryByDateRange(
            UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    
    void redeemPoints(PointRedemptionRequest request);
    
    /**
     * Points redemption (v2 - with response)
     */
    PointRedemptionResponse redeemPointsV2(PointRedemptionRequest request);
    
    List<PointTransactionDto> getExpiringPoints(UUID userId, int days);
    
    /**
     * Get point redemption options
     */
    List<RedemptionOptionDto> getRedemptionOptions(UUID userId);
    
    void processExpiredPoints();
    
    void transferPoints(UUID fromUserId, UUID toUserId, Integer amount, String reason);
}
