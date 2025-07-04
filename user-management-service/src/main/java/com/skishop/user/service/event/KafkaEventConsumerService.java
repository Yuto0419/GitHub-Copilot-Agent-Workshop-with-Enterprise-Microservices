package com.skishop.user.service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skishop.user.dto.event.EventDto;
import com.skishop.user.dto.event.UserRegistrationPayload;
import com.skishop.user.dto.event.UserDeletionPayload;
import com.skishop.user.entity.ProcessedEvent;
import com.skishop.user.repository.ProcessedEventRepository;
import com.skishop.user.service.saga.SagaOrchestrator;
import com.skishop.user.service.metrics.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Kafka Event Consumer Service
 * Receiving and processing events via Kafka
 * Implementation considering idempotency and error handling
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventConsumerService {

    private final SagaOrchestrator sagaOrchestrator;
    private final ProcessedEventRepository processedEventRepository;
    private final MetricsService metricsService;
    private final ObjectMapper objectMapper;

    /**
     * Process user registration event
     */
    @KafkaListener(topics = "${kafka.topics.user-registration:user-registration}")
    @Transactional
    public void handleUserRegistrationEvent(
            @Payload String eventJson,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        
        long startTime = System.currentTimeMillis();
        EventDto<UserRegistrationPayload> event = null;
        
        try {
            log.info("User registration event received: topic={}, partition={}, offset={}", topic, partition, offset);
            
            // Convert JSON to EventDto
            event = objectMapper.readValue(eventJson, 
                objectMapper.getTypeFactory().constructParametricType(EventDto.class, UserRegistrationPayload.class));
            
            // Idempotency check
            if (isEventAlreadyProcessed(event.getEventId())) {
                log.info("Event already processed (idempotency): eventId={}", event.getEventId());
                ack.acknowledge();
                return;
            }

            // Record event processing
            recordEventProcessing(event.getEventId(), "USER_REGISTRATION", eventJson);

            // Start Saga
            sagaOrchestrator.startUserRegistrationSaga(event);

            // Record metrics
            long processingTime = System.currentTimeMillis() - startTime;
            metricsService.recordEventProcessed("USER_REGISTRATION", "production", processingTime);
            metricsService.recordEventSuccessful("USER_REGISTRATION", "production", eventJson.length());

            ack.acknowledge();
            log.info("User registration event processing completed: eventId={}, processingTime={}ms", 
                     event.getEventId(), processingTime);

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            String eventId = event != null ? event.getEventId() : "unknown";
            
            log.error("User registration event processing error: eventId={}, error={}", eventId, e.getMessage(), e);
            
            metricsService.recordEventFailure("USER_REGISTRATION", "production", processingTime, 
                                             e.getClass().getSimpleName(), e.getMessage());
            
            // On error, NACK to promote reprocessing or send to DLQ
            // Acknowledge or throw exception according to configuration
            ack.acknowledge(); // Acknowledge here to avoid infinite retry
        }
    }

    /**
     * Process user deletion event
     */
    @KafkaListener(topics = "${kafka.topics.user-deletion:user-deletion}")
    @Transactional
    public void handleUserDeletionEvent(
            @Payload String eventJson,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        
        long startTime = System.currentTimeMillis();
        EventDto<UserDeletionPayload> event = null;
        
        try {
            log.info("User deletion event received: topic={}, partition={}, offset={}", topic, partition, offset);
            
            // JSON -> EventDto conversion
            event = objectMapper.readValue(eventJson, 
                objectMapper.getTypeFactory().constructParametricType(EventDto.class, UserDeletionPayload.class));
            
            // Idempotency check
            if (isEventAlreadyProcessed(event.getEventId())) {
                log.info("Event already processed (idempotency): eventId={}", event.getEventId());
                ack.acknowledge();
                return;
            }

            // Record event processing
            recordEventProcessing(event.getEventId(), "USER_DELETION", eventJson);

            // Start Saga
            sagaOrchestrator.startUserDeletionSaga(event);

            // Record metrics
            long processingTime = System.currentTimeMillis() - startTime;
            metricsService.recordEventProcessed("USER_DELETION", "production", processingTime);
            metricsService.recordEventSuccessful("USER_DELETION", "production", eventJson.length());

            ack.acknowledge();
            log.info("User deletion event processing completed: eventId={}, processingTime={}ms", 
                     event.getEventId(), processingTime);

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            String eventId = event != null ? event.getEventId() : "unknown";
            
            log.error("User deletion event processing error: eventId={}, error={}", eventId, e.getMessage(), e);
            
            metricsService.recordEventFailure("USER_DELETION", "production", processingTime, 
                                             e.getClass().getSimpleName(), e.getMessage());
            
            ack.acknowledge(); // Acknowledge here to avoid infinite retry
        }
    }

    /**
     * Generic event handler (fallback)
     */
    @KafkaListener(topics = "${kafka.topics.fallback:fallback-events}")
    @Transactional
    public void handleGenericEvent(
            @Payload String eventJson,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        
        try {
            log.info("Generic event received: topic={}, partition={}, offset={}", topic, partition, offset);
            
            // Parse basic EventDto structure
            EventDto<?> event = objectMapper.readValue(eventJson, EventDto.class);
            
            log.warn("Unsupported event type: eventType={}, eventId={}", 
                     event.getEventType(), event.getEventId());
            
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Generic event processing error: topic={}, error={}", topic, e.getMessage(), e);
            ack.acknowledge();
        }
    }

    /**
     * Check for duplicate event processing (idempotency guarantee)
     */
    private boolean isEventAlreadyProcessed(String eventId) {
        return processedEventRepository.existsByEventId(eventId);
    }

    /**
     * Record event processing
     */
    private void recordEventProcessing(String eventId, String eventType, String eventData) {
        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .eventId(eventId)
                .eventType(eventType)
                .eventData(eventData)
                .processedAt(LocalDateTime.now())
                .build();
        
        processedEventRepository.save(processedEvent);
        log.debug("Event processing record saved: eventId={}, eventType={}", eventId, eventType);
    }
}
