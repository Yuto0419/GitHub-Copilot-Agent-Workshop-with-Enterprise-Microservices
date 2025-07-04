package com.skishop.user.service.saga;

import com.skishop.user.entity.SagaTransaction;
import com.skishop.user.enums.SagaStatus;
import com.skishop.user.repository.SagaTransactionRepository;
import com.skishop.user.service.metrics.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Saga timeout monitoring service
 * Periodically detects timed-out Sagas and executes appropriate processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SagaTimeoutMonitoringService {

    private final SagaTransactionRepository sagaRepository;
    private final MetricsService metricsService;
    private final SagaOrchestrator sagaOrchestrator;

    /**
     * Monitor timeout Sagas and process them
     * Executed every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void monitorTimeouts() {
        try {
            log.debug("Starting Saga timeout monitoring");
            
            // Actual processing is delegated to SagaOrchestrator's checkAndProcessTimeoutSagas()
            // Since this method starts an independent transaction, only call it here
            sagaOrchestrator.checkAndProcessTimeoutSagas();
            
            log.debug("Saga timeout monitoring completed");
            
        } catch (Exception e) {
            log.error("Saga timeout monitoring error", e);
        }
    }

    /**
     * Monitor and retry retryable Sagas
     * Executed every 10 minutes
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    @Transactional
    public void monitorRetryableSagas() {
        try {
            log.debug("Starting retryable Saga monitoring");
            
            LocalDateTime currentTime = LocalDateTime.now();
            List<SagaTransaction> retryableSagas = sagaRepository.findRetryableSagas(
                SagaStatus.SAGA_STEP_FAILED, currentTime);
            
            if (!retryableSagas.isEmpty()) {
                log.info("Retryable Saga detected: count={}", retryableSagas.size());
                
                for (SagaTransaction saga : retryableSagas) {
                    handleRetryableSaga(saga);
                }
            }
            
            log.debug("Retryable Saga monitoring completed: processed={}", retryableSagas.size());
            
        } catch (Exception e) {
            log.error("Retryable Saga monitoring error", e);
        }
    }

    /**
     * Cleanup old Saga transactions
     * Executed once daily
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    @Transactional
    public void cleanupOldSagas() {
        try {
            log.info("Starting old Saga cleanup");
            
            // Delete completed Sagas older than 30 days
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(30);
            List<SagaStatus> terminalStatuses = List.of(
                SagaStatus.SAGA_COMPLETED,
                SagaStatus.SAGA_FAILED,
                SagaStatus.SAGA_COMPENSATION_FAILED,
                SagaStatus.SAGA_TIMEOUT
            );

            List<SagaTransaction> oldSagas = sagaRepository.findOldCompletedSagas(cutoffTime, terminalStatuses);
            
            if (!oldSagas.isEmpty()) {
                log.info("Old Saga deletion candidates: count={}", oldSagas.size());
                sagaRepository.deleteAll(oldSagas);
                log.info("Old Saga deletion completed: count={}", oldSagas.size());
            }
            
        } catch (Exception e) {
            log.error("Old Saga cleanup error", e);
        }
    }

    /**
     * Update Saga metrics
     * Executed every minute
     */
    @Scheduled(fixedRate = 60000) // 1 minute
    public void updateSagaMetrics() {
        try {
            List<SagaStatus> activeStatuses = List.of(
                SagaStatus.SAGA_STARTED,
                SagaStatus.SAGA_IN_PROGRESS,
                SagaStatus.SAGA_COMPENSATING
            );

            Long activeSagaCount = sagaRepository.countActiveSagas(activeStatuses);
            
            // Update metrics (directly update AtomicLong value)
            long currentActive = metricsService.getActiveSagaCount();
            if (currentActive != activeSagaCount) {
                log.debug("Active Saga count updated: {} -> {}", currentActive, activeSagaCount);
            }
            
        } catch (Exception e) {
            log.error("Saga metrics update error", e);
        }
    }

    /**
     * Process timed-out Saga
     */
    private void handleTimeoutSaga(SagaTransaction saga) {
        try {
            log.warn("Saga timeout processing: sagaId={}, eventType={}, currentStep={}", 
                     saga.getSagaId(), saga.getEventType(), saga.getCurrentStep());

            saga.setStatus(SagaStatus.SAGA_TIMEOUT);
            saga.setErrorType("TIMEOUT");
            saga.setErrorMessage("Saga timeout: " + saga.getCurrentStep());
            saga.markProcessingEnd();
            
            sagaRepository.save(saga);

            // Record metrics
            metricsService.recordSagaCompleted(saga.getEventType(), saga.getProcessingTimeMs(), false);
            
            log.info("Saga timeout processing completed: sagaId={}", saga.getSagaId());

        } catch (Exception e) {
            log.error("Saga timeout processing error: sagaId={}", saga.getSagaId(), e);
        }
    }

    /**
     * Process retryable Saga
     */
    private void handleRetryableSaga(SagaTransaction saga) {
        try {
            if (!saga.canRetry()) {
                log.warn("Saga retry not possible: sagaId={}, retryCount={}/{}", 
                         saga.getSagaId(), saga.getRetryCount(), saga.getMaxRetryCount());
                return;
            }

            log.info("Saga retry execution: sagaId={}, retryCount={}/{}", 
                     saga.getSagaId(), saga.getRetryCount() + 1, saga.getMaxRetryCount());

            saga.incrementRetryCount();
            saga.setStatus(SagaStatus.SAGA_IN_PROGRESS);
            saga.setErrorMessage(null);
            saga.setErrorType(null);
            
            sagaRepository.save(saga);

            // Delegate retry processing to SagaOrchestrator
            sagaOrchestrator.retrySaga(saga);
            
            log.info("Saga retry setup completed: sagaId={}", saga.getSagaId());

        } catch (Exception e) {
            log.error("Saga retry processing error: sagaId={}", saga.getSagaId(), e);
        }
    }

    /**
     * Output Saga statistics information
     * Executed every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void logSagaStatistics() {
        try {
            List<Object[]> statistics = sagaRepository.findStatisticsByEventTypeAndStatus();
            
            log.info("=== Saga Statistics ===");
            for (Object[] stat : statistics) {
                String eventType = (String) stat[0];
                SagaStatus status = (SagaStatus) stat[1];
                Long count = (Long) stat[2];
                
                log.info("EventType: {}, Status: {}, Count: {}", eventType, status, count);
            }
            log.info("==================");
            
        } catch (Exception e) {
            log.error("Saga statistics output error", e);
        }
    }
}
