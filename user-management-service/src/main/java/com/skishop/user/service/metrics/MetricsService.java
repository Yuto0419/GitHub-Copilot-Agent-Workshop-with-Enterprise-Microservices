package com.skishop.user.service.metrics;

import io.micrometer.core.instrument.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Comprehensive metrics collection service
 * Monitors overall system performance and health
 */
@Component
@Slf4j
public class MetricsService {
    
    private final MeterRegistry meterRegistry;
    
    // Timers
    private final Timer eventPublishingTimer;
    private final Timer eventProcessingTimer;
    private final Timer sagaExecutionTimer;
    private final Timer databaseOperationTimer;
    
    // Gauges
    private final AtomicLong activeSagaCount = new AtomicLong(0);
    private final AtomicLong pendingEventCount = new AtomicLong(0);
    private final AtomicLong failedEventCount = new AtomicLong(0);
    
    // Distribution summaries
    private final DistributionSummary payloadSizeDistribution;
    private final DistributionSummary sagaDurationDistribution;
    
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize timers
        this.eventPublishingTimer = Timer.builder("events.publishing.duration")
            .description("Time taken for event publishing")
            .register(meterRegistry);
            
        this.eventProcessingTimer = Timer.builder("events.processing.duration")
            .description("Time taken for event processing")
            .register(meterRegistry);
            
        this.sagaExecutionTimer = Timer.builder("saga.execution.duration")
            .description("Saga execution time")
            .register(meterRegistry);
            
        this.databaseOperationTimer = Timer.builder("database.operation.duration")
            .description("Database operation time")
            .register(meterRegistry);
        
        // Register gauges
        Gauge.builder("saga.active.count", activeSagaCount, AtomicLong::get)
            .description("Number of active Sagas")
            .register(meterRegistry);
            
        Gauge.builder("events.pending.count", pendingEventCount, AtomicLong::get)
            .description("Number of pending events")
            .register(meterRegistry);
            
        Gauge.builder("events.failed.count", failedEventCount, AtomicLong::get)
            .description("Number of failed events")
            .register(meterRegistry);
            
        // Initialize distribution summaries
        this.payloadSizeDistribution = DistributionSummary.builder("events.payload.size")
            .description("Event payload size distribution")
            .baseUnit("bytes")
            .register(meterRegistry);
        
        this.sagaDurationDistribution = DistributionSummary.builder("saga.duration.distribution")
            .description("Saga execution time distribution")
            .baseUnit("milliseconds")
            .register(meterRegistry);
    }
    
    // Event publishing metrics
    public void recordEventSuccessful(String eventType, String environment, int payloadSize) {
        Counter.builder("events.success.total")
            .tag("eventType", eventType)
            .tag("environment", environment)
            .register(meterRegistry)
            .increment();
            
        payloadSizeDistribution.record(payloadSize);
        
        log.debug("Successful event recorded: type={}, env={}, size={}bytes", 
                 eventType, environment, payloadSize);
    }
    
    public void recordEventFailure(String eventType, String environment, long durationMs, String errorType, String errorMessage) {
        eventPublishingTimer.record(durationMs, TimeUnit.MILLISECONDS);
        
        Counter.builder("events.failure.total")
            .tag("eventType", eventType)
            .tag("environment", environment)
            .tag("errorType", errorType)
            .register(meterRegistry)
            .increment();
            
        failedEventCount.incrementAndGet();
        
        // Record error details as custom metrics
        Counter.builder("events.errors.detail")
            .description("Error details")
            .tag("errorType", errorType)
            .tag("errorMessage", truncateErrorMessage(errorMessage))
            .register(meterRegistry)
            .increment();
        
        log.warn("Failed event recorded: type={}, env={}, duration={}ms, error={}", 
                eventType, environment, durationMs, errorType);
    }
    
    public void recordEventProcessed(String eventType, String environment, long processingTimeMs) {
        eventProcessingTimer.record(processingTimeMs, TimeUnit.MILLISECONDS);
        
        Counter.builder("events.processed.total")
            .tag("eventType", eventType)
            .tag("environment", environment)
            .register(meterRegistry)
            .increment();
        
        log.debug("Processed event recorded: type={}, env={}, processingTime={}ms", 
                 eventType, environment, processingTimeMs);
    }
    
    // Saga metrics
    public void recordSagaStarted(String sagaType) {
        activeSagaCount.incrementAndGet();
        
        Counter.builder("saga.started.total")
            .tag("sagaType", sagaType)
            .register(meterRegistry)
            .increment();
        
        log.debug("Saga started: type={}, active={}", sagaType, activeSagaCount.get());
    }
    
    public void recordSagaCompleted(String sagaType, long durationMs, boolean success) {
        activeSagaCount.decrementAndGet();
        sagaDurationDistribution.record(durationMs);
        
        if (success) {
            Counter.builder("saga.completed.total")
                .tag("sagaType", sagaType)
                .tag("status", "success")
                .register(meterRegistry)
                .increment();
        } else {
            Counter.builder("saga.failed.total")
                .tag("sagaType", sagaType)
                .tag("status", "failed")
                .register(meterRegistry)
                .increment();
        }
        
        sagaExecutionTimer.record(durationMs, TimeUnit.MILLISECONDS);
            
        log.info("Saga completed: type={}, duration={}ms, success={}, active={}", 
                sagaType, durationMs, success, activeSagaCount.get());
    }
    
    public void recordCompensationExecuted(String sagaType, String compensationType, long durationMs, boolean success) {
        Counter.builder("compensation.executed.total")
            .tag("sagaType", sagaType)
            .tag("compensationType", compensationType)
            .tag("status", success ? "success" : "failed")
            .register(meterRegistry)
            .increment();
        
        Timer.builder("compensation.execution.duration")
            .description("Compensation execution time")
            .tag("sagaType", sagaType)
            .tag("compensationType", compensationType)
            .tag("status", success ? "success" : "failed")
            .register(meterRegistry)
            .record(durationMs, TimeUnit.MILLISECONDS);
        
        log.info("Compensation executed: saga={}, compensation={}, duration={}ms, success={}", 
                sagaType, compensationType, durationMs, success);
    }
    
    // Database metrics
    public void recordDatabaseOperation(String operationType, long durationMs, boolean success) {
        databaseOperationTimer.record(durationMs, TimeUnit.MILLISECONDS);
            
        Counter.builder("database.operations.total")
            .description("Number of database operations")
            .tag("operation", operationType)
            .tag("status", success ? "success" : "failed")
            .register(meterRegistry)
            .increment();
        
        log.debug("Database operation recorded: operation={}, duration={}ms, success={}", 
                 operationType, durationMs, success);
    }
    
    // Custom metrics recording
    public void recordCustomMetric(String metricName, double value, Tags tags) {
        Gauge.builder(metricName, () -> value)
            .tags(tags)
            .register(meterRegistry);
            
        log.debug("Custom metric recorded: name={}, value={}, tags={}", metricName, value, tags);
    }
    
    public void incrementCustomCounter(String counterName, Tags tags) {
        Counter.builder(counterName)
            .tags(tags)
            .register(meterRegistry)
            .increment();
            
        log.debug("Custom counter incremented: name={}, tags={}", counterName, tags);
    }
    
    // User operation metrics
    public void recordUserRegistration(boolean success, long durationMs) {
        Timer.builder("user.registration.duration")
            .description("User registration processing time")
            .tag("status", success ? "success" : "failed")
            .register(meterRegistry)
            .record(durationMs, TimeUnit.MILLISECONDS);
        
        Counter.builder("user.registration.total")
            .description("Number of user registrations")
            .tag("status", success ? "success" : "failed")
            .register(meterRegistry)
            .increment();
    }
    
    public void recordUserDeletion(boolean success, long durationMs) {
        Timer.builder("user.deletion.duration")
            .description("User deletion processing time")
            .tag("status", success ? "success" : "failed")
            .register(meterRegistry)
            .record(durationMs, TimeUnit.MILLISECONDS);
        
        Counter.builder("user.deletion.total")
            .description("Number of user deletions")
            .tag("status", success ? "success" : "failed")
            .register(meterRegistry)
            .increment();
    }
    
    /**
     * Increment the number of user registrations
     */
    public void incrementUserRegistrations() {
        Counter.builder("user.registrations.total")
            .description("Number of user registrations")
            .register(meterRegistry)
            .increment();
        
        log.debug("User registration metric recorded");
    }
    
    // Business metrics
    public void recordActiveUsers(long count) {
        Gauge.builder("users.active.count", () -> count)
            .description("Number of active users")
            .register(meterRegistry);
    }
    
    public void recordTotalUsers(long count) {
        Gauge.builder("users.total.count", () -> count)
            .description("Total number of users")
            .register(meterRegistry);
    }
    
    // Message broker error metrics
    public void recordMessageBrokerError(String brokerType, String errorType, String errorMessage) {
        Counter.builder("messagebroker.error.total")
            .description("Number of message broker errors")
            .tag("brokerType", brokerType)
            .tag("errorType", errorType)
            .register(meterRegistry)
            .increment();
        
        Counter.builder("messagebroker.errors.detail")
            .description("Message broker error details")
            .tag("brokerType", brokerType)
            .tag("errorType", errorType)
            .tag("errorMessage", truncateErrorMessage(errorMessage))
            .register(meterRegistry)
            .increment();
        
        log.warn("Message broker error recorded: broker={}, errorType={}, error={}", 
                brokerType, errorType, truncateErrorMessage(errorMessage));
    }
    
    // Utility methods
    private String truncateErrorMessage(String errorMessage) {
        if (errorMessage == null) return "unknown";
        return errorMessage.length() > 100 ? errorMessage.substring(0, 100) + "..." : errorMessage;
    }
    
    // Methods to get current status
    public long getActiveSagaCount() {
        return activeSagaCount.get();
    }
    
    public long getPendingEventCount() {
        return pendingEventCount.get();
    }
    
    public long getFailedEventCount() {
        return failedEventCount.get();
    }
    
    public void incrementPendingEventCount() {
        pendingEventCount.incrementAndGet();
    }
    
    public void decrementPendingEventCount() {
        pendingEventCount.decrementAndGet();
    }
    
    public void resetFailedEventCount() {
        failedEventCount.set(0);
    }
    
    // System resource metrics
    public void recordMemoryUsage(long usedMemory, long maxMemory) {
        double usagePercent = (double) usedMemory / maxMemory * 100;
        
        Gauge.builder("system.memory.used", () -> usedMemory)
            .description("Used memory amount")
            .baseUnit("bytes")
            .register(meterRegistry);
        
        Gauge.builder("system.memory.usage.percent", () -> usagePercent)
            .description("Memory usage rate")
            .baseUnit("percent")
            .register(meterRegistry);
    }
    
    public void recordThreadPoolMetrics(String poolName, int activeThreads, int totalThreads, int queueSize) {
        Tags tags = Tags.of("pool", poolName);
        
        Gauge.builder("threadpool.active.threads", () -> activeThreads)
            .description("Number of active threads")
            .tags(tags)
            .register(meterRegistry);
        
        Gauge.builder("threadpool.total.threads", () -> totalThreads)
            .description("Total number of threads")
            .tags(tags)
            .register(meterRegistry);
        
        Gauge.builder("threadpool.queue.size", () -> queueSize)
            .description("Queue size")
            .tags(tags)
            .register(meterRegistry);
    }
}
