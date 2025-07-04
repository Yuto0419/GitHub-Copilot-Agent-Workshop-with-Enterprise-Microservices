package com.skishop.ai.entity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Chat Message Entity
 * 
 * <p>Immutable data class using Java 21's record feature</p>
 * <p>Manages message information with chatbot</p>
 * 
 * @param messageId Message ID
 * @param timestamp Timestamp
 * @param role Message role
 * @param content Message content
 * @param intent Intent
 * @param entities Entity information
 * @param confidence Confidence level
 * @param context Context information
 * 
 * @since 1.0.0
 */
public record ChatMessage(
    String messageId,
    LocalDateTime timestamp,
    MessageRole role,
    String content,
    String intent,
    Map<String, Object> entities,
    Double confidence,
    Map<String, Object> context
) {
    
    /**
     * Factory method for creating new messages
     */
    public static ChatMessage create(MessageRole role, String content) {
        return new ChatMessage(
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            role,
            content,
            null,
            Map.of(),
            null,
            Map.of()
        );
    }
    
    /**
     * Create message with intent
     */
    public static ChatMessage withIntent(MessageRole role, String content, String intent, Double confidence) {
        return new ChatMessage(
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            role,
            content,
            intent,
            Map.of(),
            confidence,
            Map.of()
        );
    }
    
    /**
     * Create message with entity information added
     */
    public ChatMessage withEntities(Map<String, Object> entities) {
        return new ChatMessage(
            messageId,
            timestamp,
            role,
            content,
            intent,
            entities,
            confidence,
            context
        );
    }
}