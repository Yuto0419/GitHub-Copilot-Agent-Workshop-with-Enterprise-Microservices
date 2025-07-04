package com.skishop.auth.service.scheduler;

import com.skishop.auth.service.compensation.CompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduler for monitoring and cleaning up Saga timeouts
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    value = "skishop.runtime.event-propagation-enabled", 
    havingValue = "true", 
    matchIfMissing = false
)
public class SagaTimeoutScheduler {

    private final CompensationService compensationService;

    /**
     * Check for timed out Sagas every 30 seconds
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 60000)
    public void checkSagaTimeouts() {
        try {
            log.debug("Checking for timed out sagas...");
            compensationService.checkAndHandleTimeouts();
        } catch (Exception e) {
            log.error("Error during saga timeout check: {}", e.getMessage(), e);
        }
    }

    /**
     * Check Saga health every 5 minutes
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 300000)
    public void performSagaHealthCheck() {
        try {
            log.debug("Performing saga health check...");
            // Implement additional health check logic here
            // e.g., Sagas stuck for a long time, abnormal state Sagas, etc.
        } catch (Exception e) {
            log.error("Error during saga health check: {}", e.getMessage(), e);
        }
    }
}
