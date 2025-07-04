package com.skishop.sales.entity.jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Return Entity (PostgreSQL)
 * Manages product return information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "returns", indexes = {
    @Index(name = "idx_returns_order_id", columnList = "orderId"),
    @Index(name = "idx_returns_order_item_id", columnList = "orderItemId"),
    @Index(name = "idx_returns_return_number", columnList = "returnNumber"),
    @Index(name = "idx_returns_status", columnList = "status")
})
public class Return {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Return number (unique)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String returnNumber;

    /**
     * Order ID (foreign key)
     */
    @Column(nullable = false)
    private UUID orderId;

    /**
     * Order item ID (foreign key)
     */
    @Column(nullable = false)
    private UUID orderItemId;

    /**
     * Customer ID
     */
    @Column(nullable = false, length = 100)
    private String customerId;

    /**
     * Return reason
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReturnReason reason;

    /**
     * Return reason details
     */
    @Column(length = 1000)
    private String reasonDetail;

    /**
     * Return quantity
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Refund amount
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;

    /**
     * Return status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReturnStatus status;

    /**
     * Return request date and time
     */
    @Column(nullable = false)
    private LocalDateTime requestedAt;

    /**
     * Approval date and time
     */
    private LocalDateTime approvedAt;

    /**
     * Item received date and time
     */
    private LocalDateTime receivedAt;

    /**
     * Refund completion date and time
     */
    private LocalDateTime refundedAt;

    /**
     * Admin notes
     */
    @Column(length = 1000)
    private String adminNotes;

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
     * Order relationship (Many-to-One)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", insertable = false, updatable = false)
    private Order order;

    /**
     * Order item relationship (Many-to-One)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderItemId", insertable = false, updatable = false)
    private OrderItem orderItem;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Return reason
     */
    public enum ReturnReason {
        DEFECTIVE,        // Defective product
        WRONG_ITEM,       // Wrong item
        SIZE_ISSUE,       // Size issue
        NOT_AS_DESCRIBED, // Not as described
        DAMAGED_SHIPPING, // Damaged during shipping
        CUSTOMER_CHANGED_MIND, // Customer changed mind
        OTHER             // Other
    }

    /**
     * Return status
     */
    public enum ReturnStatus {
        REQUESTED,    // Requested
        APPROVED,     // Approved
        REJECTED,     // Rejected
        RECEIVED,     // Item received
        REFUNDED,     // Refunded
        CANCELLED     // Cancelled
    }
}
