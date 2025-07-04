package com.skishop.point.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Point Transaction Entity
 * Manages the history of point acquisition, usage, and expiration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "point_transactions", indexes = {
    @Index(name = "idx_point_transactions_user_created", columnList = "userId, createdAt"),
    @Index(name = "idx_point_transactions_expires_at", columnList = "expiresAt"),
    @Index(name = "idx_point_transactions_reference", columnList = "referenceId"),
    @Index(name = "idx_point_transactions_type", columnList = "transactionType")
})
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer balanceAfter;

    @Column(nullable = false, length = 100)
    private String reason;

    @Column(length = 100)
    private String referenceId;

    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isExpired = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Transaction type enumeration
     */
    public enum TransactionType {
        EARNED("Earned"),
        REDEEMED("Used"),
        EXPIRED("Expired"),
        TRANSFERRED_IN("Received"),
        TRANSFERRED_OUT("Sent"),
        BONUS("Bonus"),
        ADJUSTMENT("Adjustment");

        private final String displayName;

        TransactionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Expire points
     */
    public void expire() {
        this.isExpired = true;
    }

    /**
     * Check expiration date
     */
    public boolean isExpiring(int days) {
        if (expiresAt == null) {
            return false;
        }
        return expiresAt.isBefore(LocalDateTime.now().plusDays(days));
    }
}
