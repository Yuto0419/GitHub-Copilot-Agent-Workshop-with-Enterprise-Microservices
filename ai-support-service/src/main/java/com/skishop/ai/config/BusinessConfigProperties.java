package com.skishop.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Business Configuration Properties
 * 
 * <p>Configuration class using Java 21's record feature</p>
 * <p>Binds business.* properties from application.yml</p>
 * 
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "business")
public record BusinessConfigProperties(
    RecommendationConfig recommendation,
    ChatbotConfig chatbot,
    SearchConfig search
) {
    
    /**
     * Create configuration with default values
     */
    public BusinessConfigProperties() {
        this(
            new RecommendationConfig(10, 3600, List.of("collaborative-filtering", "content-based", "hybrid")),
            new ChatbotConfig(50, 1800, "ja"),
            new SearchConfig(50, 10)
        );
    }
    
    /**
     * Recommendation Engine Configuration
     * 
     * @param maxResults Maximum number of recommendations
     * @param cacheTtl Cache TTL (seconds)
     * @param algorithms Available algorithms
     */
    public record RecommendationConfig(
        int maxResults,
        int cacheTtl,
        List<String> algorithms
    ) {
        public RecommendationConfig {
            // Validation with Java 21's compact constructor
            if (maxResults <= 0) {
                throw new IllegalArgumentException("maxResults must be positive");
            }
            if (cacheTtl < 0) {
                throw new IllegalArgumentException("cacheTtl must be non-negative");
            }
            if (algorithms.isEmpty()) {
                throw new IllegalArgumentException("algorithms must not be empty");
            }
        }
    }
    
    /**
     * Chatbot Configuration
     * 
     * @param maxConversationLength Maximum conversation length
     * @param sessionTimeout Session timeout (seconds)
     * @param defaultLanguage Default language
     */
    public record ChatbotConfig(
        int maxConversationLength,
        int sessionTimeout,
        String defaultLanguage
    ) {
        public ChatbotConfig {
            if (maxConversationLength <= 0) {
                throw new IllegalArgumentException("maxConversationLength must be positive");
            }
            if (sessionTimeout <= 0) {
                throw new IllegalArgumentException("sessionTimeout must be positive");
            }
            if (defaultLanguage == null || defaultLanguage.trim().isEmpty()) {
                throw new IllegalArgumentException("defaultLanguage must not be null or empty");
            }
        }
    }
    
    /**
     * Search Configuration
     * 
     * @param maxResults Maximum number of search results
     * @param autocompleteMaxResults Maximum number of autocomplete results
     */
    public record SearchConfig(
        int maxResults,
        int autocompleteMaxResults
    ) {
        public SearchConfig {
            if (maxResults <= 0) {
                throw new IllegalArgumentException("maxResults must be positive");
            }
            if (autocompleteMaxResults <= 0) {
                throw new IllegalArgumentException("autocompleteMaxResults must be positive");
            }
        }
    }
}
