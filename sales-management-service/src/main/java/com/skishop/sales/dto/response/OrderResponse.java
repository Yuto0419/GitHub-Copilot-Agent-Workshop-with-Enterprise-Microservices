package com.skishop.sales.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Response DTO
 * Defines an immutable response structure using Java 21 Record
 */
public record OrderResponse(
    String id,
    String orderNumber,
    String customerId,
    LocalDateTime orderDate,
    String status,
    String paymentStatus,
    String paymentMethod,
    BigDecimal subtotalAmount,
    BigDecimal taxAmount,
    BigDecimal shippingFee,
    BigDecimal discountAmount,
    BigDecimal totalAmount,
    String couponCode,
    Integer usedPoints,
    BigDecimal pointDiscountAmount,
    ShippingAddressResponse shippingAddress,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<OrderItemResponse> items) {

    /**
     * Order Item Response DTO
     * Uses Java 21 Record
     */
    public record OrderItemResponse(
        String id,
        String productId,
        String productName,
        String sku,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal,
        String appliedCouponId,
        BigDecimal couponDiscountAmount,
        Integer usedPoints,
        BigDecimal pointDiscountAmount,
        BigDecimal actualAmount) {
    }

    /**
     * Shipping Address Response DTO
     * Uses Java 21 Record
     */
    public record ShippingAddressResponse(
        String postalCode,
        String prefecture,
        String city,
        String addressLine1,
        String addressLine2,
        String recipientName,
        String phoneNumber) {
    }
}
