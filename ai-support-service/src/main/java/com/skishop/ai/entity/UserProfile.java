package com.skishop.ai.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * User profile entity
 * 
 * <p>Immutable data class using Java 21's record feature</p>
 * <p>Persisted as MongoDB Document and used as input for AI recommendation engine</p>
 * 
 * @param userId User ID (MongoDB _id)
 * @param preferences User preferences
 * @param purchaseHistory Purchase history (list of product IDs)
 * @param viewedProducts Viewed products history (list of product IDs)
 * @param searchHistory Search history
 * @param categoryPreferences Category preference scores
 * @param loyaltyTier Loyalty tier
 * @param totalSpent Total amount spent
 * @param lastActivity Last activity timestamp
 * @param favoriteCategories Favorite categories
 * @param behaviorMetrics Behavior metrics
 * @param createdAt Creation timestamp
 * @param updatedAt Update timestamp
 * 
 * @since 1.0.0
 */
@Document(collection = "user_profiles")
public record UserProfile(
    @Id
    String userId,
    Map<String, Object> preferences,
    List<String> purchaseHistory,
    List<String> viewedProducts,
    List<String> searchHistory,
    Map<String, Double> categoryPreferences,
    String loyaltyTier,
    Double totalSpent,
    LocalDateTime lastActivity,
    List<String> favoriteCategories,
    Map<String, Object> behaviorMetrics,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    
    /**
     * Factory method for creating new user profile
     */
    public static UserProfile createNew(String userId) {
        var now = LocalDateTime.now();
        return new UserProfile(
            userId,
            Map.of(),
            List.of(),
            List.of(),
            List.of(),
            Map.of(),
            "BRONZE",
            0.0,
            now,
            List.of(),
            Map.of(),
            now,
            now
        );
    }
    
    /**
     * Factory method for updating profile
     */
    public UserProfile withUpdatedActivity() {
        return new UserProfile(
            userId,
            preferences,
            purchaseHistory,
            viewedProducts,
            searchHistory,
            categoryPreferences,
            loyaltyTier,
            totalSpent,
            LocalDateTime.now(), // updating lastActivity
            favoriteCategories,
            behaviorMetrics,
            createdAt,
            LocalDateTime.now()  // updating updatedAt
        );
    }
}
