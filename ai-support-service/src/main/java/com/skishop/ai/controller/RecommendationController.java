package com.skishop.ai.controller;

import com.skishop.ai.service.ProductRecommendationAssistant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Product Recommendation API Controller
 * 
 * <p>Product recommendation functionality using LangChain4j 1.1.0 + Azure OpenAI</p>
 * <p>Leverages the latest Java 21 features</p>
 * 
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/recommendations")
@Tag(name = "Recommendation API", description = "AI Product Recommendation API")
public class RecommendationController {
    
    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);
    
    private final ProductRecommendationAssistant recommendationAssistant;
    
    // Mock product catalog for testing
    private static final String MOCK_PRODUCT_CATALOG;
    
    static {
        // Initialize the mock product catalog
        MOCK_PRODUCT_CATALOG = """
            [
              {
                "productId": "ski-001",
                "name": "Rossignol Experience 88 Ti",
                "category": "All-Mountain Ski",
                "price": 89000,
                "skillLevel": "Intermediate-Advanced",
                "features": ["Titanium reinforced", "Stability", "Carving performance"],
                "specifications": {
                  "length": ["160cm", "170cm", "180cm"],
                  "width": "88mm",
                  "technology": ["Titanium reinforced", "Wood core", "Auto turn"]
                }
              },
              {
                "productId": "ski-002", 
                "name": "Salomon QST 92",
                "category": "Freeride Ski",
                "price": 76000,
                "skillLevel": "Intermediate",
                "features": ["Lightweight", "Powder compatible", "All-round"],
                "specifications": {
                  "length": ["165cm", "175cm", "185cm"],
                  "width": "92mm",
                  "technology": ["Carbon", "Lightweight core"]
                }
              },
              {
                "productId": "boot-001",
                "name": "Lange RX 130",
                "category": "Ski Boots",
                "price": 98000,
                "skillLevel": "Advanced",
                "features": ["High rigidity", "Precision fit", "Competition ready"],
                "specifications": {
                  "flex": "130",
                  "lastWidth": "98mm",
                  "features": ["Heat molding", "Racing specifications"]
                }
              }
            ]
            """;
    }
    
    /**
     * Constructor
     * 
     * @param recommendationAssistant Product recommendation assistant
     */
    public RecommendationController(ProductRecommendationAssistant recommendationAssistant) {
        this.recommendationAssistant = recommendationAssistant;
    }
    
    /**
     * Request parameters using Java 21 Record
     */
    public record RecommendationParams(
        String userId,
        String productId,
        String category,
        int limit,
        String userContext
    ) {
        // Validation with compact constructor
        public RecommendationParams {
            if (limit <= 0) limit = 10;
            if (limit > 50) limit = 50;
        }
        
        public static RecommendationParams forPersonalized(String userId, int limit, String category) {
            return new RecommendationParams(userId, null, category, limit, null);
        }
        
        public static RecommendationParams forSimilar(String productId, int limit) {
            return new RecommendationParams(null, productId, null, limit, null);
        }
        
        public static RecommendationParams forTrending(int limit, String category) {
            return new RecommendationParams(null, null, category, limit, null);
        }
    }
    
    /**
     * Personalized product recommendations for users
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Personalized product recommendations", description = "Product recommendations for specified user")
    public ResponseEntity<String> getPersonalizedRecommendations(
            @PathVariable String userId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String category) {
        
        var params = RecommendationParams.forPersonalized(userId, limit, category);
        logger.info("Getting personalized recommendations for user: {} (limit: {}, category: {})", 
                 userId, params.limit(), params.category());
        
        try {
            // TODO: In actual implementation, retrieve user profile from user management service
            var userProfile = buildUserProfile(userId);
            var productCatalog = MOCK_PRODUCT_CATALOG;
            var userQuery = buildPersonalizedQuery(params.category(), params.limit());
            
            var recommendations = recommendationAssistant.generateRecommendations(
                userQuery, userProfile, productCatalog
            );
            
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            logger.error("Error generating personalized recommendations: ", e);
            return createErrorResponse("Error occurred during recommendation generation");
        }
    }
    
    /**
     * Similar product recommendations
     */
    @GetMapping("/similar/{productId}")
    @Operation(summary = "Similar product recommendations", description = "Recommendations for products similar to specified product")
    public ResponseEntity<String> getSimilarProducts(
            @PathVariable String productId,
            @RequestParam(defaultValue = "5") int limit) {
        
        var params = RecommendationParams.forSimilar(productId, limit);
        logger.info("Getting similar products for product: {} (limit: {})", productId, params.limit());
        
        try {
            var targetProduct = getProductInfo(productId);
            var productCatalog = mockProductCatalog;
            
            var similarProducts = recommendationAssistant.findSimilarProducts(
                targetProduct, productCatalog
            );
            
            return ResponseEntity.ok(similarProducts);
            
        } catch (Exception e) {
            logger.error("Error finding similar products: ", e);
            return createErrorResponse("Error occurred during similar product search");
        }
    }
    
    /**
     * Trending product recommendations
     */
    @GetMapping("/trending")
    @Operation(summary = "Trending product recommendations", description = "Current trending product recommendations")
    public ResponseEntity<String> getTrendingProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String category) {
        
        var params = RecommendationParams.forTrending(limit, category);
        logger.info("Getting trending products (limit: {}, category: {})", params.limit(), params.category());
        
        // Mock data utilizing Java 21's Text Blocks
        var trendingProducts = """
            {
              "recommendations": [
                {
                  "productId": "ski-001",
                  "productName": "Rossignol Experience 88 Ti",
                  "score": 0.95,
                  "reasons": ["Popular this season", "High rating", "Featured model"],
                  "category": "All-Mountain Ski",
                  "trendScore": 0.9
                },
                {
                  "productId": "boot-001", 
                  "productName": "Lange RX 130",
                  "score": 0.88,
                  "reasons": ["Professional model", "High performance", "Rising popularity"],
                  "category": "Ski Boots",
                  "trendScore": 0.85
                }
              ],
              "explanation": "Trending products based on current season and user interests"
            }
            """;
        
        return ResponseEntity.ok(trendingProducts);
    }
    
    /**
     * Category-based product recommendations
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Category-based product recommendations", description = "Product recommendations from specified category")
    public ResponseEntity<String> getCategoryRecommendations(
            @PathVariable String category,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String userContext) {
        
        logger.info("Getting category-based recommendations for {} (limit: {}, context: {})", 
                 category, limit, userContext);
        
        try {
            var productCatalog = mockProductCatalog;
            var query = String.format("Please recommend %d products suitable for %s from the %s category", 
                                     limit,
                                     userContext != null ? userContext : "general use", 
                                     category);
            
            var recommendations = recommendationAssistant.generateRecommendations(
                query, null, productCatalog
            );
            
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            logger.error("Error generating category recommendations: ", e);
            return createErrorResponse("Error occurred during category recommendation generation");
        }
    }
    
    /**
     * Record recommendation feedback
     */
    @PostMapping("/feedback")
    @Operation(summary = "Record recommendation feedback", description = "Record user feedback on recommendation results")
    public ResponseEntity<Void> submitRecommendationFeedback(
            @RequestParam String userId,
            @RequestParam String productId,
            @RequestParam String action,
            @RequestParam(required = false) Double rating) {
        
        logger.info("Received recommendation feedback - User: {}, Product: {}, Action: {}", 
                 userId, productId, action);
        
        // TODO: In actual implementation, save feedback to DB and use for recommendation algorithm improvement
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * Explain recommendation reasons
     */
    @GetMapping("/explain/{userId}/{productId}")
    @Operation(summary = "Explain recommendation reasons", description = "Explanation of why the product was recommended")
    public ResponseEntity<String> explainRecommendation(
            @PathVariable String userId,
            @PathVariable String productId) {
        
        logger.info("Explaining recommendation for user: {} and product: {}", userId, productId);
        
        // Utilizing Java 21's Text Blocks
        var explanation = """
            {
              "explanation": "Reasons why this product was recommended to you",
              "factors": [
                {
                  "factor": "Purchase history",
                  "description": "You have previously purchased products in similar categories",
                  "weight": 0.3
                },
                {
                  "factor": "Skill level match",
                  "description": "This product is suitable for your skill level",
                  "weight": 0.4
                },
                {
                  "factor": "Price range compatibility",
                  "description": "This product is within your budget range",
                  "weight": 0.3
                }
              ],
              "confidence": 0.85
            }
            """;
        
        return ResponseEntity.ok(explanation);
    }
    
    /**
     * Build user profile (Java 21 Text Blocks with formatted)
     */
    private String buildUserProfile(String userId) {
        // In actual implementation, retrieve from user management service
        return """
            {
              "userId": "%s",
              "skillLevel": "Intermediate",
              "preferences": {
                "budget": {"min": 50000, "max": 150000},
                "brands": ["Rossignol", "Salomon", "Atomic"],
                "usage": "Leisure",
                "categories": ["Skis", "Boots"]
              },
              "physicalAttributes": {
                "height": "170cm",
                "weight": "65kg",
                "footSize": "26.5cm"
              },
              "purchaseHistory": [
                {"productId": "ski-old-001", "category": "Skis", "purchaseDate": "2023-01-15"},
                {"productId": "boot-old-001", "category": "Boots", "purchaseDate": "2023-01-15"}
              ],
              "searchHistory": ["Intermediate skis", "Carving skis", "Ski boots"],
              "seasonalPreferences": {
                "season": "winter",
                "preferredSlopes": ["Resort", "On-piste"]
              }
            }
            """.formatted(userId);
    }
    
    /**
     * Build personalized query
     */
    private String buildPersonalizedQuery(String category, int limit) {
        var baseQuery = new StringBuilder("Please recommend products that are optimal for me");
        if (category != null) {
            baseQuery.append(". Category: ").append(category);
        }
        baseQuery.append(". Maximum ").append(limit).append(" items.");
        return baseQuery.toString();
    }
    
    /**
     * Get product information
     */
    private String getProductInfo(String productId) {
        // In actual implementation, retrieve from product management service
        return """
            {
              "productId": "%s",
              "name": "Sample Product",
              "category": "Skis",
              "features": ["High performance", "Lightweight", "Durability"],
              "price": 89000,
              "specifications": {
                "length": "170cm",
                "width": "88mm",
                "technology": ["Titanium reinforced", "Wood core"]
              }
            }
            """.formatted(productId);
    }
    
    /**
     * Get mock product catalog (utilizing Java 21 Text Blocks)
     */
    private String mockProductCatalog =
         """
            [
              {
                "productId": "ski-001",
                "name": "Rossignol Experience 88 Ti",
                "category": "All-Mountain Ski",
                "price": 89000,
                "skillLevel": "Intermediate-Advanced",
                "features": ["Titanium reinforced", "Stability", "Carving performance"],
                "specifications": {
                  "length": ["160cm", "170cm", "180cm"],
                  "width": "88mm",
                  "technology": ["Titanium reinforced", "Wood core", "Auto turn"]
                }
              },
              {
                "productId": "ski-002", 
                "name": "Salomon QST 92",
                "category": "Freeride Ski",
                "price": 76000,
                "skillLevel": "Intermediate",
                "features": ["Lightweight", "Powder compatible", "All-round"],
                "specifications": {
                  "length": ["165cm", "175cm", "185cm"],
                  "width": "92mm",
                  "technology": ["Carbon", "Lightweight core"]
                }
              },
              {
                "productId": "boot-001",
                "name": "Lange RX 130",
                "category": "Ski Boots",
                "price": 98000,
                "skillLevel": "Advanced",
                "features": ["High rigidity", "Precision fit", "Competition ready"],
                "specifications": {
                  "flex": "130",
                  "lastWidth": "98mm",
                  "features": ["Heat molding", "Racing specifications"]
                }
              }
            ]
            """;
    
    /**
     * Create error response (utilizing Java 21 Text Blocks)
     */
    private ResponseEntity<String> createErrorResponse(String message) {
        var errorResponse = """
            {
              "error": "%s",
              "timestamp": "%s",
              "status": 500
            }
            """.formatted(message, java.time.LocalDateTime.now());
        
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
