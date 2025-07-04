package com.skishop.sales.entity.jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Order Item Entity (PostgreSQL)
 * Manages detailed information of products included in orders
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_items_order_id", columnList = "orderId"),
    @Index(name = "idx_order_items_product_id", columnList = "productId")
})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Order ID (foreign key)
     */
    @Column(nullable = false)
    private UUID orderId;

    /**
     * Product ID
     */
    @Column(nullable = false, length = 100)
    private String productId;

    /**
     * Product name (snapshot at order time)
     */
    @Column(nullable = false, length = 200)
    private String productName;

    /**
     * Product SKU
     */
    @Column(nullable = false, length = 100)
    private String sku;

    /**
     * Unit price (price at order time)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Quantity
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Subtotal (unit price × quantity)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Applied coupon ID
     */
    private String appliedCouponId;

    /**
     * Coupon discount amount
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal couponDiscountAmount;

    /**
     * Used points
     */
    private Integer usedPoints;

    /**
     * Point discount amount
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal pointDiscountAmount;

    /**
     * Order relationship (Many-to-One)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", insertable = false, updatable = false)
    private Order order;

    /**
     * Method to calculate subtotal
     * Leveraging Java 21's improved null handling
     */
    public void calculateSubtotal() {
        if (unitPrice != null && quantity != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    /**
     * Calculate actual payment amount (after coupon and point discounts)
     * Using Java 21's Optional and Stream for more functional programming style
     */
    public BigDecimal getActualAmount() {
        var discounts = List.of(
            Optional.ofNullable(couponDiscountAmount),
            Optional.ofNullable(pointDiscountAmount)
        );
        
        var totalDiscount = discounts.stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return Optional.ofNullable(subtotal)
            .orElse(BigDecimal.ZERO)
            .subtract(totalDiscount)
            .max(BigDecimal.ZERO);
    }
}
