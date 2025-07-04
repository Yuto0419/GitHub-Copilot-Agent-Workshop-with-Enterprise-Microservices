package com.skishop.user.service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skishop.user.dto.event.EventDto;
import com.skishop.user.dto.event.UserDeletionPayload;
import com.skishop.user.dto.event.UserRegistrationPayload;
import com.skishop.user.entity.ProcessedEvent;
import com.skishop.user.exception.EventProcessingException;
import com.skishop.user.repository.ProcessedEventRepository;
import com.skishop.user.service.saga.SagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event processing handler
 * Separated for proper transaction management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventHandlerService {

    private final ProcessedEventRepository processedEventRepository;
    private final SagaOrchestrator sagaOrchestrator;
    private final ObjectMapper objectMapper;

    /**
     * Process user registration event
     * Clearly separate event processing and Saga orchestration
     * Set appropriate transaction boundaries
     */
    public void handleUserRegisteredEvent(String eventJson) {
        EventDto<UserRegistrationPayload> event = null;
        
        try {
            // Parse event (executed outside transaction)
            event = parseAndValidateEvent(eventJson);
            
            // Idempotency check (read-only transaction)
            if (isEventAlreadyProcessed(event.getSagaId())) {
                log.info("Event already processed, skipping: {}", event.getSagaId());
                return;
            }
            
            log.info("Starting user registration event processing: userId={}, sagaId={}", 
                    event.getPayload().getUserId(), event.getSagaId());
            
            // Start Saga orchestration (independent transaction)
            // SagaOrchestrator starts a new transaction internally
            sagaOrchestrator.startUserRegistrationSaga(event);
            
        } catch (Exception e) {
            log.error("User registration event processing error: {}", e.getMessage(), e);
            
            // Record event processing failure (executed in independent transaction)
            if (event != null) {
                try {
                    markEventAsProcessed(event.getSagaId(), 
                            "USER_REGISTERED", 
                            event.getPayload().getUserId(), 
                            false, 
                            e.getMessage());
                } catch (Exception ex) {
                    log.error("Failed to record event processing failure: sagaId={}, error={}", 
                            event.getSagaId(), ex.getMessage(), ex);
                }
            }
            
            throw new EventProcessingException("Failed to process user registration event", e);
        }
    }

    /**
     * Process user deletion event
     * Clearly separate event processing and Saga orchestration
     * Set appropriate transaction boundaries
     */
    public void handleUserDeletedEvent(String eventJson) {
        EventDto<UserDeletionPayload> event = null;
        
        try {
            // Parse event (executed outside transaction)
            event = parseAndValidateEvent(eventJson);
            
            // Idempotency check (read-only transaction)
            if (isEventAlreadyProcessed(event.getSagaId())) {
                log.info("Event already processed, skipping: {}", event.getSagaId());
                return;
            }
            
            log.info("Starting user deletion event processing: userId={}, sagaId={}", 
                    event.getPayload().getUserId(), event.getSagaId());
            
            // Start Saga orchestration (independent transaction)
            // SagaOrchestrator starts a new transaction internally
            sagaOrchestrator.startUserDeletionSaga(event);
            
        } catch (Exception e) {
            log.error("User deletion event processing error: {}", e.getMessage(), e);
            
            // Record event processing failure (executed in independent transaction)
            if (event != null) {
                try {
                    markEventAsProcessed(event.getSagaId(), 
                            "USER_DELETED", 
                            event.getPayload().getUserId(), 
                            false, 
                            e.getMessage());
                } catch (Exception ex) {
                    log.error("Failed to record event processing failure: sagaId={}, error={}", 
                            event.getSagaId(), ex.getMessage(), ex);
                }
            }
            
            throw new EventProcessingException("Failed to process user deletion event", e);
        }
    }

    /**
     * Check if event is already processed (read-only transaction)
     */
    @Transactional(readOnly = true)
    public boolean isEventAlreadyProcessed(String sagaId) {
        return processedEventRepository.existsBySagaId(sagaId);
    }

    /**
     * Record event as processed (independent transaction)
     */
    @Transactional
    public void markEventAsProcessed(String sagaId, String eventType, String userId, boolean success, String errorMessage) {
        ProcessedEvent processedEvent = ProcessedEvent.builder()
            .sagaId(sagaId)
            .eventType(eventType)
            .userId(UUID.fromString(userId))
            .isSuccess(success)
            .errorMessage(errorMessage)
            .processedAt(LocalDateTime.now())
            .build();
        
        processedEventRepository.save(processedEvent);
        log.debug("Recorded event processing status: sagaId={}, eventType={}, success={}", 
                sagaId, eventType, success);
    }
    
    /**
     * Parse and validate event (generic)
     */
    @SuppressWarnings("unchecked")
    private <T> EventDto<T> parseAndValidateEvent(String eventJson) {
        try {
            EventDto<T> event = objectMapper.readValue(eventJson, EventDto.class);
            validateEventSchema(event);
            return event;
        } catch (Exception e) {
            log.error("Failed to parse event JSON: {}", e.getMessage(), e);
            throw new EventProcessingException("Invalid event format", e);
        }
    }
    
    /**
     * Validate event schema
     */
    private void validateEventSchema(EventDto<?> event) {
        if (event.getEventId() == null || event.getEventType() == null || 
            event.getPayload() == null || event.getSagaId() == null) {
            throw new IllegalArgumentException("Required event fields are missing");
        }
    }
}
