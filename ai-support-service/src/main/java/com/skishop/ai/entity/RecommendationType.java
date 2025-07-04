package com.skishop.ai.entity;

import java.util.List;

/**
 * Product Recommendation Type - Using Java 21's sealed interface
 */
public sealed interface RecommendationType
    permits RecommendationType.Collaborative, RecommendationType.ContentBased, RecommendationType.Hybrid {
    
    record Collaborative(String algorithm) implements RecommendationType {}
    record ContentBased(List<String> features) implements RecommendationType {}
    record Hybrid(String primaryAlgorithm, List<String> fallbackAlgorithms) implements RecommendationType {}
    
    /**
     * Convert recommendation type to string representation
     */
    default String toStringValue() {
        return switch (this) {
            case Collaborative(var algorithm) -> "COLLABORATIVE_" + algorithm.toUpperCase();
            case ContentBased(var features) -> "CONTENT_BASED_" + String.join("_", features);
            case Hybrid(var primary, var fallback) -> "HYBRID_" + primary + "_WITH_" + String.join("_", fallback);
        };
    }
}
