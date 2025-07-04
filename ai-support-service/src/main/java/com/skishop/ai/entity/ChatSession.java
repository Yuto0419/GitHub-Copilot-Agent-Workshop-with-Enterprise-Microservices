package com.skishop.ai.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Chat session entity
 * 
 * <p>Immutable data class using Java 21's record feature</p>
 * <p>Manages session information with chatbot</p>
 * 
 * @param sessionId Session ID (MongoDB _id)
 * @param userId User ID
 * @param conversationId Conversation ID
 * @param messages Chat message list
 * @param sessionType Session type
 * @param status Session status
 * @param context Session context
 * @param startedAt Start timestamp
 * @param endedAt End timestamp
 * @param updatedAt Update timestamp
 * 
 * @since 1.0.0
 */
@Document(collection = "chat_sessions")
public record ChatSession(
    @Id
    String sessionId,
    String userId,
    String conversationId,
    List<ChatMessage> messages,
    SessionType sessionType,
    SessionStatus status,
    Map<String, Object> context,
    LocalDateTime startedAt,
    LocalDateTime endedAt,
    LocalDateTime updatedAt
) {
    
    /**
     * Factory method for creating new session
     */
    public static ChatSession startNew(String userId, String conversationId, SessionType type) {
        var now = LocalDateTime.now();
        return new ChatSession(
            null, // MongoDB generates ID
            userId,
            conversationId,
            List.of(),
            type,
            new SessionStatus.Active(now),
            Map.of(),
            now,
            null,
            now
        );
    }
    
    /**
     * Add message
     */
    public ChatSession withMessage(ChatMessage message) {
        var updatedMessages = new java.util.ArrayList<>(messages);
        updatedMessages.add(message);
        
        return new ChatSession(
            sessionId,
            userId,
            conversationId,
            List.copyOf(updatedMessages),
            sessionType,
            new SessionStatus.Active(LocalDateTime.now()),
            context,
            startedAt,
            endedAt,
            LocalDateTime.now()
        );
    }
    
    /**
     * Close session
     */
    public ChatSession close(String reason) {
        var now = LocalDateTime.now();
        return new ChatSession(
            sessionId,
            userId,
            conversationId,
            messages,
            sessionType,
            new SessionStatus.Closed(reason, now),
            context,
            startedAt,
            now,
            now
        );
    }
}
