package com.skishop.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * Chat Message Request DTO
 * Immutable data class using Java 21's Record feature
 * 
 * @param userId User ID (required)
 * @param content Message content (required, max 1000 characters)
 * @param conversationId Conversation ID (optional)
 * @param sessionId Session ID (optional)
 * @param context Context information (optional)
 */
public record ChatMessageRequest(
    @NotBlank(message = "User ID is required")
    String userId,
    
    @NotBlank(message = "Message content is required")
    @Size(max = 1000, message = "Message content must not exceed 1000 characters")
    String content,
    
    String conversationId,
    String sessionId,
    Map<String, Object> context
) {
    
    /**
     * Factory method to create basic chat message
     */
    public static ChatMessageRequest of(String userId, String content) {
        return new ChatMessageRequest(userId, content, null, null, null);
    }
    
    /**
     * Factory method to create chat message with context
     */
    public static ChatMessageRequest withContext(String userId, String content, 
                                               Map<String, Object> context) {
        return new ChatMessageRequest(userId, content, null, null, context);
    }
    
    /**
     * Factory method to create chat message for session continuation
     */
    public static ChatMessageRequest forSession(String userId, String content, 
                                              String sessionId, String conversationId) {
        return new ChatMessageRequest(userId, content, conversationId, sessionId, null);
    }
}
