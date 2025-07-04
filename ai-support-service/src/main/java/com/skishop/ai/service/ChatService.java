package com.skishop.ai.service;

import com.skishop.ai.dto.ChatMessageRequest;
import com.skishop.ai.dto.ChatMessageResponse;
import com.skishop.ai.exception.AiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Chat Service Implementation
 * 
 * <p>Chatbot functionality using LangChain4j 1.1.0 AI services</p>
 * <p>Integrated error handling, retry, and fallback functionality</p>
 * <p>Leverages the latest Java 21 features</p>
 * 
 * @since 1.0.0
 */
@Service
public class ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    
    private final CustomerSupportAssistant customerSupportAssistant;
    private final ProductRecommendationAssistant recommendationAssistant;
    private final EnhancedAiServiceExecutor aiServiceExecutor;
    // private final ChatConversationRepository conversationRepository;
    
    /**
     * Constructor
     * 
     * @param customerSupportAssistant Customer support assistant
     * @param recommendationAssistant Product recommendation assistant
     * @param aiServiceExecutor AI service execution engine
     */
    public ChatService(
            CustomerSupportAssistant customerSupportAssistant,
            ProductRecommendationAssistant recommendationAssistant,
            EnhancedAiServiceExecutor aiServiceExecutor) {
        this.customerSupportAssistant = customerSupportAssistant;
        this.recommendationAssistant = recommendationAssistant;
        this.aiServiceExecutor = aiServiceExecutor;
    }
    
    /**
     * Mock product catalog using Java 21 Text Blocks feature (defined as constant)
     */
    private static final String MOCK_PRODUCT_CATALOG = """
            [
              {
                "productId": "ski-001",
                "name": "Rossignol Experience 88 Ti",
                "category": "All-Mountain Ski",
                "price": 89000,
                "skillLevel": "Intermediate-Advanced",
                "features": ["Titanium reinforced", "Stability", "Carving performance"]
              },
              {
                "productId": "ski-002", 
                "name": "Salomon QST 92",
                "category": "Freeride Ski",
                "price": 76000,
                "skillLevel": "Intermediate",
                "features": ["Lightweight", "Powder compatible", "All-round"]
              }
            ]
            """;
    
    /**
     * Intent classification using Java 21 sealed interfaces
     */
    public sealed interface Intent 
        permits Intent.ProductRecommendation, Intent.TechnicalAdvice, 
                Intent.OrderSupport, Intent.GeneralInquiry {
        
        record ProductRecommendation() implements Intent {}
        record TechnicalAdvice() implements Intent {}
        record OrderSupport() implements Intent {}
        record GeneralInquiry() implements Intent {}
        
        /**
         * Detect intent from string
         */
        static Intent detectFromMessage(String message) {
            var lowerMessage = message.toLowerCase();
            
            if (lowerMessage.contains("recommend") || lowerMessage.contains("suggestion") || 
                lowerMessage.contains("choose") || lowerMessage.contains("which")) {
                return new ProductRecommendation();
            } else if (lowerMessage.contains("technique") || lowerMessage.contains("skill") || 
                      lowerMessage.contains("tips") || lowerMessage.contains("improvement")) {
                return new TechnicalAdvice();
            } else if (lowerMessage.contains("order") || lowerMessage.contains("shipping") || 
                      lowerMessage.contains("return") || lowerMessage.contains("exchange")) {
                return new OrderSupport();
            } else {
                return new GeneralInquiry();
            }
        }
        
        /**
         * Get intent as string
         */
        default String asString() {
            return switch (this) {
                case ProductRecommendation() -> "PRODUCT_RECOMMENDATION";
                case TechnicalAdvice() -> "TECHNICAL_ADVICE";
                case OrderSupport() -> "ORDER_SUPPORT";
                case GeneralInquiry() -> "GENERAL_INQUIRY";
            };
        }
        
        /**
         * Determine if action is required
         */
        default boolean requiresAction() {
            return switch (this) {
                case ProductRecommendation(), OrderSupport() -> true;
                case TechnicalAdvice(), GeneralInquiry() -> false;
            };
        }
        
        /**
         * Get action type
         */
        default String getActionType() {
            return switch (this) {
                case ProductRecommendation() -> "SHOW_PRODUCTS";
                case OrderSupport() -> "REDIRECT_TO_SUPPORT";
                case TechnicalAdvice(), GeneralInquiry() -> "NONE";
            };
        }
    }
    
    /**
     * Chat message processing
     */
    public ChatMessageResponse processMessage(ChatMessageRequest request) {
        logger.info("Processing chat message for user: {}", request.userId());
        
        try {
            // Leverage Java 21's var type inference and pattern matching
            var userMessage = request.content();
            var intent = Intent.detectFromMessage(userMessage);
            
            // Call appropriate AI service based on intent (with error handling and retry)
            var aiResponse = generateResponseWithRetry(userMessage, intent, request);
            
            // Build and return response - using Record's factory method
            return ChatMessageResponse.withConfidence(
                UUID.randomUUID().toString(),
                request.conversationId(),
                aiResponse,
                intent.asString(),
                0.9 // Actually based on analysis results
            );
            
        } catch (AiServiceException e) {
            // AI service specific errors are re-thrown as-is
            logger.error("AI service error processing chat message: {}", e.getMessage());
            throw e;
            
        } catch (Exception e) {
            logger.error("Unexpected error processing chat message: ", e);
            throw AiServiceException.internalError("An error occurred while processing the chat message", e);
        }
    }
    
    /**
     * Generate AI response based on intent (leveraging Java 21 Switch Expressions)
     */
    private String generateResponseWithRetry(String userMessage, Intent intent, ChatMessageRequest request) {
        return switch (intent) {
            case Intent.ProductRecommendation() -> 
                generateProductRecommendationWithRetry(userMessage, request);
                
            case Intent.TechnicalAdvice() -> 
                aiServiceExecutor.executeWithRetry(
                    (text, context) -> customerSupportAssistant.provideTechnicalAdvice(text),
                    userMessage,
                    intent.asString()
                );
                
            case Intent.OrderSupport(), Intent.GeneralInquiry() -> 
                aiServiceExecutor.executeWithRetry(
                    (text, context) -> customerSupportAssistant.chat(text),
                    userMessage,
                    intent.asString()
                );
        };
    }
    
    /**
     * Generate product recommendations (with error handling and retry)
     */
    private String generateProductRecommendationWithRetry(String userMessage, ChatMessageRequest request) {
        // Build user profile
        var userProfile = buildUserProfile(request);
        
        // Generate recommendations using AI (with error handling and retry)
        return aiServiceExecutor.executeWithRetry(
            (text, context) -> recommendationAssistant.generateRecommendations(text, userProfile, MOCK_PRODUCT_CATALOG),
            userMessage,
            "PRODUCT_RECOMMENDATION"
        );
    }
    
    /**
     * Build user profile (Java 21 Text Blocks with String formatting)
     */
    private String buildUserProfile(ChatMessageRequest request) {
        // In actual implementation, retrieve from user management service
        return """
            {
              "userId": "%s",
              "skillLevel": "Intermediate",
              "preferences": {
                "budget": "50000-100000",
                "brands": ["Rossignol", "Salomon"],
                "usage": "Leisure"
              },
              "physicalAttributes": {
                "height": "170cm",
                "weight": "65kg"
              }
            }
            """.formatted(request.userId());
    }

}
