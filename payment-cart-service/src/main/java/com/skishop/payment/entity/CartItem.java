package com.skishop.payment.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Cart Item Entity (PostgreSQL)
 * Manages detailed information of products included in the cart
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "cart_items", indexes = {
    @Index(name = "idx_cart_items_cart_id", columnList = "cartId"),
    @Index(name = "idx_cart_items_product_id", columnList = "productId")
})
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Cart ID (foreign key)
     */
    @Column(nullable = false)
    private UUID cartId;

    /**
     * Product ID
     */
    @Column(nullable = false)
    private UUID productId;

    /**
     * Quantity
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Unit price (price when added to cart)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Subtotal (unit price × quantity)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    /**
     * Product detail information (JSON format)
     * Snapshot of product information when added to cart
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> productDetails;

    /**
     * Creation date and time
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Update date and time
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Relationship with cart (many-to-one)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartId", insertable = false, updatable = false)
    private Cart cart;

    /**
     * Pre-processing before entity creation
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        calculateTotalPrice();
    }

    /**
     * Pre-processing before entity update
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotalPrice();
    }

    /**
     * Calculate subtotal
     */
    public void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
