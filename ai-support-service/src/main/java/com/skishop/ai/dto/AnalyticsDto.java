package com.skishop.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Analytics feature DTO definitions
 * 
 * <p>Data transfer objects for AI analytics features utilizing Java 21's Record type</p>
 * <p>Combined with Sealed Interface to ensure type safety</p>
 * 
 * @since 1.0.0
 */
public class AnalyticsDto {
    
    /**
     * User behavior analysis response
     */
    @Schema(description = "User behavior analysis results")
    public record UserBehaviorResponse(
            @Schema(description = "User ID") UUID userId,
            @Schema(description = "Analysis period") AnalysisPeriod period,
            @Schema(description = "Behavior patterns") BehaviorPatterns behaviorPatterns,
            @Schema(description = "Engagement metrics") EngagementMetrics engagement,
            @Schema(description = "Purchase patterns") PurchasePatterns purchasePatterns,
            @Schema(description = "Recommendations") List<Recommendation> recommendations,
            @Schema(description = "Analysis metadata") AnalysisMetadata metadata
    ) {
        /**
         * User behavior patterns
         */
        public record BehaviorPatterns(
                @Schema(description = "Average session duration (minutes)") double avgSessionDuration,
                @Schema(description = "Page views") int totalPageViews,
                @Schema(description = "Search count") int searchCount,
                @Schema(description = "Number of cart additions") int cartAdditions,
                @Schema(description = "Conversion rate") double conversionRate,
                @Schema(description = "Peak activity hours") List<String> peakActivityHours,
                @Schema(description = "Device distribution") Map<String, Integer> deviceDistribution
        ) {}
        
        /**
         * Engagement metrics
         */
        public record EngagementMetrics(
                @Schema(description = "Engagement score") double score,
                @Schema(description = "Repeat rate") double repeatRate,
                @Schema(description = "Bounce rate") double bounceRate,
                @Schema(description = "Loyalty level") String loyaltyLevel,
                @Schema(description = "Number of interactions") int totalInteractions,
                @Schema(description = "Social shares") int socialShares
        ) {}
        
        /**
         * Purchase patterns
         */
        public record PurchasePatterns(
                @Schema(description = "Average order value") BigDecimal avgOrderValue,
                @Schema(description = "Purchase frequency") double purchaseFrequency,
                @Schema(description = "Preferred categories") List<String> preferredCategories,
                @Schema(description = "Price sensitivity") String priceSensitivity,
                @Schema(description = "Seasonality") Map<String, Double> seasonality,
                @Schema(description = "Last purchase date") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastPurchase
        ) {}
    }
    
    /**
     * Product sentiment analysis response
     */
    @Schema(description = "Product sentiment analysis results")
    public record SentimentAnalysisResponse(
            @Schema(description = "Product ID") String productId,
            @Schema(description = "Analysis period") AnalysisPeriod period,
            @Schema(description = "Overall sentiment score") SentimentScore overallSentiment,
            @Schema(description = "Review analysis") ReviewAnalysis reviewAnalysis,
            @Schema(description = "Sentiment trends") List<SentimentTrend> sentimentTrends,
            @Schema(description = "Keyword analysis") KeywordAnalysis keywordAnalysis,
            @Schema(description = "Analysis metadata") AnalysisMetadata metadata
    ) {
        /**
         * Sentiment score
         */
        public record SentimentScore(
                @Schema(description = "Positive ratio") double positive,
                @Schema(description = "Negative ratio") double negative,
                @Schema(description = "Neutral ratio") double neutral,
                @Schema(description = "Overall score (-1.0 to 1.0)") double overallScore,
                @Schema(description = "Confidence") double confidence
        ) {}
        
        /**
         * Review analysis
         */
        public record ReviewAnalysis(
                @Schema(description = "Total reviews") int totalReviews,
                @Schema(description = "Average rating") double averageRating,
                @Schema(description = "Category scores") Map<String, Double> categoryScores,
                @Schema(description = "Positive keywords") List<String> positiveKeywords,
                @Schema(description = "Negative keywords") List<String> negativeKeywords
        ) {}
        
        /**
         * Sentiment trend
         */
        public record SentimentTrend(
                @Schema(description = "Date") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                @Schema(description = "Sentiment score") double sentimentScore,
                @Schema(description = "Review count") int reviewCount
        ) {}
        
        /**
         * Keyword analysis
         */
        public record KeywordAnalysis(
                @Schema(description = "Positive keywords") List<String> positiveKeywords,
                @Schema(description = "Negative keywords") List<String> negativeKeywords,
                @Schema(description = "Keyword frequency") Map<String, Integer> keywordFrequency
        ) {}
    }
    
    /**
     * Trend analysis response
     */
    @Schema(description = "Trend analysis results")
    public record TrendAnalysisResponse(
            @Schema(description = "Analysis period") AnalysisPeriod period,
            @Schema(description = "Trend type") String trendType,
            @Schema(description = "Trend data") List<TrendDataPoint> trendData,
            @Schema(description = "Trend insights") List<TrendInsight> insights,
            @Schema(description = "Trend predictions") List<TrendPrediction> predictions,
            @Schema(description = "Analysis metadata") AnalysisMetadata metadata
    ) {
        /**
         * Trend data point
         */
        public record TrendDataPoint(
                @Schema(description = "Category/time point") String category,
                @Schema(description = "Value") double value,
                @Schema(description = "Additional attributes") Map<String, Object> attributes
        ) {}
        
        /**
         * Trend insight
         */
        public record TrendInsight(
                @Schema(description = "Insight type") String type,
                @Schema(description = "Description") String description,
                @Schema(description = "Impact") String impact,
                @Schema(description = "Confidence") double confidence
        ) {}
        
        /**
         * Trend prediction
         */
        public record TrendPrediction(
                @Schema(description = "Prediction date") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                @Schema(description = "Predicted value") double predictedValue,
                @Schema(description = "Confidence") double confidence,
                @Schema(description = "Lower bound") double lowerBound,
                @Schema(description = "Upper bound") double upperBound
        ) {}
    }
    
    /**
     * Common record definitions
     */
    
    /**
     * Analysis period
     */
    public record AnalysisPeriod(
            @Schema(description = "Start date") @JsonFormat(pattern = "yyyy-MM-dd") LocalDateTime startDate,
            @Schema(description = "End date") @JsonFormat(pattern = "yyyy-MM-dd") LocalDateTime endDate,
            @Schema(description = "Time range") String timeRange,
            @Schema(description = "Days") int days
    ) {}
    
    /**
     * Recommendation
     */
    public record Recommendation(
            @Schema(description = "Recommendation title") String title,
            @Schema(description = "Recommendation description") String description,
            @Schema(description = "Priority") String priority,
            @Schema(description = "Actions") List<String> actions
    ) {}
    
    /**
     * Analysis metadata
     */
    public record AnalysisMetadata(
            @Schema(description = "Analysis model name") String modelName,
            @Schema(description = "Confidence") double confidence,
            @Schema(description = "Generated at") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime generatedAt,
            @Schema(description = "Additional parameters") Map<String, Object> parameters
    ) {}
}
