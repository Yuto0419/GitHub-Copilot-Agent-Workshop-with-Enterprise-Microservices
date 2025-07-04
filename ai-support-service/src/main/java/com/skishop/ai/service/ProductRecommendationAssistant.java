package com.skishop.ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Product Recommendation AI - Advanced Semantic Search and Recommendation Engine
 * 
 * <p>Provides personalized product recommendations using LangChain4j 1.1.0 + Azure OpenAI.</p>
 * 
 * <h3>Main Features:</h3>
 * <ul>
 *   <li>User profile-based recommendations</li>
 *   <li>Similar product search and recommendations</li>
 *   <li>Seasonal and scene-based recommendations</li>
 *   <li>Budget-optimized recommendations</li>
 * </ul>
 * 
 * <p>This interface utilizes machine learning and semantic analysis to provide
 * optimized product recommendations for individual users.</p>
 * 
 * @since 1.0.0
 * @see <a href="https://github.com/langchain4j/langchain4j-examples">LangChain4j Examples</a>
 */
public interface ProductRecommendationAssistant {
    
    /**
     * Personalized product recommendations
     * 
     * @param userQuery User's query and requirements
     * @param userProfile User profile
     * @param productCatalog Product catalog information
     * @return JSON format recommendation results
     */
    @SystemMessage("""
        You are a state-of-the-art ski equipment recommendation engine.
        Use machine learning and semantic analysis to recommend optimal products for users.
        
        【Analysis Target Data】
        - User Profile: Skiing level, experience years, preferences
        - Physical Attributes: Height, weight, foot size, body type
        - Budget & Price Sensitivity: Budget range, price priority
        - Usage Conditions: Frequency, scenes, region, season
        - Preference Information: Brand preferences, design trends
        - Behavioral History: Past purchases, searches, browsing patterns
        
        【Recommendation Algorithm】
        1. Content-based filtering
        2. Collaborative filtering
        3. Hybrid recommendations
        4. Seasonal adjustments
        5. Inventory & price optimization
        
        【Output Format】
        Please respond with the following JSON structure:
        ```json
        {
          "recommendations": [
            {
              "productId": "Product ID",
              "productName": "Product Name",
              "brand": "Brand Name",
              "score": 0.95,
              "confidenceLevel": "high|medium|low",
              "reasons": [
                "Optimal for user's skiing level",
                "Highest quality within budget range",
                "Latest model from preferred brand"
              ],
              "category": "Category",
              "price": "Price",
              "suitabilityScore": {
                "level": 0.9,
                "bodyType": 0.8,
                "purpose": 0.95,
                "budget": 0.85
              }
            }
          ],
          "totalRecommendations": 5,
          "searchStrategy": "Recommendation strategy used",
          "explanation": "Overall recommendation explanation and rationale"
        }
        ```
        
        【Important Notes】
        - Prioritize safety considerations
        - Use only accurate product information
        - Respect user's budget
        - Avoid excessive recommendations
        """)
    String generateRecommendations(
        @UserMessage @V("userQuery") String userQuery,
        @V("userProfile") String userProfile,
        @V("productCatalog") String productCatalog
    );
    
    /**
     * Similar product search and recommendation
     * 
     * @param targetProduct Reference product
     * @param productCatalog Product catalog
     * @return Similar product list in JSON format
     */
    @SystemMessage("""
        Please analyze the features of the specified product in detail and discover and recommend products with high similarity.
        
        【Similarity Criteria】
        - Product category (skis, boots, wear, etc.)
        - Price range and grade level
        - Brand positioning
        - Technical specifications and functional characteristics
        - Target skill level and technical standards
        - Usage purpose and scene suitability
        - Design and style trends
        - Materials and manufacturing technology
        
        【Analysis Methods】
        1. Feature vector comparison
        2. Semantic similarity analysis
        3. User review and rating pattern analysis
        4. Purchase behavior pattern analysis
        5. Correlation analysis with expert evaluations
        
        【Output Format】
        ```json
        {
          "targetProduct": {
            "id": "reference product ID",
            "name": "reference product name",
            "analysis": "product feature analysis"
          },
          "similarProducts": [
            {
              "productId": "similar product ID",
              "productName": "similar product name",
              "similarityScore": 0.88,
              "similarityReasons": [
                "Same category and price range",
                "Similar technical specifications",
                "Equivalent target level"
              ],
              "differences": [
                "Different brand",
                "Color variations"
              ],
              "recommendationReason": "Why it's suitable as an alternative"
            }
          ],
          "totalSimilarProducts": 8,
          "analysisMethod": "Analysis method used"
        }
        ```
        
        Focus on accuracy and practicality, providing information that is useful for actual purchase decisions.
        """)
    String findSimilarProducts(
        @UserMessage @V("targetProduct") String targetProduct,
        @V("productCatalog") String productCatalog
    );
    
    /**
     * Seasonal and scene-based product recommendations
     * 
     * @param season Season information
     * @param scene Usage scene
     * @param userProfile User profile
     * @return Season and scene optimized recommendations
     */
    @SystemMessage("""
        Please provide product recommendations specialized for seasons and scenes.
        
        【Seasonal Consideration Factors】
        - Early season, High season, Spring season
        - Snow quality and climate conditions
        - Slope conditions
        - Temperature and weather patterns
        
        【Scene Consideration Factors】
        - On-piste skiing, Backcountry, Competition
        - Day trip, Overnight stay, Extended stay
        - Family, Friends, Solo
        - First-time, Repeat, Special events
        
        Please provide practical recommendations optimized for seasons and scenes.
        """)
    String recommendBySeasonAndScene(
        @UserMessage @V("season") String season,
        @V("scene") String scene,
        @V("userProfile") String userProfile
    );
}
