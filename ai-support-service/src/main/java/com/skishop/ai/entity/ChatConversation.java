package com.skishop.ai.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Chat conversation entity
 * 
 * <p>Immutable data class using Java 21's record feature</p>
 * <p>Manages conversation information with chatbot</p>
 * 
 * @param conversationId Conversation ID
 * @param userId User ID
 * @param sessionId Session ID
 * @param messages Message list
 * @param status Conversation status
 * @param satisfaction Satisfaction level
 * @param createdAt Creation timestamp
 * @param updatedAt Update timestamp
 * @param metadata Metadata
 * 
 * @since 1.0.0
 */
@Document(collection = "chat_conversations")
public record ChatConversation(
    @Id String conversationId,
    String userId,
    String sessionId,
    List<ChatMessage> messages,
    ConversationStatus status,
    Double satisfaction,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Map<String, Object> metadata
) {
    
    /**
     * Factory method for creating new conversation
     * 
     * @param userId User ID
     * @param sessionId Session ID
     * @return New ChatConversation instance
     */
    public static ChatConversation create(String userId, String sessionId) {
        var now = LocalDateTime.now();
        return new ChatConversation(
            UUID.randomUUID().toString(),
            userId,
            sessionId,
            List.of(),
            new ConversationStatus.Active(),
            null,
            now,
            now,
            Map.of()
        );
    }
    
    /**
     * Create a new conversation with added message
     * 
     * @param message Message to add
     * @return New ChatConversation instance with added message
     * @throws IllegalStateException When message cannot be added in current status
     */
    public ChatConversation addMessage(ChatMessage message) {
        if (!status.canAddMessage()) {
            throw new IllegalStateException("""
                Cannot add message in current status "%s".
                """.formatted(status.asString()));
        }
        
        var newMessages = new java.util.ArrayList<>(messages);
        newMessages.add(message);
            
        return new ChatConversation(
            conversationId,
            userId,
            sessionId,
            List.copyOf(newMessages),
            status,
            satisfaction,
            createdAt,
            LocalDateTime.now(),
            metadata
        );
    }
    
    /**
     * Create a new conversation with updated status
     * 
     * @param newStatus New status
     * @return New ChatConversation instance with updated status
     */
    public ChatConversation updateStatus(ConversationStatus newStatus) {
        return new ChatConversation(
            conversationId,
            userId,
            sessionId,
            messages,
            newStatus,
            satisfaction,
            createdAt,
            LocalDateTime.now(),
            metadata
        );
    }
    
    /**
     * Create a new conversation with satisfaction rating
     * 
     * @param satisfaction Satisfaction level (0.0 - 1.0)
     * @return New ChatConversation instance with set satisfaction level
     */
    public ChatConversation setSatisfaction(Double satisfaction) {
        return new ChatConversation(
            conversationId,
            userId,
            sessionId,
            messages,
            status,
            satisfaction,
            createdAt,
            LocalDateTime.now(),
            metadata
        );
    }
}
