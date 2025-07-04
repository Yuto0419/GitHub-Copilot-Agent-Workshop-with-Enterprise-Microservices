package com.skishop.sales.entity.jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Shipment Entity (PostgreSQL)
 * Manages shipping information for orders
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "shipments", indexes = {
    @Index(name = "idx_shipments_order_id", columnList = "orderId"),
    @Index(name = "idx_shipments_tracking_number", columnList = "trackingNumber"),
    @Index(name = "idx_shipments_status", columnList = "status")
})
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Order ID (foreign key)
     */
    @Column(nullable = false)
    private UUID orderId;

    /**
     * Shipping carrier
     */
    @Column(nullable = false, length = 100)
    private String carrier;

    /**
     * Tracking number
     */
    @Column(length = 100)
    private String trackingNumber;

    /**
     * Shipment status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status;

    /**
     * Shipping address
     */
    @Embedded
    private ShippingAddress shippingAddress;

    /**
     * Shipping date and time
     */
    private LocalDateTime shippedAt;

    /**
     * Estimated delivery date and time
     */
    private LocalDateTime estimatedDeliveryAt;

    /**
     * Delivery completion date and time
     */
    private LocalDateTime deliveredAt;

    /**
     * Notes
     */
    @Column(length = 500)
    private String notes;

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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Shipment status
     */
    public enum ShipmentStatus {
        PREPARING,     // Preparing
        SHIPPED,       // Shipped
        IN_TRANSIT,    // In transit
        OUT_FOR_DELIVERY, // Out for delivery
        DELIVERED,     // Delivered
        FAILED_DELIVERY,  // Failed delivery
        RETURNED       // Returned
    }

    /**
     * Shipping address
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddress {
        
        @Column(name = "shipping_postal_code", length = 10)
        private String postalCode;
        
        @Column(name = "shipping_prefecture", length = 50)
        private String prefecture;
        
        @Column(name = "shipping_city", length = 100)
        private String city;
        
        @Column(name = "shipping_address_line1", length = 200)
        private String addressLine1;
        
        @Column(name = "shipping_address_line2", length = 200)
        private String addressLine2;
        
        @Column(name = "shipping_recipient_name", length = 100)
        private String recipientName;
        
        @Column(name = "shipping_phone_number", length = 20)
        private String phoneNumber;
    }
}
