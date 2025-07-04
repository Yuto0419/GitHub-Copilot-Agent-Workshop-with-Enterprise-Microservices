package com.skishop.ai.entity;

/**
 * Chat session type - Using Java 21's sealed interface
 */
public sealed interface SessionType permits SessionType.Support, SessionType.Recommendation, SessionType.Search {
    record Support(String category) implements SessionType {}
    record Recommendation(String intent) implements SessionType {}
    record Search(String domain) implements SessionType {}
}
