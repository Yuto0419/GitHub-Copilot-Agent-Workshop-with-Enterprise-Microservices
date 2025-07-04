package com.skishop.ai.entity;

/**
 * Sealed interface representing message role
 * 
 * <p>Using Java 21 sealed interface to define message roles in a type-safe manner</p>
 * <p>Provides pattern matching and exhaustiveness checking in switch statements</p>
 * 
 * @since 1.0.0
 */
public sealed interface MessageRole 
    permits MessageRole.User, MessageRole.Assistant, MessageRole.System {
    
    /**
     * Message from the user
     */
    record User() implements MessageRole {}
    
    /**
     * Message from the AI assistant
     */
    record Assistant() implements MessageRole {}
    
    /**
     * System message
     */
    record System() implements MessageRole {}
    
    /**
     * Factory method to create MessageRole from string
     * 
     * @param roleString string representing the role
     * @return corresponding MessageRole instance
     * @throws IllegalArgumentException if the role is unknown
     */
    static MessageRole fromString(String roleString) {
        return switch (roleString.toUpperCase()) {
            case "USER" -> new User();
            case "ASSISTANT" -> new Assistant();
            case "SYSTEM" -> new System();
            default -> throw new IllegalArgumentException("Unknown role: " + roleString);
        };
    }
    
    /**
     * Convert MessageRole to string
     * 
     * @return string representing the role
     */
    default String asString() {
        return switch (this) {
            case User() -> "USER";
            case Assistant() -> "ASSISTANT";
            case System() -> "SYSTEM";
        };
    }
    
    /**
     * Method for permission checking
     * 
     * @return Whether message creation is allowed
     */
    default boolean canCreateMessage() {
        return switch (this) {
            case User(), System() -> true;
            case Assistant() -> false; // Assistant messages are typically created by the system
        };
    }
    
    /**
     * Get display name for UI presentation
     * 
     * @return Display string
     */
    default String getDisplayName() {
        return switch (this) {
            case User() -> "User";
            case Assistant() -> "AI Assistant";
            case System() -> "System";
        };
    }
}
