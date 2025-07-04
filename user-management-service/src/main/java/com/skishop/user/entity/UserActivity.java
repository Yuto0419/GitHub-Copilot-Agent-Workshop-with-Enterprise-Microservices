package com.skishop.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User Activity Entity
 * Records user activity history
 */
@Entity
@Table(name = "user_activities")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @Column(length = 500)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Types of activities
     */
    public enum ActivityType {
        LOGIN,              // Login
        LOGOUT,             // Logout
        PROFILE_UPDATE,     // Profile update
        PASSWORD_CHANGE,    // Password change
        EMAIL_VERIFY,       // Email verification
        PHONE_VERIFY,       // Phone number verification
        PRODUCT_VIEW,       // Product view
        PRODUCT_SEARCH,     // Product search
        ORDER_CREATE,       // Order creation
        ORDER_CANCEL,       // Order cancellation
        CART_ADD,           // Add to cart
        CART_REMOVE,        // Remove from cart
        WISHLIST_ADD,       // Add to wishlist
        WISHLIST_REMOVE,    // Remove from wishlist
        REVIEW_CREATE,      // Create review
        SUPPORT_CONTACT,    // Contact support
        COUPON_USE,         // Use coupon
        POINT_EARN,         // Earn points
        POINT_USE,          // Use points
        ACCOUNT_SUSPEND,    // Account suspension
        ACCOUNT_RESTORE     // Account restoration
    }

    /**
     * Set metadata in JSON format
     */
    public void setMetadataJson(String json) {
        this.metadata = json;
    }

    /**
     * Get simplified device information from user agent
     */
    public String getSimplifiedDevice() {
        return switch (userAgent) {
            case null -> "Unknown";
            case String ua when ua.contains("Mobile") -> "Mobile";
            case String ua when ua.contains("Tablet") -> "Tablet";
            default -> "Desktop";
        };
    }

    /**
     * Check if the activity is security-related
     */
    public boolean isSecurityActivity() {
        return switch (activityType) {
            case LOGIN, LOGOUT, PASSWORD_CHANGE, EMAIL_VERIFY, 
                 PHONE_VERIFY, ACCOUNT_SUSPEND, ACCOUNT_RESTORE -> true;
            default -> false;
        };
    }
}
