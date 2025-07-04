package com.skishop.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skishop.auth.dto.EventDto;
import com.skishop.auth.entity.SagaState;
import com.skishop.auth.enums.SagaStatus;
import com.skishop.auth.enums.UserRegistrationStatus;
import com.skishop.auth.enums.UserDeletionStatus;
import com.skishop.auth.repository.SagaStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service to receive and process status feedback from user-management-service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatusFeedbackService implements MessageListener {

    private final SagaStateRepository sagaStateRepository;
    private final EventPublishingService eventPublishingService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());
            
            log.info("Received status feedback from channel: {}, body: {}", channel, body);
            
            // Get event type from channel name
            String eventType = extractEventTypeFromChannel(channel);
            
            if ("user_management_status".equals(eventType)) {
                handleUserManagementStatusEvent(body);
            } else {
                log.warn("Unknown status event type: {} from channel: {}", eventType, channel);
            }
            
        } catch (Exception e) {
            log.error("Failed to process status feedback: {}", e.getMessage(), e);
        }
    }

    /**
     * Process status feedback from user-management-service
     */
    @Transactional
    public void handleUserManagementStatusEvent(String eventJson) {
        try {
            EventDto event = objectMapper.readValue(eventJson, EventDto.class);
            Map<String, Object> payload = (Map<String, Object>) event.getPayload();
            
            String sagaId = event.getSagaId();
            String originalEventId = (String) payload.get("originalEventId");
            String status = (String) payload.get("status");
            String reason = (String) payload.get("reason");
            Long processingTime = payload.get("processingTime") != null ? 
                ((Number) payload.get("processingTime")).longValue() : null;
            
            log.info("Processing status feedback for saga: {}, status: {}", sagaId, status);

            // Update Saga state
            sagaStateRepository.findBySagaId(sagaId).ifPresent(saga -> {
                updateSagaBasedOnStatus(saga, status, reason, processingTime);
            });
            
            log.info("Successfully processed status feedback for saga: {}", sagaId);
            
        } catch (Exception e) {
            log.error("Failed to handle user management status event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process status feedback", e);
        }
    }

    /**
     * Update Saga state based on status
     */
    private void updateSagaBasedOnStatus(SagaState saga, String status, String reason, Long processingTime) {
        try {
            if ("SUCCESS".equals(status)) {
                handleSuccessStatus(saga, processingTime);
            } else if ("FAILED".equals(status)) {
                handleFailedStatus(saga, reason, processingTime);
            } else {
            log.warn("Unknown status received: {} for saga: {}", status, saga.getSagaId());
            }
        } catch (Exception e) {
            log.error("Failed to update saga status for {}: {}", saga.getSagaId(), e.getMessage(), e);
        }
    }

    /**
     * Handle success status
     */
    private void handleSuccessStatus(SagaState saga, Long processingTime) {
        if ("USER_REGISTRATION".equals(saga.getSagaType())) {
            saga.setStatus(UserRegistrationStatus.REGISTRATION_COMPLETED.name());
            saga.setSagaStatus(SagaStatus.SAGA_COMPLETED);
        } else if ("USER_DELETION".equals(saga.getSagaType())) {
            saga.setStatus(UserDeletionStatus.DELETION_COMPLETED.name());
            saga.setSagaStatus(SagaStatus.SAGA_COMPLETED);
        }
        
        saga.setEndTime(Instant.now());
        saga.setUpdatedAt(LocalDateTime.now());
        
        // Record processing time (optional)
        if (processingTime != null) {
            saga.setData(String.format("{\"processingTime\": %d}", processingTime));
        }
        
        sagaStateRepository.save(saga);
        
        log.info("Saga completed successfully: {} (type: {})", saga.getSagaId(), saga.getSagaType());
    }

    /**
     * Handle failure status
     */
    private void handleFailedStatus(SagaState saga, String reason, Long processingTime) {
        if ("USER_REGISTRATION".equals(saga.getSagaType())) {
            saga.setStatus(UserRegistrationStatus.REGISTRATION_FAILED.name());
            saga.setSagaStatus(SagaStatus.SAGA_FAILED);
            
            // Determine if compensation is required
            if (shouldTriggerCompensation(saga)) {
                saga.setStatus(UserRegistrationStatus.COMPENSATION_REQUIRED.name());
                saga.setSagaStatus(SagaStatus.SAGA_COMPENSATING);
                
                // Execute compensation asynchronously
                triggerCompensationAsync(saga.getSagaId(), reason);
            }
            
        } else if ("USER_DELETION".equals(saga.getSagaType())) {
            saga.setStatus(UserDeletionStatus.DELETION_FAILED.name());
            saga.setSagaStatus(SagaStatus.SAGA_FAILED);
            
            // Determine if rollback is required
            if (shouldTriggerRollback(saga)) {
                saga.setStatus(UserDeletionStatus.DELETION_ROLLBACK_REQUIRED.name());
                saga.setSagaStatus(SagaStatus.SAGA_COMPENSATING);
                
                // Execute rollback asynchronously
                triggerRollbackAsync(saga.getSagaId(), reason);
            }
        }
        
        saga.setErrorReason(reason);
        saga.setEndTime(Instant.now());
        saga.setUpdatedAt(LocalDateTime.now());
        
        sagaStateRepository.save(saga);
        
        log.warn("Saga failed: {} (type: {}, reason: {})", saga.getSagaId(), saga.getSagaType(), reason);
    }

    /**
     * Determine if compensation is required
     * Compensation is required if the account has already been created or the event has been published.
     */
    private boolean shouldTriggerCompensation(SagaState saga) {
        return UserRegistrationStatus.ACCOUNT_CREATED.name().equals(saga.getStatus()) ||
               UserRegistrationStatus.EVENT_PUBLISHED.name().equals(saga.getStatus());
    }

    /**
     * Determine if rollback is required
     * Rollback is required if the account has already been soft deleted or the deletion event has been published.
     */
    private boolean shouldTriggerRollback(SagaState saga) {
        return UserDeletionStatus.ACCOUNT_SOFT_DELETED.name().equals(saga.getStatus()) ||
               UserDeletionStatus.DELETION_EVENT_PUBLISHED.name().equals(saga.getStatus());
    }

    /**
     * Execute compensation asynchronously
     */
    private void triggerCompensationAsync(String sagaId, String reason) {
        // In actual implementation, use @Async method for asynchronous execution
        try {
            eventPublishingService.compensateFailedSaga(sagaId, reason);
        } catch (Exception e) {
            log.error("Failed to trigger compensation for saga {}: {}", sagaId, e.getMessage(), e);
        }
    }

    /**
     * Execute rollback asynchronously
     */
    private void triggerRollbackAsync(String sagaId, String reason) {
        try {
            // User account recovery process
            rollbackUserDeletion(sagaId, reason);
        } catch (Exception e) {
            log.error("Failed to trigger rollback for saga {}: {}", sagaId, e.getMessage(), e);
        }
    }

    /**
     * Rollback process for user deletion
     * This method performs the actual rollback process, such as user account recovery, depending on business logic.
     */
    private void rollbackUserDeletion(String sagaId, String reason) {
        sagaStateRepository.findBySagaId(sagaId).ifPresent(saga -> {
            try {
                // Actual rollback process (e.g., user account recovery)
                // This part depends on specific business logic
                
                saga.setStatus(UserDeletionStatus.DELETION_ROLLED_BACK.name());
                saga.setSagaStatus(SagaStatus.SAGA_COMPENSATED);
                saga.setUpdatedAt(LocalDateTime.now());
                sagaStateRepository.save(saga);
                
                log.info("Rollback completed for saga: {}", sagaId);
                
            } catch (Exception e) {
                saga.setSagaStatus(SagaStatus.SAGA_COMPENSATION_FAILED);
                saga.setErrorReason("Rollback failed: " + e.getMessage());
                saga.setUpdatedAt(LocalDateTime.now());
                sagaStateRepository.save(saga);
                
                log.error("Rollback failed for saga {}: {}", sagaId, e.getMessage(), e);
            }
        });
    }

    /**
     * Extract event type from channel name
     * Channel format: "skishop:events:user_management_status"
     */
    private String extractEventTypeFromChannel(String channel) {
        String[] parts = channel.split(":");
        return parts.length >= 3 ? parts[2] : "unknown";
    }
    
    /**
     * Process status feedback from Azure Service Bus
     * Called from AzureServiceBusStatusFeedbackReceiver
     */
    public void processStatusFeedback(EventDto eventDto) {
        try {
            log.info("Processing status feedback event: eventType={}, sagaId={}", 
                eventDto.getEventType(), eventDto.getSagaId());
                
            if (eventDto.getPayload() != null) {
                String eventJson = objectMapper.writeValueAsString(eventDto.getPayload());
                handleUserManagementStatusEvent(eventJson);
            } else {
                log.warn("Received status feedback event with null payload: eventId={}", eventDto.getEventId());
            }
            
        } catch (Exception e) {
            log.error("Failed to process status feedback: eventId={}, error={}", 
                eventDto.getEventId(), e.getMessage(), e);
        }
    }
}
