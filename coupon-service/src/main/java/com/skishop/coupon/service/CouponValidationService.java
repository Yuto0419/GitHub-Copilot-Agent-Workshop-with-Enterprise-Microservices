package com.skishop.coupon.service;

import com.skishop.coupon.entity.Coupon;
import com.skishop.coupon.exception.CouponException;
import com.skishop.coupon.repository.CouponUsageRepository;
import com.skishop.coupon.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponValidationService {

    private final CouponUsageRepository couponUsageRepository;
    private final UserCouponRepository userCouponRepository;
    private static final boolean IS_USER_SPECIFIC_COUPON = false;

    public void validateCoupon(Coupon coupon, UUID userId, BigDecimal cartAmount) {
        log.debug("Validating coupon: {} for user: {}", coupon.getCode(), userId);

        // Basic validation
        validateBasicCouponConditions(coupon);
        
        // Minimum purchase amount check
        validateMinimumPurchaseAmount(coupon, cartAmount);
        
        // User usage limit check
        validateUserUsageLimit(coupon, userId);
        
        // Check if coupon is assigned to user (if needed)
        validateUserCouponAssignment(coupon, userId);

        log.debug("Coupon validation passed for: {}", coupon.getCode());
    }

    public void validateCouponForRedemption(Coupon coupon, UUID userId, BigDecimal orderAmount, UUID orderId) {
        log.debug("Validating coupon for redemption: {} for user: {}", coupon.getCode(), userId);

        // Basic validation
        validateCoupon(coupon, userId, orderAmount);
        
        // Check for duplicate use in the same order
        validateOrderDuplication(coupon, orderId);

        log.debug("Coupon redemption validation passed for: {}", coupon.getCode());
    }

    private void validateBasicCouponConditions(Coupon coupon) {
        // Check if active
        if (!Boolean.TRUE.equals(coupon.getIsActive())) {
            throw new CouponException("Coupon is not active: " + coupon.getCode());
        }

        // Check expiration date
        if (coupon.isExpired()) {
            throw new CouponException("Coupon has expired: " + coupon.getCode());
        }

        // Check usage limit
        if (coupon.isExhausted()) {
            throw new CouponException("Coupon usage limit has been reached: " + coupon.getCode());
        }

        // Check campaign validity
        if (coupon.getCampaign() == null || !coupon.getCampaign().isActive()) {
            throw new CouponException("Associated campaign is not active: " + coupon.getCode());
        }
    }

    private void validateMinimumPurchaseAmount(Coupon coupon, BigDecimal cartAmount) {
        if (coupon.getMinimumAmount() != null && 
            cartAmount.compareTo(coupon.getMinimumAmount()) < 0) {
            throw new CouponException(
                String.format("Minimum purchase amount not met. Required: %s, Current: %s", 
                    coupon.getMinimumAmount(), cartAmount));
        }
    }

    private void validateUserUsageLimit(Coupon coupon, UUID userId) {
        // Check user's usage limit (typically once per coupon)
        long userUsageCount = couponUsageRepository.countByCouponIdAndUserId(coupon.getId(), userId);
        
        if (userUsageCount > 0) {
            throw new CouponException("User has already used this coupon: " + coupon.getCode());
        }
    }

    private void validateUserCouponAssignment(Coupon coupon, UUID userId) {
        // Check if coupon is specifically assigned to user
        boolean hasAssignment = userCouponRepository.existsByUserIdAndCouponId(userId, coupon.getId());
        
        // Skip assignment check for public coupons
        // This check is performed using campaign.rules or coupon.couponType
        if (!hasAssignment && IS_USER_SPECIFIC_COUPON) {
            throw new CouponException("Coupon is not assigned to this user: " + coupon.getCode());
        }
    }

    private void validateOrderDuplication(Coupon coupon, UUID orderId) {
        if (couponUsageRepository.existsByCouponIdAndOrderId(coupon.getId(), orderId)) {
            throw new CouponException("Coupon has already been used for this order: " + coupon.getCode());
        }
    }

    public void validateCouponCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new CouponException("Coupon code cannot be empty");
        }
        
        if (code.length() < 3 || code.length() > 50) {
            throw new CouponException("Coupon code must be between 3 and 50 characters");
        }
        
        // Allow only alphanumeric characters
        if (!code.matches("^[A-Za-z0-9\\-_]+$")) {
            throw new CouponException("Coupon code can only contain letters, numbers, hyphens, and underscores");
        }
    }

    public void validateTimeConstraints(Coupon coupon) {
        LocalDateTime now = LocalDateTime.now();
        
        if (coupon.getExpiresAt() != null && now.isAfter(coupon.getExpiresAt())) {
            throw new CouponException("Coupon has expired");
        }
        
        if (coupon.getCampaign() != null) {
            LocalDateTime campaignStart = coupon.getCampaign().getStartDate();
            LocalDateTime campaignEnd = coupon.getCampaign().getEndDate();
            
            if (campaignStart != null && now.isBefore(campaignStart)) {
                throw new CouponException("Campaign has not started yet");
            }
            
            if (campaignEnd != null && now.isAfter(campaignEnd)) {
                throw new CouponException("Campaign has ended");
            }
        }
    }
}
