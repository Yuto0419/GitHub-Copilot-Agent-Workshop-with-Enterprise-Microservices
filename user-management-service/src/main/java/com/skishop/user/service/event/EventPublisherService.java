package com.skishop.user.service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skishop.user.dto.event.EventDto;
import com.skishop.user.service.metrics.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Event Publisher Service
 * Event publishing via Kafka
 * Implements asynchronous processing, error handling, and fallback functionality
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final MetricsService metricsService;

    @Value("${kafka.topics.user-management-status:user-management-status}")
    private String statusTopic;

    @Value("${kafka.producer.retry.max:3}")
    private Integer maxRetryCount;

    @Value("${kafka.producer.timeout:10000}")
    private Long timeoutMs;

    /**
     * Publish event (asynchronous)
     */
    public CompletableFuture<Boolean> publishEvent(EventDto<?> event) {
        long startTime = System.currentTimeMillis();
        
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String topic = determineTopicByEventType(event.getEventType());
            String key = generatePartitionKey(event);

            log.info("Event publishing started: eventId={}, eventType={}, topic={}", 
                     event.getEventId(), event.getEventType(), topic);

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, eventJson);
            
            return future.handle((result, throwable) -> {
                long processingTime = System.currentTimeMillis() - startTime;
                
                if (throwable != null) {
                    log.error("Event publishing failed: eventId={}, topic={}, error={}", 
                             event.getEventId(), topic, throwable.getMessage(), throwable);
                    
                    metricsService.recordEventFailure(event.getEventType(), "production", processingTime, 
                                                     throwable.getClass().getSimpleName(), throwable.getMessage());
                    return false;
                } else {
                    log.info("Event publishing successful: eventId={}, topic={}, partition={}, offset={}, processingTime={}ms", 
                             event.getEventId(), topic, 
                             result.getRecordMetadata().partition(), 
                             result.getRecordMetadata().offset(),
                             processingTime);
                    
                    metricsService.recordEventSuccessful(event.getEventType(), "production", eventJson.length());
                    return true;
                }
            });

        } catch (Exception e) {
            log.error("Event publication error: eventId={}, error={}", event.getEventId(), e.getMessage(), e);
            
            long processingTime = System.currentTimeMillis() - startTime;
            metricsService.recordEventFailure(event.getEventType(), "production", processingTime, 
                                             e.getClass().getSimpleName(), e.getMessage());
            
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Publish event (synchronous - for health check)
     */
    public boolean publishEventSync(String eventType, Object payload, String correlationId) {
        try {
            EventDto<Object> event = EventDto.builder()
                    .eventId(java.util.UUID.randomUUID().toString())
                    .eventType(eventType)
                    .timestamp(java.time.Instant.now())
                    .version("1.0")
                    .producer("user-management-service")
                    .payload(payload)
                    .correlationId(correlationId)
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            String topic = determineTopicByEventType(eventType);
            String key = generatePartitionKey(event);

            SendResult<String, String> result = kafkaTemplate.send(topic, key, eventJson).get();
            
            log.debug("Synchronous event publication success: eventType={}, topic={}, partition={}, offset={}", 
                     eventType, topic, 
                     result.getRecordMetadata().partition(), 
                     result.getRecordMetadata().offset());
            
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Synchronous event publication interrupted: eventType={}", eventType, e);
            return false;
        } catch (Exception e) {
            log.error("Synchronous event publication error: eventType={}, error={}", eventType, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Event publication with retry
     */
    public CompletableFuture<Boolean> publishEventWithRetry(EventDto<?> event) {
        return publishEventWithRetry(event, 0);
    }

    private CompletableFuture<Boolean> publishEventWithRetry(EventDto<?> event, int retryCount) {
        return publishEvent(event).thenCompose(success -> {
            if (Boolean.FALSE.equals(success) && retryCount < maxRetryCount) {
                log.warn("Event publication retry: eventId={}, retryCount={}/{}", 
                         event.getEventId(), retryCount + 1, maxRetryCount);
                
                // Exponential backoff
                try {
                    Thread.sleep((long) Math.pow(2, retryCount) * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return CompletableFuture.completedFuture(false);
                }
                
                return publishEventWithRetry(event, retryCount + 1);
            }
            return CompletableFuture.completedFuture(success);
        });
    }

    /**
     * Bulk event publication
     */
    public CompletableFuture<Boolean> publishEventsInBatch(java.util.List<EventDto<?>> events) {
        log.info("Starting bulk event publication: eventCount={}", events.size());
        
        java.util.List<CompletableFuture<Boolean>> futures = events.stream()
                .map(this::publishEvent)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .allMatch(future -> {
                            try {
                                return future.get();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                log.error("Bulk event processing interrupted", e);
                                return false;
                            } catch (Exception e) {
                                log.error("Error retrieving bulk event results", e);
                                return false;
                            }
                        }));
    }

    /**
     * Determine topic based on event type
     */
    private String determineTopicByEventType(String eventType) {
        return switch (eventType) {
            case "USER_MANAGEMENT_STATUS" -> statusTopic;
            case "HEALTH_CHECK" -> "health-check";
            default -> "default-events";
        };
    }

    /**
     * Generate key for partition distribution
     */
    private String generatePartitionKey(EventDto<?> event) {
        // Partition distribution based on user ID
        String userId = extractUserId(event);
        if (userId != null) {
            return userId;
        }
        
        // Fallback: correlationId or eventId
        return event.getCorrelationId() != null ? event.getCorrelationId() : event.getEventId();
    }

    /**
     * Extract user ID from event
     */
    private String extractUserId(EventDto<?> event) {
        Object payload = event.getPayload();
        if (payload instanceof com.skishop.user.dto.event.UserManagementStatusPayload statusPayload) {
            return statusPayload.getUserId();
        }
        return null;
    }

    /**
     * Verify Kafka template configuration
     */
    public boolean isKafkaAvailable() {
        try {
            kafkaTemplate.getProducerFactory().createProducer().partitionsFor("test-topic");
            return true;
        } catch (Exception e) {
            log.warn("Kafka connection check failed: {}", e.getMessage());
            return false;
        }
    }
}
