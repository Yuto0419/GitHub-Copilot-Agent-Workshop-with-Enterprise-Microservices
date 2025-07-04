package com.skishop.ai.controller;

import com.skishop.ai.service.SearchEnhancementAssistant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Search Enhancement API Controller
 * 
 * <p>Semantic search functionality using LangChain4j 1.1.0 + Azure OpenAI</p>
 * 
 * <h3>Provided Features:</h3>
 * <ul>
 *   <li>Semantic Search - Search by semantic similarity</li>
 *   <li>Autocomplete - Automatic completion of search suggestions</li>
 *   <li>Search Enhancement - Automatic query improvement</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search API", description = "AI Search Enhancement Related API")
public class SearchController {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    
    private final SearchEnhancementAssistant searchAssistant;
    
    /**
     * Constructor
     * 
     * @param searchAssistant Search enhancement assistant
     */
    public SearchController(SearchEnhancementAssistant searchAssistant) {
        this.searchAssistant = searchAssistant;
    }
    
    // Java 21 Text Blocks for mock data
    private static final String MOCK_PRODUCT_CATALOG = """
            [
              {
                "productId": "ski-001",
                "name": "Rossignol Experience 88 Ti",
                "category": "All-Mountain Ski",
                "brand": "Rossignol",
                "price": 89000,
                "description": "Versatile ski for intermediate to advanced skiers"
              },
              {
                "productId": "ski-002", 
                "name": "Salomon XDR 88 Ti",
                "category": "All-Mountain Ski",
                "brand": "Salomon",
                "price": 95000,
                "description": "Excellent edge grip and stability"
              },
              {
                "productId": "boots-001",
                "name": "Lange RX 120",
                "category": "Ski Boots",
                "brand": "Lange",
                "price": 78000,
                "description": "Precision fit racing boots"
              }
            ]
            """;
    
    private static final String MOCK_POPULAR_QUERIES = """
            [
              {"query": "beginner ski", "popularity": 0.9},
              {"query": "ski boots", "popularity": 0.8},
              {"query": "ski wear", "popularity": 0.7},
              {"query": "goggles", "popularity": 0.6},
              {"query": "ski gloves", "popularity": 0.5}
            ]
            """;
    
    /**
     * Semantic search
     */
    @PostMapping("/semantic")
    @Operation(summary = "Semantic Search", description = "AI-powered semantic search")
    public ResponseEntity<String> performSemanticSearch(
            @RequestParam String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String userId) {
        
        logger.info("Performing semantic search for query: {}", query);
        
        try {
            // Build user context
            var userContext = buildUserContext(userId, category);
            
            // Execute AI search
            var searchResults = searchAssistant.performSemanticSearch(
                query, MOCK_PRODUCT_CATALOG, userContext
            );
            
            return ResponseEntity.ok(searchResults);
            
        } catch (Exception e) {
            logger.error("Error performing semantic search: ", e);
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"An error occurred during search\"}");
        }
    }
    
    /**
     * Autocomplete
     */
    @GetMapping("/autocomplete")
    @Operation(summary = "Search Autocomplete", description = "Automatic completion of search queries")
    public ResponseEntity<String> getAutocompleteSuggestions(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {
        
        logger.info("Getting autocomplete suggestions for: {}", q);
        
        try {
            // Generate autocomplete suggestions
            var suggestions = searchAssistant.generateAutocompleteSuggestions(
                q, MOCK_POPULAR_QUERIES
            );
            
            return ResponseEntity.ok(suggestions);
            
        } catch (Exception e) {
            logger.error("Error generating autocomplete suggestions: ", e);
            return ResponseEntity.internalServerError()
                    .body("{\"suggestions\": []}");
        }
    }
    
    /**
     * Search suggestions
     */
    @GetMapping("/suggest")
    @Operation(summary = "Search Suggestions", description = "Propose search candidates")
    public ResponseEntity<String> getSearchSuggestions(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "5") int limit) {
        
        logger.info("Getting search suggestions for category: {}", category);
        
        // Return popular search queries by category
        String suggestions = """
            {
              "suggestions": [
                "Beginner ski boards",
                "Recommended carving skis",
                "Ski boot sizes",
                "Waterproof ski wear",
                "Anti-fog ski goggles"
              ]
            }
            """;
        
        return ResponseEntity.ok(suggestions);
    }
    
    /**
     * Image search (future implementation)
     */
    @PostMapping("/visual")
    @Operation(summary = "Image Search", description = "Product search using images")
    public ResponseEntity<String> performVisualSearch() {
        
        logger.info("Visual search requested");
        
        return ResponseEntity.ok("{\"message\": \"Image search functionality will be implemented in the future\"}");
    }
    
    /**
     * User context construction
     * Using Java 21's String formatted() method
     */
    private String buildUserContext(String userId, String category) {
        return """
            {
              "userId": "%s",
              "preferredCategory": "%s",
              "searchHistory": ["skis", "boots", "wear"],
              "skillLevel": "intermediate",
              "budget": {"min": 30000, "max": 100000}
            }
            """.formatted(
                userId != null ? userId : "anonymous",
                category != null ? category : "all"
            );
    }
}
