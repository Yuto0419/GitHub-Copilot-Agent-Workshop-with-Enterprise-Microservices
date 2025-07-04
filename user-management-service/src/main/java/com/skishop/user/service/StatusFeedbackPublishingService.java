package com.skishop.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skishop.user.config.SkishopRuntimeProperties;
import com.skishop.user.dto.event.EventDto;
import com.skishop.user.exception.EventProcessingException;
import com.skishop.user.service.azure.AzureServiceBusStatusFeedbackPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Service for sending status feedback to Authentication-service
 */
@Service
@Slf4j
public class StatusFeedbackPublishingService {

    private final SkishopRuntimeProperties runtimeProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final AzureServiceBusStatusFeedbackPublisher azureServiceBusStatusFeedbackPublisher;

    public StatusFeedbackPublishingService(
            SkishopRuntimeProperties runtimeProperties,
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper,
            @Autowired(required = false) AzureServiceBusStatusFeedbackPublisher azureServiceBusStatusFeedbackPublisher) {
        this.runtimeProperties = runtimeProperties;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.azureServiceBusStatusFeedbackPublisher = azureServiceBusStatusFeedbackPublisher;
    }

    /**
     * Send processing success status
     */
    @Transactional
    public void publishSuccessStatus(String sagaId, String userId, String originalEventId, long processingTimeMs) {
        publishStatusFeedback(sagaId, userId, originalEventId, "SUCCESS", null, processingTimeMs);
    }

    /**
     * Send processing failure status
     */
    @Transactional
    public void publishFailureStatus(String sagaId, String userId, String originalEventId, String reason, long processingTimeMs) {
        publishStatusFeedback(sagaId, userId, originalEventId, "FAILED", reason, processingTimeMs);
    }

    /**
     * Publish status feedback event
     */
    private void publishStatusFeedback(String sagaId, String userId, String originalEventId, String status, String reason, long processingTimeMs) {
        if (!runtimeProperties.isEventPropagationEnabled()) {
            log.debug("Event propagation is disabled, skipping status feedback for saga {}", sagaId);
            return;
        }

        try {
            String eventId = UUID.randomUUID().toString();
            String correlationId = UUID.randomUUID().toString();

            // Create status feedback payload
            Map<String, Object> payload = Map.of(
                "userId", userId,
                "originalEventId", originalEventId,
                "status", status,
                "reason", reason != null ? reason : "",
                "processingTime", processingTimeMs
            );

            // Create event with complete schema
            EventDto<Map<String, Object>> event = EventDto.<Map<String, Object>>builder()
                .eventId(eventId)
                .eventType("USER_MANAGEMENT_STATUS")
                .timestamp(Instant.now())
                .version("1.0")
                .producer("user-management-service")
                .payload(payload)
                .correlationId(correlationId)
                .sagaId(sagaId)
                .retry(0)
                .build();

            // Publish to Redis/message broker
            publishEventToRedis("USER_MANAGEMENT_STATUS", event);

            log.info("Successfully published status feedback for saga {}: {}", sagaId, status);

        } catch (Exception e) {
            log.error("Failed to publish status feedback for saga {}: {}", sagaId, e.getMessage(), e);
            throw new EventProcessingException("Failed to publish status feedback", e);
        }
    }

    /**
     * Publish event to appropriate message broker
     */
    private void publishEventToRedis(String eventType, EventDto<Map<String, Object>> event) {
        try {
            // Use Azure Service Bus if enabled and available
            if (isAzureServiceBusEnabled() && azureServiceBusStatusFeedbackPublisher != null) {
                azureServiceBusStatusFeedbackPublisher.publishStatusFeedback(event);
                log.debug("Published status feedback to Azure Service Bus: eventType={}", eventType);
            } else if ("redis".equals(runtimeProperties.getEventBrokerType())) {
                // Use Redis
                String channel = getRedisChannel(eventType);
                String eventJson = objectMapper.writeValueAsString(event);
                redisTemplate.convertAndSend(channel, eventJson);
                log.debug("Published status feedback to Redis channel: {}", channel);
            } else {
                // Fallback: Use Redis
                log.warn("Event broker type {} not supported, using Redis fallback", 
                    runtimeProperties.getEventBrokerType());
                String channel = getRedisChannel(eventType);
                String eventJson = objectMapper.writeValueAsString(event);
                redisTemplate.convertAndSend(channel, eventJson);
            }
            
        } catch (Exception e) {
            log.error("Failed to publish status feedback: eventType={}, error={}", eventType, e.getMessage(), e);
            throw new EventProcessingException("Failed to publish status feedback", e);
        }
    }

    /**
     * Check if Azure Service Bus is enabled
     */
    private boolean isAzureServiceBusEnabled() {
        try {
            // Determine based on configuration properties
            return runtimeProperties.getAzureServicebus() != null && 
                   runtimeProperties.getAzureServicebus().isEnabled();
        } catch (Exception e) {
            log.debug("Azure Service Bus availability check failed", e);
            return false;
        }
    }

    /**
     * Get Redis channel name
     */
    private String getRedisChannel(String eventType) {
        return String.format("%s:events:%s", 
            runtimeProperties.getEventRedisKeyPrefix(), 
            eventType.toLowerCase());
    }
}
