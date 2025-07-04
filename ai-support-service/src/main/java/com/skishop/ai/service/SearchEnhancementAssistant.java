package com.skishop.ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Search Enhancement AI - Semantic Search and Autocomplete
 * 
 * <p>Provides advanced search functionality using LangChain4j 1.1.0 + Azure OpenAI.</p>
 * 
 * <h3>Main Features:</h3>
 * <ul>
 *   <li>Semantic Search - Search for semantically related products</li>
 *   <li>Search query expansion and improvement</li>
 *   <li>Search result ranking optimization</li>
 *   <li>Search intent understanding and analysis</li>
 *   <li>Autocomplete functionality</li>
 * </ul>
 * 
 * @since 1.0.0
 * @see CustomerSupportAssistant
 * @see ProductRecommendationAssistant
 */
public interface SearchEnhancementAssistant {
    
    /**
     * Perform semantic search
     * 
     * @param query Search query
     * @param productCatalog Product catalog information
     * @param userContext User context
     * @return Search results in JSON format
     */
    @SystemMessage("""
        You are an advanced search system.
        Analyze user search queries and provide optimal search results.
        
        Features:
        1. Semantic search - Find semantically related products
        2. Search query expansion and improvement
        3. Search result ranking optimization
        4. Search intent understanding and analysis
        
        Return search results in JSON format with the following structure:
        {
          "results": [
            {
              "productId": "product ID",
              "title": "product name",
              "relevanceScore": 0.95,
              "matchType": "semantic|exact|partial",
              "highlights": ["parts that match the search"]
            }
          ],
          "suggestedQueries": ["related search queries"],
          "filters": {"category": "category", "priceRange": {"min": 0, "max": 100000}}
        }
        """)
    String performSemanticSearch(
        @UserMessage @V("query") String query,
        @V("productCatalog") String productCatalog,
        @V("userContext") String userContext
    );
    
    /**
     * Generate autocomplete suggestions
     * 
     * @param partialQuery Partial search query
     * @param popularQueries Popular search queries
     * @return Completion suggestions in JSON format
     */
    @SystemMessage("""
        Please provide search autocomplete functionality.
        Generate appropriate completion suggestions for user's partial input query.
        
        Completion criteria:
        - Popular search queries
        - Seasonality
        - User history
        - Product categories
        
        Return in JSON format with the following structure:
        {
          "suggestions": [
            {
              "completion": "completed query",
              "type": "product|category|brand",
              "popularity": 0.8
            }
          ]
        }
        """)
    String generateAutocompleteSuggestions(
        @UserMessage @V("partialQuery") String partialQuery,
        @V("popularQueries") String popularQueries
    );
}
