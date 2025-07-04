package com.skishop.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive application configuration validation
 */
@Component
@Validated
@RequiredArgsConstructor
@Slf4j
public class ConfigurationValidator implements ApplicationListener<ApplicationReadyEvent> {

    private final SkishopRuntimeProperties runtimeProperties;
    
    @Value("${azure.servicebus.connection-string:}")
    private String serviceBusConnectionString;
    
    @Value("${azure.eventgrid.topic-endpoint:}")
    private String eventGridEndpoint;
    
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;
    
    @Value("${spring.data.redis.password:}")
    private String redisPassword;
    
    @Value("${saga.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${saga.timeout:30000}")
    private int sagaTimeout;
    
    @Value("${spring.datasource.url:}")
    private String datasourceUrl;
    
    @Value("${spring.datasource.username:}")
    private String datasourceUsername;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            validateConfiguration();
            log.info("Configuration validation completed successfully");
        } catch (Exception e) {
            log.error("Configuration validation failed: {}", e.getMessage());
            // In production environment, may stop startup
            // System.exit(1);
        }
    }
    
    private void validateConfiguration() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Validate settings by environment
        String environment = runtimeProperties.getEnvironment();
        log.info("Validating configuration for environment: {}", environment);

        if ("production".equals(environment)) {
            validateProductionConfiguration(errors, warnings);
        } else if ("staging".equals(environment)) {
            validateStagingConfiguration(warnings);
        } else {
            validateLocalConfiguration(errors, warnings);
        }

        // Validate common settings
        validateCommonConfiguration(errors, warnings);

        // Log warnings
        if (!warnings.isEmpty()) {
            log.warn("Configuration warnings found:\n{}", String.join("\n", warnings));
        }

        // Throw exception if there are errors
        if (!errors.isEmpty()) {
            String errorMessage = "Configuration validation errors:\n" + String.join("\n", errors);
            throw new IllegalStateException(errorMessage);
        }
    }
    
    private void validateProductionConfiguration(List<String> errors, List<String> warnings) {
        // Required settings for production environment
        if (!runtimeProperties.isEventPropagationEnabled()) {
            errors.add("Event propagation must be enabled in production environment");
        }

        // Azure Service Bus settings check
        if ("azure-servicebus".equals(runtimeProperties.getEventBrokerType())
                && (serviceBusConnectionString == null || serviceBusConnectionString.trim().isEmpty())) {
            errors.add("Azure Service Bus connection string is not set in production environment");
        }

        // Security settings check
        if (runtimeProperties.isDebugMode()) {
            warnings.add("Debug mode is enabled in production environment");
        }

        // Database settings check
        if (datasourceUrl.contains("localhost") || datasourceUrl.contains("127.0.0.1")) {
            warnings.add("Local database may be used in production environment");
        }

        // Redis settings check
        if ("localhost".equals(redisHost) || "127.0.0.1".equals(redisHost)) {
            warnings.add("Local Redis may be used in production environment");
        }

        if (redisPassword.isEmpty()) {
            warnings.add("Redis password is not set in production environment");
        }
    }
    
    private void validateStagingConfiguration(List<String> warnings) {
        // Staging environment settings
        if (!runtimeProperties.isEventPropagationEnabled()) {
            warnings.add("Event propagation is disabled in staging environment");
        }

        // Azure Service Bus settings check (recommended for staging)
        if ("azure-servicebus".equals(runtimeProperties.getEventBrokerType())
                && (serviceBusConnectionString == null || serviceBusConnectionString.trim().isEmpty())) {
            warnings.add("Azure Service Bus connection string is not set in staging environment");
        }
    }
    
    private void validateLocalConfiguration(List<String> errors, List<String> warnings) {
        // Local environment settings
        if ("azure-servicebus".equals(runtimeProperties.getEventBrokerType()) && 
            (serviceBusConnectionString == null || serviceBusConnectionString.trim().isEmpty())) {
            warnings.add("Azure Service Bus is selected in local environment, but connection string is not set. Falling back to Redis.");
        }

        // Redis settings check
        if (redisHost == null || redisHost.trim().isEmpty()) {
            errors.add("Redis host is not set");
        }

        if (redisPort <= 0 || redisPort > 65535) {
            errors.add("Invalid Redis port number: " + redisPort);
        }
    }
    
    private void validateCommonConfiguration(List<String> errors, List<String> warnings) {
        // Basic settings check
        if (maxRetryAttempts < 0 || maxRetryAttempts > 10) {
            warnings.add("Retry attempts are out of recommended range (recommended: 0-10): " + maxRetryAttempts);
        }

        if (sagaTimeout < 1000 || sagaTimeout > 300000) { // 1 second - 5 minutes
            warnings.add("Saga timeout is out of recommended range (recommended: 1000-300000ms): " + sagaTimeout);
        }

        // Data source settings check
        if (datasourceUrl == null || datasourceUrl.trim().isEmpty()) {
            errors.add("Datasource URL is not set");
        }

        if (datasourceUsername == null || datasourceUsername.trim().isEmpty()) {
            errors.add("Datasource username is not set");
        }

        // Event propagation settings check
        String eventBrokerType = runtimeProperties.getEventBrokerType();
        if (eventBrokerType == null || 
            (!eventBrokerType.equals("redis") && !eventBrokerType.equals("azure-servicebus"))) {
            errors.add("Invalid event broker type: " + eventBrokerType);
        }

        // Validate Runtime Properties
        validateRuntimeProperties(warnings);
    }
    
    private void validateRuntimeProperties(List<String> warnings) {
        if (runtimeProperties.getEventTimeoutMs() < 5000 || runtimeProperties.getEventTimeoutMs() > 600000) {
            warnings.add("Event timeout is out of recommended range (recommended: 5000-600000ms): " + runtimeProperties.getEventTimeoutMs());
        }

        if (runtimeProperties.getEventMaxRetries() < 0 || runtimeProperties.getEventMaxRetries() > 5) {
            warnings.add("Maximum event retries are out of recommended range (recommended: 0-5): " + runtimeProperties.getEventMaxRetries());
        }

        if (runtimeProperties.getEventConcurrency() < 1 || runtimeProperties.getEventConcurrency() > 20) {
            warnings.add("Event concurrency is out of recommended range (recommended: 1-20): " + runtimeProperties.getEventConcurrency());
        }

        String redisKeyPrefix = runtimeProperties.getEventRedisKeyPrefix();
        if (redisKeyPrefix == null || redisKeyPrefix.trim().isEmpty()) {
            warnings.add("Redis key prefix is not set");
        } else if (redisKeyPrefix.contains(" ") || redisKeyPrefix.contains(":")) {
            warnings.add("Redis key prefix contains invalid characters: " + redisKeyPrefix);
        }
    }
}
