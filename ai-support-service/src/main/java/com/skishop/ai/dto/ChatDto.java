package com.skishop.ai.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Chat feature related DTO definitions
 * Immutable data classes using Java 21's Record feature
 */
public final class ChatDto {

    // Add private constructor to make it a utility class
    private ChatDto() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Chat request DTO
     * 
     * @param message Message content (required)
     * @param sessionId Session ID (optional)
     * @param conversationType Conversation type (SUPPORT, RECOMMENDATION, SEARCH)
     * @param context Context information
     */
    public record ChatRequest(
        @NotBlank(message = "Message is required")
        String message,
        String sessionId,
        String conversationType,
        Map<String, Object> context
    ) {
        
        public ChatRequest {
            if (context == null) {
                context = Map.of();
            }
        }
        
        /**
         * Create a basic chat request
         */
        public static ChatRequest of(String message) {
            return new ChatRequest(message, null, null, Map.of());
        }
        
        /**
         * Create chat request for session continuation
         */
        public static ChatRequest forSession(String message, String sessionId) {
            return new ChatRequest(message, sessionId, null, Map.of());
        }
        
        /**
         * Create request to start a specific type of conversation
         */
        public static ChatRequest forType(String message, ConversationType type) {
            return new ChatRequest(message, null, type.asString(), Map.of());
        }
        
        /**
         * Create chat request with context
         */
        public static ChatRequest withContext(String message, Map<String, Object> context) {
            return new ChatRequest(message, null, null, context);
        }
    }
    
    /**
     * Sealed interface representing conversation type
     */
    public sealed interface ConversationType 
        permits ConversationType.Support, ConversationType.Recommendation, ConversationType.Search {
        
        record Support() implements ConversationType {}
        record Recommendation() implements ConversationType {}
        record Search() implements ConversationType {}
        
        static ConversationType fromString(String typeString) {
            return switch (typeString.toUpperCase()) {
                case "SUPPORT" -> new Support();
                case "RECOMMENDATION" -> new Recommendation();
                case "SEARCH" -> new Search();
                default -> throw new IllegalArgumentException("Unknown conversation type: " + typeString);
            };
        }
        
        default String asString() {
            return switch (this) {
                case Support() -> "SUPPORT";
                case Recommendation() -> "RECOMMENDATION";
                case Search() -> "SEARCH";
            };
        }
    }

    /**
     * Chat response DTO
     * 
     * @param sessionId Session ID
     * @param response Response content
     * @param responseType Response type (TEXT, PRODUCT_LIST, RECOMMENDATION)
     * @param data Related data
     * @param metadata Metadata
     * @param timestamp Timestamp
     */
    public record ChatResponse(
        String sessionId,
        String response,
        String responseType,
        List<Object> data,
        Map<String, Object> metadata,
        LocalDateTime timestamp
    ) {
        
        public ChatResponse {
            if (data == null) {
                data = List.of();
            }
            if (metadata == null) {
                metadata = Map.of();
            }
            if (timestamp == null) {
                timestamp = LocalDateTime.now();
            }
        }
        
        /**
         * Create text response
         */
        public static ChatResponse textResponse(String sessionId, String response) {
            return new ChatResponse(sessionId, response, "TEXT", List.of(), Map.of(), LocalDateTime.now());
        }
        
        /**
         * Create response with product list
         */
        public static ChatResponse withProductList(String sessionId, String response, List<Object> products) {
            return new ChatResponse(sessionId, response, "PRODUCT_LIST", products, Map.of(), LocalDateTime.now());
        }
        
        /**
         * Create response with recommendation
         */
        public static ChatResponse withRecommendation(String sessionId, String response, 
                                                    List<Object> recommendations, Map<String, Object> metadata) {
            return new ChatResponse(sessionId, response, "RECOMMENDATION", recommendations, metadata, LocalDateTime.now());
        }
        
        /**
         * Check if data is included
         */
        public boolean hasData() {
            return !data.isEmpty();
        }
        
        /**
         * Get response type (sealed interface)
         */
        public ResponseType getResponseTypeEnum() {
            return ResponseType.fromString(responseType);
        }
    }
    
    /**
     * Sealed interface representing response type
     */
    public sealed interface ResponseType 
        permits ResponseType.Text, ResponseType.ProductList, ResponseType.Recommendation {
        
        record Text() implements ResponseType {}
        record ProductList() implements ResponseType {}
        record Recommendation() implements ResponseType {}
        
        static ResponseType fromString(String typeString) {
            return switch (typeString.toUpperCase()) {
                case "TEXT" -> new Text();
                case "PRODUCT_LIST" -> new ProductList();
                case "RECOMMENDATION" -> new Recommendation();
                default -> new Text(); // Default is text
            };
        }
        
        default String asString() {
            return switch (this) {
                case Text() -> "TEXT";
                case ProductList() -> "PRODUCT_LIST";
                case Recommendation() -> "RECOMMENDATION";
            };
        }
    }

    /**
     * Chat history response DTO
     * 
     * @param sessionId Session ID
     * @param messages Message list
     * @param status Status
     * @param startedAt Start timestamp
     * @param updatedAt Update timestamp
     */
    public record ChatHistoryResponse(
        String sessionId,
        List<ChatMessageDto> messages,
        String status,
        LocalDateTime startedAt,
        LocalDateTime updatedAt
    ) {
        
        public ChatHistoryResponse {
            if (messages == null) {
                messages = List.of();
            }
        }
        
        /**
         * Get message count
         */
        public int getMessageCount() {
            return messages.size();
        }
        
        /**
         * Get latest message
         */
        public ChatMessageDto getLatestMessage() {
            return messages.isEmpty() ? null : messages.get(messages.size() - 1);
        }
        
        /**
         * Check if session is active
         */
        public boolean isActive() {
            return "ACTIVE".equalsIgnoreCase(status);
        }
    }

    /**
     * Chat message DTO
     * 
     * @param messageId Message ID
     * @param role Role (USER, ASSISTANT, SYSTEM)
     * @param content Message content
     * @param metadata Metadata
     * @param timestamp Timestamp
     */
    public record ChatMessageDto(
        String messageId,
        String role,
        String content,
        Map<String, Object> metadata,
        LocalDateTime timestamp
    ) {
        
        public ChatMessageDto {
            if (metadata == null) {
                metadata = Map.of();
            }
            if (timestamp == null) {
                timestamp = LocalDateTime.now();
            }
        }
        
        /**
         * Create user message
         */
        public static ChatMessageDto userMessage(String messageId, String content) {
            return new ChatMessageDto(messageId, "USER", content, Map.of(), LocalDateTime.now());
        }
        
        /**
         * Create assistant message
         */
        public static ChatMessageDto assistantMessage(String messageId, String content) {
            return new ChatMessageDto(messageId, "ASSISTANT", content, Map.of(), LocalDateTime.now());
        }
        
        /**
         * Create system message
         */
        public static ChatMessageDto systemMessage(String messageId, String content) {
            return new ChatMessageDto(messageId, "SYSTEM", content, Map.of(), LocalDateTime.now());
        }
        
        /**
         * Check role
         */
        public boolean isUserMessage() {
            return "USER".equalsIgnoreCase(role);
        }
        
        public boolean isAssistantMessage() {
            return "ASSISTANT".equalsIgnoreCase(role);
        }
        
        public boolean isSystemMessage() {
            return "SYSTEM".equalsIgnoreCase(role);
        }
    }
}
