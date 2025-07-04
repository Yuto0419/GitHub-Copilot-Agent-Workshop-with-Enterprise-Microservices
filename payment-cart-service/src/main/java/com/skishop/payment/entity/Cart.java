package com.skishop.payment.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Cart Entity (PostgreSQL)
 * Manages shopping cart information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "carts", indexes = {
    @Index(name = "idx_carts_user_id", columnList = "userId"),
    @Index(name = "idx_carts_expires_at", columnList = "expiresAt")
})
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * User ID
     */
    @Column(nullable = false)
    private UUID userId;

    /**
     * Cart total amount
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Currency code
     */
    @Column(nullable = false, length = 3)
    private String currency;

    /**
     * Creation date
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Update date
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Expiration date
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Cart items list
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartItem> items;

    /**
     * Pre-entity creation processing
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (expiresAt == null) {
            expiresAt = now.plusDays(7); // Valid for 7 days
        }
        if (currency == null) {
            currency = "JPY";
        }
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
    }

    /**
     * Pre-entity update processing
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calculate total amount
     */
    public void calculateTotal() {
        if (items != null && !items.isEmpty()) {
            totalAmount = items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            totalAmount = BigDecimal.ZERO;
        }
    }
}
