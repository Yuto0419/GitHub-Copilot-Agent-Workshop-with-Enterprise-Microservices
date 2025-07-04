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
 * Payment entity (PostgreSQL)
 * Manages payment processing and history
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_user_id", columnList = "userId"),
    @Index(name = "idx_payments_cart_id", columnList = "cartId"),
    @Index(name = "idx_payments_status", columnList = "status"),
    @Index(name = "idx_payments_intent_id", columnList = "paymentIntentId")
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * User ID
     */
    @Column(nullable = false)
    private UUID userId;

    /**
     * Cart ID
     */
    @Column(nullable = false)
    private UUID cartId;

    /**
     * Payment intent ID (e.g., Stripe)
     */
    @Column(unique = true, length = 200)
    private String paymentIntentId;

    /**
     * Payment method
     */
    @Column(nullable = false, length = 50)
    private String paymentMethod;

    /**
     * Payment amount
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * Currency code
     */
    @Column(nullable = false, length = 3)
    private String currency;

    /**
     * Payment status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    /**
     * Payment gateway provider
     */
    @Column(nullable = false, length = 50)
    private String gatewayProvider;

    /**
     * Gateway response (JSON format)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> gatewayResponse;

    /**
     * Failure reason
     */
    @Column(length = 500)
    private String failureReason;

    /**
     * Refunded amount
     */
    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    /**
     * Created at
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Updated at
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Payment completed at
     */
    private LocalDateTime completedAt;

    /**
     * Pre-persist entity processing
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
        if (currency == null) {
            currency = "JPY";
        }
    }

    /**
     * Pre-update entity processing
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == PaymentStatus.COMPLETED && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }

    /**
     * Payment status enum
     */
    public enum PaymentStatus {
        PENDING("Processing"),
        REQUIRES_ACTION("Action Required"),
        CONFIRMED("Confirmed"),
        COMPLETED("Completed"),
        FAILED("Failed"),
        CANCELLED("Cancelled"),
        REFUNDED("Refunded"),
        PARTIALLY_REFUNDED("Partially Refunded");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
