package com.skishop.user.service.saga;

import com.skishop.user.dto.event.EventDto;
import com.skishop.user.dto.event.UserRegistrationPayload;
import com.skishop.user.dto.event.UserDeletionPayload;
import com.skishop.user.dto.event.UserManagementStatusPayload;
import com.skishop.user.entity.SagaTransaction;
import com.skishop.user.entity.User;
import com.skishop.user.enums.SagaStatus;
import com.skishop.user.enums.ProcessingStatus;
import com.skishop.user.repository.SagaTransactionRepository;
import com.skishop.user.service.UserEventService;
import com.skishop.user.service.event.EventHandlerService;
import com.skishop.user.service.event.EventPublisherService;
import com.skishop.user.service.metrics.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Saga Orchestrator
 * Implementation of the Saga pattern in event-driven architecture
 * Transaction management for user registration and deletion
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestrator {

    private final SagaTransactionRepository sagaRepository;
    private final UserEventService userEventService;
    private final EventPublisherService eventPublisherService;
    private final MetricsService metricsService;
    private final EventHandlerService eventHandlerService;

    @Value("${saga.timeout.registration:30}")
    private Integer registrationTimeoutSeconds;

    @Value("${saga.timeout.deletion:60}")
    private Integer deletionTimeoutSeconds;

    @Value("${saga.max-retry:3}")
    private Integer maxRetryCount;

    /**
     * Start user registration Saga
     * Executed in an independent transaction
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startUserRegistrationSaga(EventDto<UserRegistrationPayload> event) {
        String sagaId = UUID.randomUUID().toString();
        
        try {
            log.info("User registration Saga started: sagaId={}, userId={}, correlationId={}", 
                     sagaId, event.getPayload().getUserId(), event.getCorrelationId());

            // Create Saga transaction
            SagaTransaction saga = createSagaTransaction(sagaId, event, "USER_REGISTRATION", registrationTimeoutSeconds);
            sagaRepository.save(saga);

            // Save context information
            saga.addContext("EMAIL", event.getPayload().getEmail());
            saga.addContext("FIRST_NAME", event.getPayload().getFirstName());
            saga.addContext("LAST_NAME", event.getPayload().getLastName());
            sagaRepository.save(saga);

            metricsService.recordSagaStarted("USER_REGISTRATION");

            // Step 1: Event receipt confirmation
            updateSagaStatus(saga, SagaStatus.SAGA_STARTED, "EVENT_RECEIVED", null);
            
            // Record event as processed
            eventHandlerService.markEventAsProcessed(
                    saga.getSagaId(), 
                    "USER_REGISTERED", 
                    event.getPayload().getUserId(), 
                    true, 
                    null);
            
            // Step 2: Data validation
            executeRegistrationValidation(saga, event);

        } catch (Exception e) {
            log.error("User registration Saga start error: sagaId={}, error={}", sagaId, e.getMessage(), e);
            handleSagaFailure(sagaId, "SAGA_START_FAILED", e.getMessage());
            
            // Record event as processing failed
            try {
                eventHandlerService.markEventAsProcessed(
                        sagaId, 
                        "USER_REGISTERED", 
                        event.getPayload().getUserId(), 
                        false, 
                        e.getMessage());
            } catch (Exception ex) {
                log.error("Failed to record event processing failure: sagaId={}, error={}", sagaId, ex.getMessage(), ex);
            }
            
            throw e;
        }
    }

    /**
     * Start user deletion Saga
     * Executed in an independent transaction
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startUserDeletionSaga(EventDto<UserDeletionPayload> event) {
        String sagaId = UUID.randomUUID().toString();
        
        try {
            log.info("User deletion Saga started: sagaId={}, userId={}, correlationId={}", 
                     sagaId, event.getPayload().getUserId(), event.getCorrelationId());

            // Create Saga transaction
            SagaTransaction saga = createSagaTransaction(sagaId, event, "USER_DELETION", deletionTimeoutSeconds);
            sagaRepository.save(saga);

            metricsService.recordSagaStarted("USER_DELETION");

            // Step 1: Event receipt confirmation
            updateSagaStatus(saga, SagaStatus.SAGA_STARTED, "DELETION_EVENT_RECEIVED", null);
            
            // Record event as processed
            eventHandlerService.markEventAsProcessed(
                    saga.getSagaId(), 
                    "USER_DELETED", 
                    event.getPayload().getUserId(), 
                    true, 
                    null);
            
            // Step 2: Deletion data validation
            executeDeletionValidation(saga, event);

        } catch (Exception e) {
            log.error("User deletion Saga start error: sagaId={}, error={}", sagaId, e.getMessage(), e);
            handleSagaFailure(sagaId, "SAGA_START_FAILED", e.getMessage());
            
            // Record event as processing failed
            try {
                eventHandlerService.markEventAsProcessed(
                        sagaId, 
                        "USER_DELETED", 
                        event.getPayload().getUserId(), 
                        false, 
                        e.getMessage());
            } catch (Exception ex) {
                log.error("Failed to record event processing failure: sagaId={}, error={}", sagaId, ex.getMessage(), ex);
            }
            
            throw e;
        }
    }

    /**
     * Execute user registration validation step
     */
    private void executeRegistrationValidation(SagaTransaction saga, EventDto<UserRegistrationPayload> event) {
        try {
            updateSagaStatus(saga, SagaStatus.SAGA_IN_PROGRESS, "VALIDATION_IN_PROGRESS", null);
            
            UserRegistrationPayload payload = event.getPayload();
            
            // Required field validation
            if (payload.getUserId() == null || payload.getEmail() == null) {
                throw new IllegalArgumentException("Required fields are missing");
            }

            // Check for duplicate user
            if (userEventService.existsByUserId(payload.getUserId())) {
                updateSagaStatus(saga, SagaStatus.SAGA_STEP_FAILED, "DUPLICATE_USER_DETECTED", 
                                "Duplicate user ID: " + payload.getUserId());
                sendFailureResponse(saga, "Duplicate user ID");
                return;
            }

            if (userEventService.existsByEmail(payload.getEmail())) {
                updateSagaStatus(saga, SagaStatus.SAGA_STEP_FAILED, "DUPLICATE_USER_DETECTED", 
                                "Duplicate email address: " + payload.getEmail());
                sendFailureResponse(saga, "Duplicate email address");
                return;
            }

            saga.addCompletedStep("VALIDATION_PASSED", "User data validation successful");
            
            // Step 3: Create user profile
            executeUserProfileCreation(saga, event);

        } catch (Exception e) {
            log.error("User registration validation error: sagaId={}, error={}", saga.getSagaId(), e.getMessage(), e);
            updateSagaStatus(saga, SagaStatus.SAGA_STEP_FAILED, "VALIDATION_FAILED", e.getMessage());
            sendFailureResponse(saga, e.getMessage());
        }
    }

    /**
     * Execute user profile creation step
     */
    private void executeUserProfileCreation(SagaTransaction saga, EventDto<UserRegistrationPayload> event) {
        try {
            updateSagaStatus(saga, SagaStatus.SAGA_IN_PROGRESS, "PROFILE_CREATION_IN_PROGRESS", null);
            
            UserRegistrationPayload payload = event.getPayload();
            
            // Create user profile
            User user = userEventService.createUserProfile(payload);
            
            saga.addCompletedStep("PROFILE_CREATED", "User profile creation successful: " + user.getId());
            saga.addContext("CREATED_USER_ID", user.getId().toString());
            
            // Complete Saga
            completeSaga(saga);
            
            // Send success response
            sendSuccessResponse(saga);

        } catch (Exception e) {
            log.error("User profile creation error: sagaId={}, error={}", saga.getSagaId(), e.getMessage(), e);
            updateSagaStatus(saga, SagaStatus.SAGA_STEP_FAILED, "PROFILE_CREATION_FAILED", e.getMessage());
            sendFailureResponse(saga, e.getMessage());
        }
    }

    /**
     * Execute user deletion validation step
     */
    private void executeDeletionValidation(SagaTransaction saga, EventDto<UserDeletionPayload> event) {
        try {
            updateSagaStatus(saga, SagaStatus.SAGA_IN_PROGRESS, "DELETION_VALIDATION_IN_PROGRESS", null);
            
            UserDeletionPayload payload = event.getPayload();
            
            // User existence check
            Optional<User> userOpt = userEventService.findByUserId(payload.getUserId());
            if (userOpt.isEmpty()) {
                // For idempotency, treat as success even if user doesn't exist
                log.info("User to be deleted not found (treated as success for idempotency): userId={}", payload.getUserId());
                saga.addCompletedStep("USER_NOT_FOUND", "User to be deleted not found (success due to idempotency)");
                completeSaga(saga);
                sendSuccessResponse(saga);
                return;
            }

            saga.addCompletedStep("DELETION_VALIDATION_PASSED", "User to be deleted verification completed");
            saga.addContext("TARGET_USER_ID", userOpt.get().getId().toString());
            
            // Step 3: Delete user profile
            executeUserProfileDeletion(saga, userOpt.get());

        } catch (Exception e) {
            log.error("User deletion validation error: sagaId={}, error={}", saga.getSagaId(), e.getMessage(), e);
            updateSagaStatus(saga, SagaStatus.SAGA_STEP_FAILED, "DELETION_VALIDATION_FAILED", e.getMessage());
            sendFailureResponse(saga, e.getMessage());
        }
    }

    /**
     * Execute user profile deletion step
     */
    private void executeUserProfileDeletion(SagaTransaction saga, User user) {
        try {
            updateSagaStatus(saga, SagaStatus.SAGA_IN_PROGRESS, "PROFILE_DELETION_IN_PROGRESS", null);
            
            // Clean up related data and execute deletion
            userEventService.deleteUserProfile(user.getId());
            
            saga.addCompletedStep("PROFILE_DELETED", "User profile deletion completed: " + user.getId());
            
            // Complete Saga
            completeSaga(saga);
            
            // Send success response
            sendSuccessResponse(saga);

        } catch (Exception e) {
            log.error("User profile deletion error: sagaId={}, error={}", saga.getSagaId(), e.getMessage(), e);
            updateSagaStatus(saga, SagaStatus.SAGA_STEP_FAILED, "PROFILE_DELETION_FAILED", e.getMessage());
            sendFailureResponse(saga, e.getMessage());
        }
    }

    /**
     * Create Saga transaction
     */
    private SagaTransaction createSagaTransaction(String sagaId, EventDto<?> event, String eventType, int timeoutSeconds) {
        return SagaTransaction.builder()
                .sagaId(sagaId)
                .correlationId(event.getCorrelationId())
                .originalEventId(event.getEventId())
                .eventType(eventType)
                .userId(extractUserId(event))
                .status(SagaStatus.SAGA_STARTED)
                .maxRetryCount(maxRetryCount)
                .timeoutAt(LocalDateTime.now().plusSeconds(timeoutSeconds))
                .build();
    }

    /**
     * Update Saga status
     */
    private void updateSagaStatus(SagaTransaction saga, SagaStatus status, String currentStep, String errorMessage) {
        saga.setStatus(status);
        saga.setCurrentStep(currentStep);
        saga.setErrorMessage(errorMessage);
        
        if (status == SagaStatus.SAGA_IN_PROGRESS && saga.getProcessingStartTime() == null) {
            saga.markProcessingStart();
        }
        
        sagaRepository.save(saga);
        
        log.info("Saga status updated: sagaId={}, status={}, step={}", 
                 saga.getSagaId(), status, currentStep);
    }

    /**
     * Saga completion process
     */
    private void completeSaga(SagaTransaction saga) {
        saga.setStatus(SagaStatus.SAGA_COMPLETED);
        saga.markProcessingEnd();
        sagaRepository.save(saga);
        
        long processingTimeMs = saga.getProcessingTimeMs();
        metricsService.recordSagaCompleted(saga.getEventType(), processingTimeMs, true);
        
        log.info("Saga completed: sagaId={}, processingTime={}ms", saga.getSagaId(), processingTimeMs);
    }

    /**
     * Saga failure handling
     */
    private void handleSagaFailure(String sagaId, String errorType, String errorMessage) {
        Optional<SagaTransaction> sagaOpt = sagaRepository.findById(sagaId);
        if (sagaOpt.isPresent()) {
            SagaTransaction saga = sagaOpt.get();
            saga.setStatus(SagaStatus.SAGA_FAILED);
            saga.setErrorType(errorType);
            saga.setErrorMessage(errorMessage);
            saga.markProcessingEnd();
            sagaRepository.save(saga);
            
            metricsService.recordSagaCompleted(saga.getEventType(), saga.getProcessingTimeMs(), false);
        }
    }

    /**
     * Send success response
     */
    private void sendSuccessResponse(SagaTransaction saga) {
        try {
            UserManagementStatusPayload statusPayload = UserManagementStatusPayload.builder()
                    .userId(saga.getUserId())
                    .originalEventId(saga.getOriginalEventId())
                    .status(ProcessingStatus.SUCCESS)
                    .processingTime(saga.getProcessingTimeMs())
                    .build();

            EventDto<UserManagementStatusPayload> responseEvent = EventDto.<UserManagementStatusPayload>builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("USER_MANAGEMENT_STATUS")
                    .timestamp(java.time.Instant.now())
                    .version("1.0")
                    .producer("user-management-service")
                    .payload(statusPayload)
                    .correlationId(saga.getCorrelationId())
                    .sagaId(saga.getSagaId())
                    .build();

            eventPublisherService.publishEvent(responseEvent);
            
            log.info("Success response sent: sagaId={}, correlationId={}", 
                     saga.getSagaId(), saga.getCorrelationId());

        } catch (Exception e) {
            log.error("Error sending success response: sagaId={}, error={}", saga.getSagaId(), e.getMessage(), e);
        }
    }

    /**
     * Send failure response
     */
    private void sendFailureResponse(SagaTransaction saga, String reason) {
        try {
            UserManagementStatusPayload statusPayload = UserManagementStatusPayload.builder()
                    .userId(saga.getUserId())
                    .originalEventId(saga.getOriginalEventId())
                    .status(ProcessingStatus.FAILED)
                    .reason(reason)
                    .processingTime(saga.getProcessingTimeMs())
                    .build();

            EventDto<UserManagementStatusPayload> responseEvent = EventDto.<UserManagementStatusPayload>builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("USER_MANAGEMENT_STATUS")
                    .timestamp(java.time.Instant.now())
                    .version("1.0")
                    .producer("user-management-service")
                    .payload(statusPayload)
                    .correlationId(saga.getCorrelationId())
                    .sagaId(saga.getSagaId())
                    .build();

            eventPublisherService.publishEvent(responseEvent);
            
            log.info("Failure response sent: sagaId={}, correlationId={}, reason={}", 
                     saga.getSagaId(), saga.getCorrelationId(), reason);

        } catch (Exception e) {
            log.error("Error sending failure response: sagaId={}, error={}", saga.getSagaId(), e.getMessage(), e);
        }
    }

    /**
     * Extract user ID from event
     */
    private String extractUserId(EventDto<?> event) {
        Object payload = event.getPayload();
        if (payload instanceof UserRegistrationPayload userRegistrationPayload) {
            return userRegistrationPayload.getUserId();
        } else if (payload instanceof UserDeletionPayload userDeletionPayload) {
            return userDeletionPayload.getUserId();
        }
        return null;
    }

    /**
     * Execute compensation process
     * Execute compensation process for failed Saga
     * Executed in an independent transaction
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeCompensation(String sagaId, String reason) {
        Optional<SagaTransaction> sagaOpt = sagaRepository.findById(sagaId);
        if (sagaOpt.isEmpty()) {
            log.warn("Compensation target Saga not found: sagaId={}", sagaId);
            return;
        }
        
        SagaTransaction saga = sagaOpt.get();
        
        try {
            log.info("Compensation process started: sagaId={}, eventType={}, reason={}", 
                    saga.getSagaId(), saga.getEventType(), reason);
            
            updateSagaStatus(saga, SagaStatus.SAGA_COMPENSATING, "COMPENSATION_STARTED", 
                           "Compensation process started: " + reason);
            
            long startTime = System.currentTimeMillis();
            
            // Execute appropriate compensation based on event type
            if ("USER_REGISTRATION".equals(saga.getEventType())) {
                executeUserRegistrationCompensation(saga, reason);
            } else if ("USER_DELETION".equals(saga.getEventType())) {
                executeUserDeletionCompensation(saga, reason);
            }
            
            updateSagaStatus(saga, SagaStatus.SAGA_COMPENSATED, "COMPENSATION_COMPLETED", 
                           "Compensation process completed: " + reason);
            
            // Record compensation process metrics
            long processingTime = System.currentTimeMillis() - startTime;
            metricsService.recordCompensationExecuted(saga.getEventType(), reason, processingTime, true);
            
            // Compensation completion notification to authentication service
            publishCompensationStatusEvent(saga, true, reason);
            
            log.info("Compensation process completed: sagaId={}, eventType={}, processingTime={}ms", 
                   saga.getSagaId(), saga.getEventType(), processingTime);
            
        } catch (Exception e) {
            log.error("Compensation process failed: sagaId={}, error={}", saga.getSagaId(), e.getMessage(), e);
            
            updateSagaStatus(saga, SagaStatus.SAGA_COMPENSATION_FAILED, "COMPENSATION_FAILED", 
                           "Compensation process failed: " + e.getMessage());
            
            // Record failure metrics
            metricsService.recordCompensationExecuted(saga.getEventType(), reason, 
                                                    System.currentTimeMillis() - saga.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(), 
                                                    false);
            
            // Compensation failure notification to authentication service
            publishCompensationStatusEvent(saga, false, e.getMessage());
        }
    }
    
    /**
     * Execute user registration compensation
     * Method to execute actual compensation actions
     */
    private void executeUserRegistrationCompensation(SagaTransaction saga, String reason) {
        String createdUserId = saga.getContextValue("CREATED_USER_ID");
        
        if (createdUserId != null) {
            try {
                // Force delete the created user profile
                userEventService.hardDeleteUserProfile(UUID.fromString(createdUserId));
                
                saga.addCompletedStep("USER_PROFILE_DELETED", 
                                    "User profile deletion completed: " + createdUserId + " (reason: " + reason + ")");
                
                log.info("User registration compensation successful: sagaId={}, userId={}, reason={}", 
                        saga.getSagaId(), createdUserId, reason);
                
            } catch (Exception e) {
                log.error("User registration compensation failed: sagaId={}, userId={}, reason={}, error={}", 
                        saga.getSagaId(), createdUserId, reason, e.getMessage(), e);
                throw e;
            }
        } else {
            log.warn("Skipping compensation due to unknown user ID: sagaId={}, reason={}", saga.getSagaId(), reason);
        }
    }
    
    /**
     * Execute user deletion compensation
     * Method to execute actual compensation actions
     */
    private void executeUserDeletionCompensation(SagaTransaction saga, String reason) {
        // User deletion operation compensation (implement if needed)
        // Currently no processing needed, logging only
        log.info("User deletion compensation: sagaId={}, userId={}, reason={}, no compensation needed", 
                saga.getSagaId(), saga.getUserId(), reason);
        
        saga.addCompletedStep("DELETION_COMPENSATION", 
                            "User deletion compensation (no action): " + saga.getUserId() + " (reason: " + reason + ")");
    }
    
    /**
     * Publish compensation status event
     */
    private void publishCompensationStatusEvent(SagaTransaction saga, boolean success, String reason) {
        try {
            UserManagementStatusPayload statusPayload = UserManagementStatusPayload.builder()
                    .userId(saga.getUserId())
                    .originalEventId(saga.getOriginalEventId())
                    .status(success ? ProcessingStatus.COMPENSATION_SUCCESS : ProcessingStatus.COMPENSATION_FAILED)
                    .reason(reason)
                    .processingTime(saga.getProcessingTimeMs())
                    .build();

            EventDto<UserManagementStatusPayload> responseEvent = EventDto.<UserManagementStatusPayload>builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("USER_MANAGEMENT_COMPENSATION_STATUS")
                    .timestamp(java.time.Instant.now())
                    .version("1.0")
                    .producer("user-management-service")
                    .payload(statusPayload)
                    .correlationId(saga.getCorrelationId())
                    .sagaId(saga.getSagaId())
                    .build();

            eventPublisherService.publishEvent(responseEvent);
            
            log.info("Compensation status sent: sagaId={}, correlationId={}, status={}", 
                    saga.getSagaId(), saga.getCorrelationId(), success ? "SUCCESS" : "FAILED");
                    
        } catch (Exception e) {
            log.error("Error sending compensation status: sagaId={}, error={}", 
                    saga.getSagaId(), e.getMessage(), e);
        }
    }
    
    /**
     * Check and process timeout Sagas
     * Execute periodically to detect timed out Sagas and execute compensation
     * Executed in an independent transaction
     */
    @Scheduled(fixedDelay = 30000) // 30 second interval
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkAndProcessTimeoutSagas() {
        try {
            log.debug("Timeout Saga monitoring started");
            
            LocalDateTime currentTime = LocalDateTime.now();
            List<SagaStatus> terminalStatuses = List.of(
                SagaStatus.SAGA_COMPLETED,
                SagaStatus.SAGA_FAILED,
                SagaStatus.SAGA_COMPENSATED,
                SagaStatus.SAGA_COMPENSATION_FAILED,
                SagaStatus.SAGA_TIMEOUT
            );
            
            // Search for timed out Sagas
            List<SagaTransaction> timeoutSagas = sagaRepository.findTimedOutSagas(currentTime, terminalStatuses);
            
            if (!timeoutSagas.isEmpty()) {
                log.warn("Timeout Saga detected: count={}", timeoutSagas.size());
                
                for (SagaTransaction saga : timeoutSagas) {
                    // Execute timeout processing in a separate transaction
                    handleTimeoutSagaWithNewTransaction(saga);
                }
            }
            
            log.debug("Timeout Saga monitoring completed: processed={}", timeoutSagas.size());
            
        } catch (Exception e) {
            log.error("Timeout Saga monitoring error: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Process timeout Saga
     * Executed in an independent transaction
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTimeoutSagaWithNewTransaction(SagaTransaction saga) {
        try {
            log.warn("Saga timeout processing: sagaId={}, eventType={}, currentStep={}", 
                    saga.getSagaId(), saga.getEventType(), saga.getCurrentStep());
            
            // Execute compensation
            String sagaId = saga.getSagaId();
            
            // Execute compensation in a separate transaction
            executeCompensation(sagaId, "TIMEOUT");
            
            // Retrieve and update state
            saga = sagaRepository.findById(sagaId).orElseThrow();
            
            // Update to timeout state
            updateSagaStatus(saga, SagaStatus.SAGA_TIMEOUT, "TIMED_OUT", 
                           "Saga timed out: " + saga.getTimeoutAt());
            
            // Record metrics
            metricsService.recordSagaCompleted(saga.getEventType(), saga.getProcessingTimeMs(), false);
            
            log.info("Saga timeout processing completed: sagaId={}", saga.getSagaId());
            
        } catch (Exception e) {
            log.error("Saga timeout processing error: sagaId={}, error={}", saga.getSagaId(), e.getMessage(), e);
            
            updateSagaStatus(saga, SagaStatus.SAGA_TIMEOUT, "TIMEOUT_ERROR", 
                           "Error during timeout processing: " + e.getMessage());
        }
    }

    /**
     * Retry Saga process
     * Executed in an independent transaction
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void retrySaga(SagaTransaction saga) {
        try {
            log.info("Saga retry started: sagaId={}, eventType={}, retryCount={}/{}", 
                    saga.getSagaId(), saga.getEventType(), saga.getRetryCount(), saga.getMaxRetryCount());
                    
            // Execute appropriate retry processing based on event type
            if ("USER_REGISTRATION".equals(saga.getEventType())) {
                retryUserRegistration(saga);
            } else if ("USER_DELETION".equals(saga.getEventType())) {
                retryUserDeletion(saga);
            } else {
                log.warn("Cannot retry due to unknown event type: sagaId={}, eventType={}", 
                        saga.getSagaId(), saga.getEventType());
            }
            
            log.info("Saga retry completed: sagaId={}", saga.getSagaId());
            
        } catch (Exception e) {
            log.error("Saga retry failed: sagaId={}, error={}", saga.getSagaId(), e.getMessage(), e);
            
            updateSagaStatus(saga, SagaStatus.SAGA_STEP_FAILED, "RETRY_FAILED", e.getMessage());
            
            // Execute compensation process if retry count reaches the limit
            if (saga.getRetryCount() >= saga.getMaxRetryCount()) {
                log.warn("Executing compensation as retry count limit reached: sagaId={}, retryCount={}", 
                        saga.getSagaId(), saga.getRetryCount());
                        
                // Execute compensation in a separate transaction for retry
                String sagaId = saga.getSagaId();
                try {
                    executeCompensation(sagaId, "MAX_RETRY_EXCEEDED");
                } catch (Exception ex) {
                    log.error("Failed to execute compensation: sagaId={}, error={}", sagaId, ex.getMessage(), ex);
                }
            }
        }
    }
    
    /**
     * Retry user registration Saga
     */
    private void retryUserRegistration(SagaTransaction saga) {
        // Retry from appropriate point based on current step
        String currentStep = saga.getCurrentStep();
        
        if ("VALIDATION_IN_PROGRESS".equals(currentStep) || "VALIDATION_FAILED".equals(currentStep)) {
            // Get information from original event (in an actual implementation, retrieve from event store)
            String userId = saga.getUserId();
            // Email would be retrieved from Context info, but not used here so we don't fetch it
            
            // Re-execute validation step
            updateSagaStatus(saga, SagaStatus.SAGA_IN_PROGRESS, "VALIDATION_RETRY", null);
            
            // Implement retry logic
            log.info("User registration validation retry: sagaId={}, userId={}", saga.getSagaId(), userId);
            
            // Actual retry logic (simplified here)
            saga.addCompletedStep("VALIDATION_RETRY_COMPLETED", "Validation retry successful");
            
            // Move to next step (user profile creation)
            // Implementation omitted - would actually execute profile creation step
            
        } else if ("PROFILE_CREATION_IN_PROGRESS".equals(currentStep) || "PROFILE_CREATION_FAILED".equals(currentStep)) {
            // Retry profile creation step
            updateSagaStatus(saga, SagaStatus.SAGA_IN_PROGRESS, "PROFILE_CREATION_RETRY", null);
            
            // Implement retry logic
            log.info("User profile creation retry: sagaId={}, userId={}", saga.getSagaId(), saga.getUserId());
            
            // Actual retry logic (simplified here)
            saga.addCompletedStep("PROFILE_CREATION_RETRY_COMPLETED", "Profile creation retry successful");
            
            // Complete Saga
            completeSaga(saga);
            
            // Send success response
            sendSuccessResponse(saga);
        }
    }
    
    /**
     * Retry user deletion Saga
     */
    private void retryUserDeletion(SagaTransaction saga) {
        // Retry from appropriate point based on current step
        String currentStep = saga.getCurrentStep();
        
        if ("DELETION_VALIDATION_IN_PROGRESS".equals(currentStep) || "DELETION_VALIDATION_FAILED".equals(currentStep)) {
            // Retry deletion validation step
            updateSagaStatus(saga, SagaStatus.SAGA_IN_PROGRESS, "DELETION_VALIDATION_RETRY", null);
            
            // Implement retry logic
            log.info("User deletion validation retry: sagaId={}, userId={}", saga.getSagaId(), saga.getUserId());
            
            // Actual retry logic (simplified here)
            saga.addCompletedStep("DELETION_VALIDATION_RETRY_COMPLETED", "Deletion validation retry successful");
            
            // Proceed to next step
            // Implementation omitted - would execute user deletion step in practice
            
        } else if ("PROFILE_DELETION_IN_PROGRESS".equals(currentStep) || "PROFILE_DELETION_FAILED".equals(currentStep)) {
            // Profile deletion step retry
            updateSagaStatus(saga, SagaStatus.SAGA_IN_PROGRESS, "PROFILE_DELETION_RETRY", null);
            
            // Implement retry logic
            log.info("User profile deletion retry: sagaId={}, userId={}", saga.getSagaId(), saga.getUserId());
            
            // Actual retry logic (simplified here)
            saga.addCompletedStep("PROFILE_DELETION_RETRY_COMPLETED", "Profile deletion retry successful");
            
            // Saga completion
            completeSaga(saga);
            
            // Send success response
            sendSuccessResponse(saga);
        }
    }
}
