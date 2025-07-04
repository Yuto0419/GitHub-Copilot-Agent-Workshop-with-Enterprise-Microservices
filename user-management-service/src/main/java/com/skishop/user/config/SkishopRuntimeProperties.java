package com.skishop.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Skishop runtime configuration properties
 * Unified configuration property management
 */
@Configuration
@ConfigurationProperties(prefix = "skishop.runtime")
@Data
public class SkishopRuntimeProperties {

    /**
     * Enable/disable event propagation feature
     */
    private boolean eventPropagationEnabled = false;

    /**
     * Type of event broker (redis, azure-servicebus, kafka, etc.)
     */
    private String eventBrokerType = "redis";

    /**
     * Maximum number of retries for event processing
     */
    private int eventMaxRetries = 3;

    /**
     * Event processing timeout (milliseconds)
     */
    private long eventTimeoutMs = 30000;

    /**
     * Prefix for Redis event keys
     */
    private String eventRedisKeyPrefix = "skishop";

    /**
     * Event processing concurrency
     */
    private int eventConcurrency = 4;

    /**
     * Enable/disable event persistence
     */
    private boolean eventPersistenceEnabled = true;

    /**
     * Retention period for processed events (days)
     */
    private int processedEventRetentionDays = 30;

    /**
     * Enable/disable debug mode
     */
    private boolean debugMode = false;

    /**
     * Environment setting (local, development, production)
     */
    private String environment = "local";

    /**
     * Azure Service Bus settings
     */
    private AzureServiceBus azureServicebus = new AzureServiceBus();

    @Data
    public static class AzureServiceBus {
        /**
         * Enable/disable Azure Service Bus
         */
        private boolean enabled = false;

        /**
         * Topic name
         */
        private String topicName = "skishop-events-prod";

        /**
         * Subscription name
         */
        private String subscriptionName = "user-service-subscription";

        /**
         * Status feedback topic
         */
        private String statusFeedbackTopic = "skishop-status-feedback-prod";

        /**
         * Maximum concurrent calls
         */
        private int maxConcurrentCalls = 4;

        /**
         * Maximum number of retries
         */
        private int maxRetries = 3;

        /**
         * Prefetch count
         */
        private int prefetchCount = 10;
    }
}
