package com.skishop.auth.service.compensation;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

/**
 * Context information for compensation processing.
 */
@Data
@Builder
public class CompensationContext {
    
    /**
     * Saga ID
     */
    private String sagaId;
    
    /**
     * Type of Saga
     */
    private String sagaType;
    
    /**
     * Target user ID
     */
    private UUID userId;
    
    // Default constructor for manual builder
    public CompensationContext() {}
    
    // All-args constructor for Lombok compatibility
    public CompensationContext(String sagaId, String sagaType, UUID userId, 
                              String failureReason, String currentStatus, 
                              Map<String, Object> sagaData, Map<String, Object> additionalData, 
                              long errorTimestamp, int retryCount) {
        this.sagaId = sagaId;
        this.sagaType = sagaType;
        this.userId = userId;
        this.failureReason = failureReason;
        this.currentStatus = currentStatus;
        this.sagaData = sagaData;
        this.additionalData = additionalData;
        this.errorTimestamp = errorTimestamp;
        this.retryCount = retryCount;
    }
    
    /**
     * Reason for failure
     */
    private String failureReason;
    
    /**
     * Current status
     */
    private String currentStatus;
    
    /**
     * Data at the time of failure
     */
    private Map<String, Object> sagaData;
    
    /**
     * Additional information required for compensation processing
     */
    private Map<String, Object> additionalData;
    
    /**
     * Error occurrence time (milliseconds)
     */
    private long errorTimestamp;
    
    /**
     * Number of retries
     */
    private int retryCount;
    
    // Manual builder method since Lombok may not be working properly
    public static CompensationContextBuilder builder() {
        return new CompensationContextBuilder();
    }
    
    // Inner Builder class
    public static class CompensationContextBuilder {
        private String sagaId;
        private String sagaType;
        private UUID userId;
        private String failureReason;
        private String currentStatus;
        private Map<String, Object> sagaData;
        private Map<String, Object> additionalData;
        private long errorTimestamp;
        private int retryCount;
        
        public CompensationContextBuilder sagaId(String sagaId) { this.sagaId = sagaId; return this; }
        public CompensationContextBuilder sagaType(String sagaType) { this.sagaType = sagaType; return this; }
        public CompensationContextBuilder userId(UUID userId) { this.userId = userId; return this; }
        public CompensationContextBuilder failureReason(String failureReason) { this.failureReason = failureReason; return this; }
        public CompensationContextBuilder currentStatus(String currentStatus) { this.currentStatus = currentStatus; return this; }
        public CompensationContextBuilder sagaData(Map<String, Object> sagaData) { this.sagaData = sagaData; return this; }
        public CompensationContextBuilder additionalData(Map<String, Object> additionalData) { this.additionalData = additionalData; return this; }
        public CompensationContextBuilder errorTimestamp(long errorTimestamp) { this.errorTimestamp = errorTimestamp; return this; }
        public CompensationContextBuilder retryCount(int retryCount) { this.retryCount = retryCount; return this; }
        
        public CompensationContext build() {
            CompensationContext context = new CompensationContext();
            context.sagaId = this.sagaId;
            context.sagaType = this.sagaType;
            context.userId = this.userId;
            context.failureReason = this.failureReason;
            context.currentStatus = this.currentStatus;
            context.sagaData = this.sagaData;
            context.additionalData = this.additionalData;
            context.errorTimestamp = this.errorTimestamp;
            context.retryCount = this.retryCount;
            return context;
        }
    }
}
