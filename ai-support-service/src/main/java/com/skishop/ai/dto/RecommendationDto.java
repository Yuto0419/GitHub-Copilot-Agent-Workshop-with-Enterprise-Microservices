package com.skishop.ai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO definitions for recommendation functionality
 * Immutable data classes using Java 21 Record feature
 */
public final class RecommendationDto {

    // Private constructor to make this a utility class
    private RecommendationDto() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Recommendation request DTO
     * 
     * @param limit Result limit (1-50, default 10)
     * @param category Category filter
     * @param minPrice Minimum price
     * @param maxPrice Maximum price
     * @param excludeProductIds Product IDs to exclude
     * @param preferences User preferences
     */
    public record RecommendationRequest(
        @Min(value = 1, message = "Limit must be at least 1")
        @Max(value = 50, message = "Limit cannot exceed 50")
        Integer limit,
        String category,
        Double minPrice,
        Double maxPrice,
        List<String> excludeProductIds,
        Map<String, Object> preferences
    ) {
        
        // Validation and default value setting in record's compact constructor
        public RecommendationRequest {
            if (limit == null) {
                limit = 10;
            }
            if (excludeProductIds == null) {
                excludeProductIds = List.of();
            }
            if (preferences == null) {
                preferences = Map.of();
            }
        }
        
        /**
         * Create a basic recommendation request
         */
        public static RecommendationRequest defaultRequest() {
            return new RecommendationRequest(10, null, null, null, List.of(), Map.of());
        }
        
        /**
         * Create a recommendation request with specific category
         */
        public static RecommendationRequest forCategory(String category, Integer limit) {
            return new RecommendationRequest(limit, category, null, null, List.of(), Map.of());
        }
        
        /**
         * Create a recommendation request with price range
         */
        public static RecommendationRequest withPriceRange(Double minPrice, Double maxPrice, Integer limit) {
            return new RecommendationRequest(limit, null, minPrice, maxPrice, List.of(), Map.of());
        }
    }

    /**
     * Recommendation response DTO
     * 
     * @param recommendations List of recommended products
     * @param strategy Recommendation strategy used
     * @param metadata Metadata
     * @param generatedAt Generated timestamp
     */
    public record RecommendationResponse(
        List<ProductRecommendationDto> recommendations,
        String strategy,
        Map<String, Object> metadata,
        LocalDateTime generatedAt
    ) {
        
        public RecommendationResponse {
            if (recommendations == null) {
                recommendations = List.of();
            }
            if (metadata == null) {
                metadata = Map.of();
            }
            if (generatedAt == null) {
                generatedAt = LocalDateTime.now();
            }
        }
        
        /**
         * Get recommendation count
         */
        public int getRecommendationCount() {
            return recommendations.size();
        }
        
        /**
         * Calculate average score
         */
        public double getAverageScore() {
            return recommendations.stream()
                .mapToDouble(ProductRecommendationDto::score)
                .average()
                .orElse(0.0);
        }
    }

    /**
     * Product recommendation details DTO
     * 
     * @param productId Product ID
     * @param productName Product name
     * @param category Category
     * @param price Price
     * @param imageUrl Image URL
     * @param score Recommendation score
     * @param reason Recommendation reason
     * @param features Feature list
     */
    public record ProductRecommendationDto(
        String productId,
        String productName,
        String category,
        Double price,
        String imageUrl,
        Double score,
        String reason,
        List<String> features
    ) {
        
        public ProductRecommendationDto {
            if (features == null) {
                features = List.of();
            }
        }
        
        /**
         * Determine recommendation level (score-based)
         */
        public RecommendationLevel getRecommendationLevel() {
            if (score == null) return RecommendationLevel.LOW;
            
            if (score >= 0.8) {
                return RecommendationLevel.HIGH;
            } else if (score >= 0.6) {
                return RecommendationLevel.MEDIUM;
            } else {
                return RecommendationLevel.LOW;
            }
        }
    }
    
    /**
     * Recommendation level enum (sealed interface)
     */
    public sealed interface RecommendationLevel 
        permits RecommendationLevel.High, RecommendationLevel.Medium, RecommendationLevel.Low {
        
        record High() implements RecommendationLevel {}
        record Medium() implements RecommendationLevel {}
        record Low() implements RecommendationLevel {}
        
        static final RecommendationLevel HIGH = new High();
        static final RecommendationLevel MEDIUM = new Medium();
        static final RecommendationLevel LOW = new Low();
    }

    /**
     * Search request DTO
     * 
     * @param query Search query
     * @param limit Result limit (1-100, default 20)
     * @param category Category filter
     * @param minPrice Minimum price
     * @param maxPrice Maximum price
     * @param filters Additional filters
     */
    public record SearchRequest(
        @NotBlank(message = "Query is required")
        String query,
        
        @Min(value = 1, message = "Limit must be at least 1")
        @Max(value = 100, message = "Limit cannot exceed 100")
        Integer limit,
        
        String category,
        Double minPrice,
        Double maxPrice,
        List<String> filters
    ) {
        
        public SearchRequest {
            if (limit == null) {
                limit = 20;
            }
            if (filters == null) {
                filters = List.of();
            }
        }
        
        /**
         * Create a basic search request
         */
        public static SearchRequest of(String query) {
            return new SearchRequest(query, 20, null, null, null, List.of());
        }
        
        /**
         * Create a search request with category specification
         */
        public static SearchRequest withCategory(String query, String category) {
            return new SearchRequest(query, 20, category, null, null, List.of());
        }
    }

    /**
     * Search response DTO
     * 
     * @param results Search result list
     * @param totalCount Total count
     * @param query Search query
     * @param suggestions Search suggestions
     * @param facets Facet information
     */
    public record SearchResponse(
        List<ProductSearchResultDto> results,
        Integer totalCount,
        String query,
        List<String> suggestions,
        Map<String, Object> facets
    ) {
        
        public SearchResponse {
            if (results == null) {
                results = List.of();
            }
            if (suggestions == null) {
                suggestions = List.of();
            }
            if (facets == null) {
                facets = Map.of();
            }
        }
        
        /**
         * Get search result count
         */
        public int getResultCount() {
            return results.size();
        }
        
        /**
         * Calculate total pages when pagination information is available
         */
        public int calculateTotalPages(int pageSize) {
            return (totalCount + pageSize - 1) / pageSize;
        }
    }

    /**
     * Product search result details DTO
     * 
     * @param productId Product ID
     * @param productName Product name
     * @param description Product description
     * @param category Category
     * @param price Price
     * @param imageUrl Image URL
     * @param relevanceScore Relevance score
     * @param highlights Highlighted sections
     */
    public record ProductSearchResultDto(
        String productId,
        String productName,
        String description,
        String category,
        Double price,
        String imageUrl,
        Double relevanceScore,
        List<String> highlights
    ) {
        
        public ProductSearchResultDto {
            if (highlights == null) {
                highlights = List.of();
            }
        }
        
        /**
         * Determine relevance level
         */
        public RelevanceLevel getRelevanceLevel() {
            if (relevanceScore == null) return RelevanceLevel.LOW;
            
            if (relevanceScore >= 0.9) {
                return RelevanceLevel.VERY_HIGH;
            } else if (relevanceScore >= 0.7) {
                return RelevanceLevel.HIGH;
            } else if (relevanceScore >= 0.5) {
                return RelevanceLevel.MEDIUM;
            } else {
                return RelevanceLevel.LOW;
            }
        }
    }
    
    /**
     * Relevance level enum (sealed interface)
     */
    public sealed interface RelevanceLevel 
        permits RelevanceLevel.VeryHigh, RelevanceLevel.High, 
                RelevanceLevel.Medium, RelevanceLevel.Low {
        
        record VeryHigh() implements RelevanceLevel {}
        record High() implements RelevanceLevel {}
        record Medium() implements RelevanceLevel {}
        record Low() implements RelevanceLevel {}
        
        static final RelevanceLevel VERY_HIGH = new VeryHigh();
        static final RelevanceLevel HIGH = new High();
        static final RelevanceLevel MEDIUM = new Medium();
        static final RelevanceLevel LOW = new Low();
    }
}
