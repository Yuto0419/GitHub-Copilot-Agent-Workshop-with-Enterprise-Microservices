package com.skishop.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Forecast Functionality DTO Definitions
 * 
 * <p>Data transfer objects for AI forecasting functionality leveraging Java 21's Record types</p>
 * <p>Supports demand forecasting, price forecasting, inventory forecasting, and other functions</p>
 * 
 * @since 1.0.0
 */
public class ForecastDto {
    
    /**
     * Demand forecast response
     */
    @Schema(description = "Demand forecast result")
    public record DemandForecastResponse(
            @Schema(description = "Product ID") String productId,
            @Schema(description = "Forecast period") ForecastPeriod period,
            @Schema(description = "Forecast model") String model,
            @Schema(description = "Demand forecast data") List<DemandForecastPoint> forecastData,
            @Schema(description = "Statistics") ForecastStatistics statistics,
            @Schema(description = "Confidence interval") ConfidenceInterval confidenceInterval,
            @Schema(description = "Alerts") List<ForecastAlert> alerts,
            @Schema(description = "Metadata") ForecastMetadata metadata
    ) {
        /**
         * Demand forecast point
         */
        public record DemandForecastPoint(
                @Schema(description = "Date") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                @Schema(description = "Predicted demand") int predictedDemand,
                @Schema(description = "Confidence") double confidence,
                @Schema(description = "Seasonality factor") double seasonalityFactor,
                @Schema(description = "Trend factor") double trendFactor
        ) {}
    }
    
    /**
     * Price forecast response
     */
    @Schema(description = "Price forecast result")
    public record PriceForecastResponse(
            @Schema(description = "Product ID") String productId,
            @Schema(description = "Current price") BigDecimal currentPrice,
            @Schema(description = "Recommended price") BigDecimal recommendedPrice,
            @Schema(description = "Target profit margin") double targetMargin,
            @Schema(description = "Price scenarios") List<PriceScenario> priceScenarios,
            @Schema(description = "Competitor analysis") CompetitorAnalysis competitorAnalysis,
            @Schema(description = "Price sensitivity analysis") PriceSensitivityAnalysis sensitivity,
            @Schema(description = "Revenue forecast") RevenueForecast revenueForecast,
            @Schema(description = "Metadata") ForecastMetadata metadata
    ) {
        /**
         * Price scenario
         */
        public record PriceScenario(
                @Schema(description = "Scenario name") String scenarioName,
                @Schema(description = "price") BigDecimal price,
                @Schema(description = "forecast sales") int forecastSales,
                @Schema(description = "forecast revenue") BigDecimal forecastRevenue,
                @Schema(description = "margin rate") double marginRate,
                @Schema(description = "success probability") double successProbability
        ) {}
        
        /**
         * Competitor analysis
         */
        public record CompetitorAnalysis(
                @Schema(description = "average competitor price") BigDecimal avgCompetitorPrice,
                @Schema(description = "minimum competitor price") BigDecimal minCompetitorPrice,
                @Schema(description = "maximum competitor price") BigDecimal maxCompetitorPrice,
                @Schema(description = "market position") String marketPosition,
                @Schema(description = "price advantage") double priceAdvantage
        ) {}
        
        /**
         * Price sensitivity analysis
         */
        public record PriceSensitivityAnalysis(
                @Schema(description = "price elasticity") double priceElasticity,
                @Schema(description = "optimal price range") PriceRange optimalPriceRange,
                @Schema(description = "segment sensitivity") Map<String, Double> segmentSensitivity
        ) {}
        
        /**
         * Price range
         */
        public record PriceRange(
                @Schema(description = "minimum price") BigDecimal minPrice,
                @Schema(description = "maximum price") BigDecimal maxPrice,
                @Schema(description = "recommended price") BigDecimal recommendedPrice
        ) {}
        
        /**
         * Revenue forecast
         */
        public record RevenueForecast(
                @Schema(description = "30-day forecast revenue") BigDecimal revenue30Days,
                @Schema(description = "90-day forecast revenue") BigDecimal revenue90Days,
                @Schema(description = "annual forecast revenue") BigDecimal revenueAnnual,
                @Schema(description = "forecast ROI") double forecastROI
        ) {}
    }
    
    /**
     * Inventory forecast response
     */
    @Schema(description = "inventory forecast results")
    public record InventoryForecastResponse(
            @Schema(description = "Product ID") String productId,
            @Schema(description = "Current stock level") int currentStock,
            @Schema(description = "Inventory forecast data") List<InventoryForecastPoint> forecastData,
            @Schema(description = "Reorder recommendation") ReorderRecommendation reorderRecommendation,
            @Schema(description = "Inventory optimization") InventoryOptimization optimization,
            @Schema(description = "Risk analysis") InventoryRiskAnalysis riskAnalysis,
            @Schema(description = "Metadata") ForecastMetadata metadata
    ) {
        /**
         * Inventory forecast point
         */
        public record InventoryForecastPoint(
                @Schema(description = "date") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                @Schema(description = "predicted stock") int predictedStock,
                @Schema(description = "predicted demand") int predictedDemand,
                @Schema(description = "stockout risk") double stockoutRisk,
                @Schema(description = "recommended action") String recommendedAction
        ) {}
        
        /**
         * Reorder recommendation
         */
        public record ReorderRecommendation(
                @Schema(description = "reorder date") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate reorderDate,
                @Schema(description = "recommended quantity") int recommendedQuantity,
                @Schema(description = "urgency") String urgency,
                @Schema(description = "reason") String reason,
                @Schema(description = "estimated cost") BigDecimal estimatedCost
        ) {}
        
        /**
         * Inventory optimization
         */
        public record InventoryOptimization(
                @Schema(description = "optimal stock level") int optimalStockLevel,
                @Schema(description = "safety stock") int safetyStock,
                @Schema(description = "economic order quantity") int economicOrderQuantity,
                @Schema(description = "target turnover rate") double targetTurnoverRate,
                @Schema(description = "potential cost savings") BigDecimal potentialCostSavings
        ) {}
        
        /**
         * Inventory risk analysis
         */
        public record InventoryRiskAnalysis(
                @Schema(description = "stockout risk") double stockoutRisk,
                @Schema(description = "overstock risk") double overstockRisk,
                @Schema(description = "obsolescence risk") double obsolescenceRisk,
                @Schema(description = "overall risk score") double overallRiskScore,
                @Schema(description = "mitigation strategies") List<String> mitigationStrategies
        ) {}
    }
    
    /**
     * Seasonal forecast response
     */
    @Schema(description = "seasonal forecast results")
    public record SeasonalForecastResponse(
            @Schema(description = "Category") String category,
            @Schema(description = "forecast year") int year,
            @Schema(description = "region") String region,
            @Schema(description = "monthly forecasts") List<MonthlyForecast> monthlyForecasts,
            @Schema(description = "Seasonal pattern") SeasonalPattern seasonalPattern,
            @Schema(description = "peak analysis") PeakAnalysis peakAnalysis,
            @Schema(description = "Event impacts") List<EventImpact> eventImpacts,
            @Schema(description = "Metadata") ForecastMetadata metadata
    ) {
        /**
         * Monthly forecast
         */
        public record MonthlyForecast(
                @Schema(description = "month") int month,
                @Schema(description = "forecast demand index") double demandIndex,
                @Schema(description = "year-over-year change") double yearOverYearChange,
                @Schema(description = "confidence") double confidence,
                @Schema(description = "recommended stock level") String recommendedStockLevel
        ) {}
        
        /**
         * Seasonal pattern
         */
        public record SeasonalPattern(
                @Schema(description = "Pattern type") String patternType,
                @Schema(description = "seasonality strength") double seasonalityStrength,
                @Schema(description = "peak months") List<Integer> peakMonths,
                @Schema(description = "off-season months") List<Integer> offSeasonMonths,
                @Schema(description = "coefficient of variation") double coefficientOfVariation
        ) {}
        
        /**
         * Peak analysis
         */
        public record PeakAnalysis(
                @Schema(description = "primary peak") Peak primaryPeak,
                @Schema(description = "secondary peaks") List<Peak> secondaryPeaks,
                @Schema(description = "peak preparation days") int peakPreparationDays,
                @Schema(description = "stock buildup recommendations") List<StockBuildupRecommendation> stockBuildupRecommendations
        ) {}
        
        /**
         * Peak
         */
        public record Peak(
                @Schema(description = "start month") int startMonth,
                @Schema(description = "end month") int endMonth,
                @Schema(description = "intensity") double intensity,
                @Schema(description = "forecast demand increase rate") double demandIncrease
        ) {}
        
        /**
         * Stock buildup recommendation
         */
        public record StockBuildupRecommendation(
                @Schema(description = "product group") String productGroup,
                @Schema(description = "buildup start date") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate buildupStartDate,
                @Schema(description = "target stock level") double targetStockLevel,
                @Schema(description = "recommended order quantity") int recommendedOrderQuantity
        ) {}
        
        /**
         * Event impact
         */
        public record EventImpact(
                @Schema(description = "event name") String eventName,
                @Schema(description = "impact period") DateRange impactPeriod,
                @Schema(description = "demand change rate") double demandChangeRate,
                @Schema(description = "impact level") String impactLevel
        ) {}
    }
    
    /**
     * Forecast dashboard response
     */
    @Schema(description = "Forecast dashboard")
    public record ForecastDashboardResponse(
            @Schema(description = "Dashboard type") String dashboardType,
            @Schema(description = "Generated at") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime generatedAt,
            @Schema(description = "Summary") ForecastSummary summary,
            @Schema(description = "Alerts") List<ForecastAlert> alerts,
            @Schema(description = "Key metrics") List<KeyMetric> keyMetrics,
            @Schema(description = "Forecast accuracy") ForecastAccuracy accuracy,
            @Schema(description = "Recommended actions") List<RecommendedAction> recommendedActions
    ) {
        /**
         * Forecast summary
         */
        public record ForecastSummary(
                @Schema(description = "Monitored products count") int monitoredProducts,
                @Schema(description = "High accuracy forecast rate") double highAccuracyRate,
                @Schema(description = "Average forecast error") double avgForecastError,
                @Schema(description = "Optimized products count") int optimizedProducts
        ) {}
        
        /**
         * Key metric
         */
        public record KeyMetric(
                @Schema(description = "Metric name") String metricName,
                @Schema(description = "Current value") double currentValue,
                @Schema(description = "Forecast value") double forecastValue,
                @Schema(description = "Change rate") double changeRate,
                @Schema(description = "Trend") String trend
        ) {}
        
        /**
         * Recommended action
         */
        public record RecommendedAction(
                @Schema(description = "Action type") String actionType,
                @Schema(description = "Target product") String targetProduct,
                @Schema(description = "Description") String description,
                @Schema(description = "Priority") String priority,
                @Schema(description = "Deadline") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate deadline
        ) {}
    }
    
    /**
     * Accuracy evaluation response
     */
    @Schema(description = "Forecast accuracy evaluation results")
    public record AccuracyEvaluationResponse(
            @Schema(description = "Model ID") String modelId,
            @Schema(description = "Evaluation period") DateRange evaluationPeriod,
            @Schema(description = "Accuracy metrics") AccuracyMetrics accuracyMetrics,
            @Schema(description = "Detailed evaluations") List<DetailedEvaluation> detailedEvaluations,
            @Schema(description = "Improvement suggestions") List<ImprovementSuggestion> improvementSuggestions,
            @Schema(description = "Metadata") ForecastMetadata metadata
    ) {
        /**
         * Accuracy metrics
         */
        public record AccuracyMetrics(
                @Schema(description = "MAPE (Mean Absolute Percentage Error)") double mape,
                @Schema(description = "RMSE (Root Mean Square Error)") double rmse,
                @Schema(description = "MAE (Mean Absolute Error)") double mae,
                @Schema(description = "R-squared (Coefficient of determination)") double rSquared,
                @Schema(description = "Overall rating") String overallRating
        ) {}
        
        /**
         * Detailed evaluation
         */
        public record DetailedEvaluation(
                @Schema(description = "Product ID") String productId,
                @Schema(description = "Forecast accuracy") double accuracy,
                @Schema(description = "Error analysis") ErrorAnalysis errorAnalysis,
                @Schema(description = "Improvement points") List<String> improvementPoints
        ) {}
        
        /**
         * Error analysis
         */
        public record ErrorAnalysis(
                @Schema(description = "Mean error") double meanError,
                @Schema(description = "Standard deviation") double standardDeviation,
                @Schema(description = "Maximum error") double maxError,
                @Schema(description = "Error distribution") Map<String, Integer> errorDistribution
        ) {}
        
        /**
         * Improvement suggestion
         */
        public record ImprovementSuggestion(
                @Schema(description = "Improvement area") String improvementArea,
                @Schema(description = "Suggestion content") String suggestion,
                @Schema(description = "Expected benefit") String expectedBenefit,
                @Schema(description = "Implementation difficulty") String implementationDifficulty
        ) {}
    }
    
    /**
     * Batch forecast request
     */
    @Schema(description = "Batch forecast request")
    public record BatchForecastRequest(
            @Schema(description = "Product ID list") List<String> productIds,
            @Schema(description = "Forecast type") String forecastType,
            @Schema(description = "Forecast period (days)") int forecastDays,
            @Schema(description = "Forecast model") String model,
            @Schema(description = "Callback URL") String callbackUrl,
            @Schema(description = "Additional parameters") Map<String, Object> additionalParams
    ) {}
    
    /**
     * Batch forecast response
     */
    @Schema(description = "Batch forecast response")
    public record BatchForecastResponse(
            @Schema(description = "Batch ID") String batchId,
            @Schema(description = "Status") String status,
            @Schema(description = "Start time") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Schema(description = "Estimated completion time") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime estimatedCompletionTime,
            @Schema(description = "Total products") int totalProducts,
            @Schema(description = "Progress URL") String progressUrl
    ) {}
    
    /**
     * Common record definitions
     */
    
    /**
     * Forecast period
     */
    public record ForecastPeriod(
            @Schema(description = "Start date") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Schema(description = "End date") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Schema(description = "Period (days)") int days
    ) {}
    
    /**
     * Date range
     */
    public record DateRange(
            @Schema(description = "Start date") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Schema(description = "End date") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {}
    
    /**
     * Forecast statistics
     */
    public record ForecastStatistics(
            @Schema(description = "Mean value") double mean,
            @Schema(description = "Median value") double median,
            @Schema(description = "Standard deviation") double standardDeviation,
            @Schema(description = "Minimum value") double min,
            @Schema(description = "Maximum value") double max,
            @Schema(description = "Total sum") double total
    ) {}
    
    /**
     * Confidence interval
     */
    public record ConfidenceInterval(
            @Schema(description = "Lower bound") List<Double> lowerBound,
            @Schema(description = "Upper bound") List<Double> upperBound,
            @Schema(description = "Confidence level") double confidenceLevel
    ) {}
    
    /**
     * Forecast alert
     */
    public record ForecastAlert(
            @Schema(description = "Alert type") String alertType,
            @Schema(description = "Severity") String severity,
            @Schema(description = "Message") String message,
            @Schema(description = "Recommended action") String recommendedAction,
            @Schema(description = "Deadline") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate deadline
    ) {}
    
    /**
     * Forecast accuracy
     */
    public record ForecastAccuracy(
            @Schema(description = "Short-term accuracy (7 days)") double shortTermAccuracy,
            @Schema(description = "Medium-term accuracy (30 days)") double mediumTermAccuracy,
            @Schema(description = "Long-term accuracy (90 days)") double longTermAccuracy,
            @Schema(description = "Overall accuracy") double overallAccuracy
    ) {}
    
    /**
     * Forecast metadata
     */
    public record ForecastMetadata(
            @Schema(description = "Model version") String modelVersion,
            @Schema(description = "Algorithm") String algorithm,
            @Schema(description = "Processing time (ms)") long processingTimeMs,
            @Schema(description = "Data quality score") double dataQualityScore,
            @Schema(description = "Last updated") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastUpdated
    ) {}
}
