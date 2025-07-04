package com.skishop.auth.service.azure;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skishop.auth.dto.EventDto;
import com.skishop.auth.exception.EventSerializationException;
import com.skishop.auth.exception.EventPublishingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Azure Service Bus event publishing service
 * 
 * Uses Azure Service Bus for event propagation in production environment
 * Secure authentication with managed identity
 * Supports automatic retry and dead-letter queue
 */
@Service
@ConditionalOnProperty(
    prefix = "skishop.runtime.azure-servicebus", 
    name = "enabled", 
    havingValue = "true"
)
@RequiredArgsConstructor
@Slf4j
public class AzureServiceBusEventPublisher {

    private final ServiceBusSenderClient eventSenderClient;
    private final ObjectMapper objectMapper;

    /**
     * Publish event to Azure Service Bus topic
     * 
     * @param event Event to publish
     * @throws RuntimeException if publishing fails
     */
    @Retryable(
        retryFor = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void publishEvent(EventDto event) {
        try {
            log.debug("Publishing event to Azure Service Bus: eventId={}, eventType={}, sagaId={}", 
                event.getEventId(), event.getEventType(), event.getSagaId());

            // Serialize event to JSON
            String jsonPayload = objectMapper.writeValueAsString(event);
            
            // Create Service Bus message
            ServiceBusMessage message = new ServiceBusMessage(jsonPayload)
                .setContentType("application/json")
                .setCorrelationId(event.getCorrelationId())
                .setMessageId(event.getEventId())
                .setTimeToLive(Duration.ofMinutes(30)); // 30分でタイムアウト

            // Set message properties (for filtering)
            Map<String, Object> properties = new HashMap<>();
            properties.put("eventType", event.getEventType());
            properties.put("producer", event.getProducer());
            properties.put("sagaId", event.getSagaId());
            properties.put("version", event.getVersion());
            
            message.getApplicationProperties().putAll(properties);

            // Send message
            eventSenderClient.sendMessage(message);
            
            log.info("Successfully published event to Azure Service Bus: eventId={}, eventType={}", 
                event.getEventId(), event.getEventType());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event to JSON: eventId={}", event.getEventId(), e);
            throw new EventSerializationException("Event serialization failed", e);
        } catch (Exception e) {
            log.error("Failed to publish event to Azure Service Bus: eventId={}, eventType={}", 
                event.getEventId(), event.getEventType(), e);
            throw new EventPublishingException("Event publishing failed", e);
        }
    }

    /**
     * Publish events in batch (improves performance)
     * 
     * @param events List of events to publish
     */
    @Retryable(
        retryFor = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void publishEvents(Iterable<EventDto> events) {
        try {
            log.debug("Publishing batch of events to Azure Service Bus");

            for (EventDto event : events) {
                publishEvent(event);
            }
            
            log.info("Successfully published batch of events to Azure Service Bus");

        } catch (Exception e) {
            log.error("Failed to publish batch of events to Azure Service Bus", e);
            throw new EventPublishingException("Batch event publishing failed", e);
        }
    }

    /**
     * Check the health of the Service Bus connection
     * 
     * @return true if the connection is healthy
     */
    public boolean isHealthy() {
        try {
            // Send a lightweight health test message
            ServiceBusMessage testMessage = new ServiceBusMessage("health-check")
                .setContentType("text/plain")
                .setTimeToLive(Duration.ofSeconds(10));
            
            eventSenderClient.sendMessage(testMessage);
            return true;
        } catch (Exception e) {
            log.warn("Azure Service Bus health check failed", e);
            return false;
        }
    }
}
