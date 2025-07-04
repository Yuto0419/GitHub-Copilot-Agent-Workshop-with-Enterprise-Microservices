package com.skishop.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Skishop runtime environment configuration properties
 */
@ConfigurationProperties(prefix = "skishop.runtime")
@Component
@Data
public class SkishopRuntimeProperties {
    
    /**
     * Runtime environment setting
     * local: Local development environment (Redis + REST)
     * production: Production environment (Azure Service Bus + Event Grid)
     */
    private Runtime environment = Runtime.LOCAL;
    
    /**
     * Enable/disable event propagation feature
     */
    private boolean eventPropagationEnabled = false;
    
    /**
     * Control publishing of user registration events
     */
    private boolean userRegistrationEventEnabled = false;
    
    /**
     * Control publishing of user deletion events
     */
    private boolean userDeletionEventEnabled = false;
    
    /**
     * Control fallback behavior
     */
    private boolean fallbackToSyncProcessing = true;
    
    /**
     * Debug mode
     */
    private boolean debugMode = false;
    
    /**
     * Event broker type
     */
    private String eventBrokerType = "redis";
    
    /**
     * Event timeout (milliseconds)
     */
    private long eventTimeoutMs = 30000;
    
    /**
     * Redis key prefix
     */
    private String eventRedisKeyPrefix = "skishop";
    
    /**
     * Maximum number of event retries
     */
    private int eventMaxRetries = 3;
    
    /**
     * Number of concurrent event executions
     */
    private int eventConcurrency = 10;
    

    
    /**
     * Saga timeout configuration
     */
    private SagaConfig saga = new SagaConfig();
    
    /**
     * Retry configuration
     */
    private RetryConfig retry = new RetryConfig();
    
    public enum Runtime {
        LOCAL, PRODUCTION
    }
    
    @Data
    public static class SagaConfig {
        private Duration timeout = Duration.ofSeconds(30);
        private int maxActiveSagas = 1000;
    }

    @Data
    public static class RetryConfig {
        private int maxAttempts = 3;
        private Duration initialDelay = Duration.ofMillis(1000);
        private double multiplier = 2.0;
        private Duration maxDelay = Duration.ofMillis(10000);
    }

    // Methods
    public boolean isEventPropagationEnabled() {
        return eventPropagationEnabled;
    }

    public String getEventBrokerType() {
        return eventBrokerType;
    }

    public long getEventTimeoutMs() {
        return eventTimeoutMs;
    }

    public String getEventRedisKeyPrefix() {
        return eventRedisKeyPrefix;
    }

    public String getEnvironment() {
        return environment.name().toLowerCase();
    }

    public int getEventMaxRetries() {
        return eventMaxRetries;
    }

    public int getEventConcurrency() {
        return eventConcurrency;
    }
}
