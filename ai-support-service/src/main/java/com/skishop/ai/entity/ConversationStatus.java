package com.skishop.ai.entity;

/**
 * Sealed interface representing conversation status
 * 
 * <p>Uses Java 21's sealed interface to define conversation statuses in a type-safe manner</p>
 * <p>Provides completeness checks in pattern matching and switch statements</p>
 * 
 * @since 1.0.0
 */
public sealed interface ConversationStatus 
    permits ConversationStatus.Active, ConversationStatus.Completed, 
            ConversationStatus.Abandoned, ConversationStatus.Escalated {
    
    /**
     * Active conversation
     */
    record Active() implements ConversationStatus {}
    
    /**
     * Completed conversation
     */
    record Completed() implements ConversationStatus {}
    
    /**
     * Abandoned conversation
     */
    record Abandoned() implements ConversationStatus {}
    
    /**
     * Escalated conversation
     */
    record Escalated() implements ConversationStatus {}
    
    /**
     * Factory method to create ConversationStatus from string
     * 
     * @param statusString String representing status
     * @return Corresponding ConversationStatus instance
     * @throws IllegalArgumentException When status is unknown
     */
    static ConversationStatus fromString(String statusString) {
        return switch (statusString.toUpperCase()) {
            case "ACTIVE" -> new Active();
            case "COMPLETED" -> new Completed();
            case "ABANDONED" -> new Abandoned();
            case "ESCALATED" -> new Escalated();
            default -> throw new IllegalArgumentException("Unknown status: " + statusString);
        };
    }
    
    /**
     * Convert ConversationStatus to string
     * 
     * @return String representing status
     */
    default String asString() {
        return switch (this) {
            case Active() -> "ACTIVE";
            case Completed() -> "COMPLETED";
            case Abandoned() -> "ABANDONED";
            case Escalated() -> "ESCALATED";
        };
    }
    
    /**
     * Determine if status is in a finished state
     * 
     * @return true if in finished state
     */
    default boolean isFinished() {
        return switch (this) {
            case Completed(), Abandoned(), Escalated() -> true;
            case Active() -> false;
        };
    }
    
    /**
     * Determine if messages can be added
     * 
     * @return true if messages can be added
     */
    default boolean canAddMessage() {
        return switch (this) {
            case Active() -> true;
            case Completed(), Abandoned(), Escalated() -> false;
        };
    }
    
    /**
     * Get display name string
     * 
     * @return Display string
     */
    default String getDisplayName() {
        return switch (this) {
            case Active() -> "Active";
            case Completed() -> "Completed";
            case Abandoned() -> "Abandoned";
            case Escalated() -> "Escalated";
        };
    }
}
