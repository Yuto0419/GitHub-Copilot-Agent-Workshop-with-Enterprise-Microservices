package com.skishop.auth.service.compensation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skishop.auth.entity.SagaState;
import com.skishop.auth.enums.SagaStatus;
import com.skishop.auth.repository.SagaStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service to manage and execute compensation processes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompensationService {

    private final SagaStateRepository sagaStateRepository;
    private final List<CompensationAction> compensationActions;
    private final ObjectMapper objectMapper;

    /**
     * Execute compensation process for the specified Saga
     */
    @Transactional
    public boolean executeCompensation(String sagaId, String failureReason) {
        try {
            Optional<SagaState> sagaOpt = sagaStateRepository.findBySagaId(sagaId);
            
            if (sagaOpt.isEmpty()) {
                log.error("Saga not found for compensation: {}", sagaId);
                return false;
            }

            SagaState saga = sagaOpt.get();
            
            // Record the start of compensation process
            saga.setSagaStatus(SagaStatus.SAGA_COMPENSATING);
            saga.setErrorReason(failureReason);
            saga.setUpdatedAt(LocalDateTime.now());
            sagaStateRepository.save(saga);

            log.info("Starting compensation for saga: {} (type: {}, status: {})", 
                sagaId, saga.getSagaType(), saga.getStatus());

            // Build compensation context
            CompensationContext context = buildCompensationContext(saga, failureReason);

            // Find and execute applicable compensation actions
            boolean allSuccessful = executeApplicableCompensations(saga, context);

            // Record the result of compensation process
            if (allSuccessful) {
                saga.setSagaStatus(SagaStatus.SAGA_COMPENSATED);
                saga.setEndTime(Instant.now());
                log.info("Compensation completed successfully for saga: {}", sagaId);
            } else {
                saga.setSagaStatus(SagaStatus.SAGA_COMPENSATION_FAILED);
                log.error("Compensation failed for saga: {}", sagaId);
            }

            saga.setUpdatedAt(LocalDateTime.now());
            sagaStateRepository.save(saga);

            return allSuccessful;

        } catch (Exception e) {
            log.error("Failed to execute compensation for saga {}: {}", sagaId, e.getMessage(), e);
            
            // Record compensation failure
            markCompensationFailed(sagaId, e.getMessage());
            return false;
        }
    }

    /**
     * Build compensation context
     */
    private CompensationContext buildCompensationContext(SagaState saga, String failureReason) {
        Map<String, Object> sagaData = new HashMap<>();
        
        try {
            if (saga.getData() != null) {
                sagaData = objectMapper.readValue(saga.getData(), new TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception e) {
            log.warn("Failed to parse saga data for compensation: {}", e.getMessage());
        }

        return CompensationContext.builder()
            .sagaId(saga.getSagaId())
            .sagaType(saga.getSagaType())
            .userId(saga.getUserId())
            .failureReason(failureReason)
            .currentStatus(saga.getStatus())
            .sagaData(sagaData)
            .errorTimestamp(System.currentTimeMillis())
            .retryCount(saga.getRetryCount())
            .build();
    }

    /**
     * Execute applicable compensation actions
     */
    private boolean executeApplicableCompensations(SagaState saga, CompensationContext context) {
        // Sort by priority
        List<CompensationAction> applicableActions = compensationActions.stream()
            .filter(action -> action.isApplicable(saga.getSagaType(), saga.getStatus()))
            .sorted((a, b) -> Integer.compare(a.getPriority(), b.getPriority()))
            .toList();

        if (applicableActions.isEmpty()) {
            log.warn("No applicable compensation actions found for saga: {} (type: {}, status: {})", 
                saga.getSagaId(), saga.getSagaType(), saga.getStatus());
            return true; // No compensation needed
        }

        boolean allSuccessful = true;

        for (CompensationAction action : applicableActions) {
            try {
                log.info("Executing compensation action: {} for saga: {}", 
                    action.getActionName(), saga.getSagaId());

                boolean success = action.compensate(saga.getSagaId(), context);
                
                if (success) {
                    log.info("Compensation action {} completed successfully for saga: {}", 
                        action.getActionName(), saga.getSagaId());
                } else {
                    log.error("Compensation action {} failed for saga: {}", 
                        action.getActionName(), saga.getSagaId());
                    allSuccessful = false;
                }

            } catch (Exception e) {
                log.error("Exception in compensation action {} for saga {}: {}", 
                    action.getActionName(), saga.getSagaId(), e.getMessage(), e);
                allSuccessful = false;
            }
        }

        return allSuccessful;
    }

    /**
     * Record compensation failure
     */
    private void markCompensationFailed(String sagaId, String errorMessage) {
        try {
            sagaStateRepository.findBySagaId(sagaId).ifPresent(saga -> {
                saga.setSagaStatus(SagaStatus.SAGA_COMPENSATION_FAILED);
                saga.setErrorReason("Compensation execution failed: " + errorMessage);
                saga.setUpdatedAt(LocalDateTime.now());
                sagaStateRepository.save(saga);
            });
        } catch (Exception e) {
            log.error("Failed to mark compensation as failed for saga {}: {}", sagaId, e.getMessage());
        }
    }

    /**
     * Determine if compensation is required
     */
    public boolean isCompensationRequired(String sagaType, String status) {
        return compensationActions.stream()
            .anyMatch(action -> action.isApplicable(sagaType, status));
    }

    /**
     * Check Saga timeouts and execute compensation if necessary
     */
    @Transactional
    public void checkAndHandleTimeouts() {
        try {
            Instant now = Instant.now();
            
            // Find timed out Sagas
            List<SagaState> timedOutSagas = sagaStateRepository.findBySagaStatusAndTimeoutAtBefore(
                SagaStatus.SAGA_IN_PROGRESS, now);
            
            for (SagaState saga : timedOutSagas) {
                log.warn("Saga timeout detected: {} (started: {}, timeout: {})", 
                    saga.getSagaId(), saga.getStartTime(), saga.getTimeoutAt());
                
                // Update to timeout status
                saga.setSagaStatus(SagaStatus.SAGA_TIMEOUT);
                saga.setErrorReason("Saga execution timeout");
                saga.setEndTime(now);
                saga.setUpdatedAt(LocalDateTime.now());
                sagaStateRepository.save(saga);
                
                // Check if compensation is required
                if (isCompensationRequired(saga.getSagaType(), saga.getStatus())) {
                    executeCompensation(saga.getSagaId(), "Saga timeout");
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to check and handle timeouts: {}", e.getMessage(), e);
        }
    }
}
