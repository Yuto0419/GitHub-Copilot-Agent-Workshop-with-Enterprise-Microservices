package com.skishop.sales.entity.jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Order Entity (PostgreSQL)
 * Manages customer order information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_customer_id", columnList = "customerId"),
    @Index(name = "idx_orders_status", columnList = "status"),
    @Index(name = "idx_orders_order_date", columnList = "orderDate"),
    @Index(name = "idx_orders_order_number", columnList = "orderNumber")
})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Order number (unique)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;

    /**
     * Customer ID
     */
    @Column(nullable = false)
    private String customerId;

    /**
     * Order date and time
     */
    @Column(nullable = false)
    private LocalDateTime orderDate;

    /**
     * Order status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    /**
     * Payment status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    /**
     * Payment method
     */
    @Column(nullable = false, length = 50)
    private String paymentMethod;

    /**
     * Subtotal amount
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotalAmount;

    /**
     * Tax amount
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    /**
     * Shipping fee
     */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    /**
     * Discount amount
     */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /**
     * Total amount
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Coupon code
     */
    @Column(length = 50)
    private String couponCode;

    /**
     * Used points
     */
    @Builder.Default
    private Integer usedPoints = 0;

    /**
     * Point discount amount
     */
    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal pointDiscountAmount = BigDecimal.ZERO;

    /**
     * Shipping address
     */
    @Embedded
    private ShippingAddress shippingAddress;

    /**
     * Currency code
     */
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currencyCode = "JPY";

    /**
     * Notes
     */
    @Column(length = 1000)
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
     * Order items list
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    /**
     * Pre-persist entity processing
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (orderDate == null) {
            orderDate = now;
        }
        updatedAt = now;
        if (status == null) {
            status = OrderStatus.PENDING;
        }
        if (currencyCode == null) {
            currencyCode = "JPY";
        }
        if (shippingFee == null) {
            shippingFee = BigDecimal.ZERO;
        }
        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }
    }

    /**
     * Pre-update entity processing
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Order status enumeration
     * Leveraging Java 21's extended Enum functionality
     */
    public enum OrderStatus {
        PENDING("Order pending", "We have received your order"),
        CONFIRMED("Order confirmed", "Order details confirmed and processing started"),
        PROCESSING("Processing", "Preparing products"),
        SHIPPED("Shipped", "Products have been shipped"),
        DELIVERED("Delivered", "Product delivery completed"),
        CANCELLED("Cancelled", "Order has been cancelled"),
        RETURNED("Returned", "Products have been returned");

        private final String displayName;
        private final String description;

        OrderStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        /**
         * Using Java 21's Switch expression to determine status category
         */
        public StatusCategory getCategory() {
            return switch (this) {
                case PENDING, CONFIRMED -> StatusCategory.ACTIVE;
                case PROCESSING, SHIPPED -> StatusCategory.IN_PROGRESS;
                case DELIVERED -> StatusCategory.COMPLETED;
                case CANCELLED, RETURNED -> StatusCategory.TERMINATED;
            };
        }

        /**
         * Status category enumeration
         */
        public enum StatusCategory {
            ACTIVE, IN_PROGRESS, COMPLETED, TERMINATED
        }
    }

    /**
     * Payment status enumeration
     * Leveraging Java 21's extended Enum functionality
     */
    public enum PaymentStatus {
        PENDING("Payment pending", false),
        PAID("Payment completed", true),
        FAILED("Payment failed", false),
        REFUNDED("Refund completed", false),
        PARTIALLY_REFUNDED("Partial refund", false);

        private final String displayName;
        private final boolean isSuccessful;

        PaymentStatus(String displayName, boolean isSuccessful) {
            this.displayName = displayName;
            this.isSuccessful = isSuccessful;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isSuccessful() {
            return isSuccessful;
        }

        /**
         * Using Java 21's Switch expression to get payment status details
         */
        public PaymentDetails getPaymentDetails() {
            return switch (this) {
                case PENDING -> new PaymentDetails("Payment processing", "#FFA500", false);
                case PAID -> new PaymentDetails("Payment completed", "#008000", true);
                case FAILED -> new PaymentDetails("Payment failed", "#FF0000", false);
                case REFUNDED -> new PaymentDetails("Full refund completed", "#0000FF", true);
                case PARTIALLY_REFUNDED -> new PaymentDetails("Partial refund completed", "#800080", false);
            };
        }

        /**
         * Record to hold payment detail information
         */
        public record PaymentDetails(String message, String colorCode, boolean isFinal) {}
    }

    /**
     * Shipping address embeddable class
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShippingAddress {
        @Column(name = "shipping_postal_code", length = 10)
        private String postalCode;
        
        @Column(name = "shipping_prefecture", length = 20)
        private String prefecture;
        
        @Column(name = "shipping_city", length = 50)
        private String city;
        
        @Column(name = "shipping_address_line1", length = 100)
        private String addressLine1;
        
        @Column(name = "shipping_address_line2", length = 100)
        private String addressLine2;
        
        @Column(name = "shipping_recipient_name", length = 100)
        private String recipientName;
        
        @Column(name = "shipping_phone", length = 20)
        private String phone;
    }

    /**
     * Billing address embeddable class
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BillingAddress {
        @Column(name = "billing_postal_code", length = 10)
        private String postalCode;
        
        @Column(name = "billing_prefecture", length = 20)
        private String prefecture;
        
        @Column(name = "billing_city", length = 50)
        private String city;
        
        @Column(name = "billing_address_line1", length = 100)
        private String addressLine1;
        
        @Column(name = "billing_address_line2", length = 100)
        private String addressLine2;
        
        @Column(name = "billing_recipient_name", length = 100)
        private String recipientName;
        
        @Column(name = "billing_phone", length = 20)
        private String phone;
    }
}
