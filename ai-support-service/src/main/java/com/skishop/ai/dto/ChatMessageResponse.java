package com.skishop.ai.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Chat message response DTO
 * Immutable data class using Java 21's Record feature
 * 
 * @param messageId Message ID
 * @param conversationId Conversation ID
 * @param content Message content
 * @param intent Intent
 * @param confidence Confidence level
 * @param timestamp Timestamp
 * @param context Context information
 * @param requiresAction Action required flag
 * @param actionType Action type
 */
public record ChatMessageResponse(
    String messageId,
    String conversationId,
    String content,
    String intent,
    Double confidence,
    LocalDateTime timestamp,
    Map<String, Object> context,
    boolean requiresAction,
    String actionType
) {
    
    /**
     * Factory method to create a basic response
     */
    public static ChatMessageResponse of(String messageId, String conversationId, String content) {
        return new ChatMessageResponse(
            messageId,
            conversationId,
            content,
            null,
            null,
            LocalDateTime.now(),
            null,
            false,
            null
        );
    }
    
    /**
     * Factory method to create a response with confidence level
     */
    public static ChatMessageResponse withConfidence(String messageId, String conversationId, 
                                                   String content, String intent, Double confidence) {
        return new ChatMessageResponse(
            messageId,
            conversationId,
            content,
            intent,
            confidence,
            LocalDateTime.now(),
            null,
            false,
            null
        );
    }
    
    /**
     * Factory method to create a response with action request
     */
    public static ChatMessageResponse withAction(String messageId, String conversationId, 
                                               String content, String actionType) {
        return new ChatMessageResponse(
            messageId,
            conversationId,
            content,
            null,
            null,
            LocalDateTime.now(),
            null,
            true,
            actionType
        );
    }
}
