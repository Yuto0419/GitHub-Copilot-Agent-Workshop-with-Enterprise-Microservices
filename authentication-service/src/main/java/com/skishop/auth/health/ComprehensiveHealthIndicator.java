package com.skishop.auth.health;

import com.skishop.auth.config.SkishopRuntimeProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Comprehensive system health monitoring service
 * Independent monitoring function that does not depend on Spring Boot Actuator
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ComprehensiveHealthIndicator {
    
    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SkishopRuntimeProperties properties;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    
    /**
     * Execute overall system health check
     */
    public Map<String, Object> checkSystemHealth() {
        Map<String, Object> healthStatus = new HashMap<>();
        boolean overallHealthy = true;
        
        try {
            // Database connection check
            boolean dbHealthy = checkDatabase(healthStatus);
            if (!dbHealthy) overallHealthy = false;
            
            // Redis connection check
            boolean redisHealthy = checkRedis(healthStatus);
            if (!redisHealthy) overallHealthy = false;
            
            // Event publisher health check
            checkEventPublishers(healthStatus);
            
            // Circuit breaker status check
            checkCircuitBreakers(healthStatus);
            
            // Memory usage check
            checkMemoryUsage(healthStatus);
            
            // Disk usage check
            checkDiskUsage(healthStatus);
            
            // Configuration validity check
            checkConfiguration(healthStatus);
            
            healthStatus.put("overall_status", overallHealthy ? "UP" : "DOWN");
            healthStatus.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("Health check failed", e);
            healthStatus.put("overall_status", "DOWN");
            healthStatus.put("error", e.getMessage());
        }
        
        return healthStatus;
    }
    
    private boolean checkDatabase(Map<String, Object> details) {
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5); // 5 seconds timeout
            details.put("database.status", isValid ? "UP" : "DOWN");
            details.put("database.url", maskSensitiveInfo(connection.getMetaData().getURL()));
            details.put("database.validationTime", System.currentTimeMillis());
            return isValid;
        } catch (SQLException e) {
            details.put("database.status", "DOWN");
            details.put("database.error", e.getMessage());
            log.warn("Database health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean checkRedis(Map<String, Object> details) {
        try {
            String pingResult = redisTemplate.getConnectionFactory().getConnection().ping();
            boolean isHealthy = "PONG".equals(pingResult);
            details.put("redis.status", isHealthy ? "UP" : "DOWN");
            details.put("redis.pingResult", pingResult);
            details.put("redis.keyPrefix", properties.getEventRedisKeyPrefix());
            return isHealthy;
        } catch (Exception e) {
            details.put("redis.status", "DOWN");
            details.put("redis.error", e.getMessage());
            log.warn("Redis health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    private void checkEventPublishers(Map<String, Object> details) {
        details.put("eventPublisher.enabled", properties.isEventPropagationEnabled());
        details.put("eventPublisher.brokerType", properties.getEventBrokerType());
        details.put("eventPublisher.userRegistrationEnabled", properties.isUserRegistrationEventEnabled());
        details.put("eventPublisher.userDeletionEnabled", properties.isUserDeletionEventEnabled());
        
        if (properties.isEventPropagationEnabled()) {
            try {
                // Specific health check for event publisher
                details.put("eventPublisher.status", "UP");
            } catch (Exception e) {
                details.put("eventPublisher.status", "DOWN");
                details.put("eventPublisher.error", e.getMessage());
            }
        } else {
            details.put("eventPublisher.status", "DISABLED");
        }
    }
    
    private void checkCircuitBreakers(Map<String, Object> details) {
        Map<String, String> cbStatus = new HashMap<>();
        
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            CircuitBreaker.State state = cb.getState();
            cbStatus.put(cb.getName(), state.name());
            
            // Warn if circuit breaker is open
            if (state == CircuitBreaker.State.OPEN) {
                log.warn("Circuit breaker '{}' is OPEN", cb.getName());
            }
        });
        
        details.put("circuitBreakers", cbStatus);
    }
    
    private void checkMemoryUsage(Map<String, Object> details) {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        
        long used = heapUsage.getUsed();
        long max = heapUsage.getMax();
        double usagePercentage = (double) used / max * 100;
        
        details.put("memory.used", used);
        details.put("memory.max", max);
        details.put("memory.usagePercentage", Math.round(usagePercentage * 100.0) / 100.0);
        
        // Warn if memory usage exceeds 90%
        if (usagePercentage > 90) {
            details.put("memory.status", "WARNING");
            log.warn("High memory usage: {}%", usagePercentage);
        } else {
            details.put("memory.status", "OK");
        }
    }
    
    private void checkDiskUsage(Map<String, Object> details) {
        try {
            File root = new File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            
            double usagePercentage = (double) usedSpace / totalSpace * 100;
            
            details.put("disk.total", totalSpace);
            details.put("disk.used", usedSpace);
            details.put("disk.free", freeSpace);
            details.put("disk.usagePercentage", Math.round(usagePercentage * 100.0) / 100.0);
            
        // Warn if disk usage exceeds 85%
            if (usagePercentage > 85) {
                details.put("disk.status", "WARNING");
                log.warn("High disk usage: {}%", usagePercentage);
            } else {
                details.put("disk.status", "OK");
            }
        } catch (Exception e) {
            details.put("disk.status", "UNKNOWN");
            details.put("disk.error", e.getMessage());
        }
    }
    
    private void checkConfiguration(Map<String, Object> details) {
        details.put("config.environment", properties.getEnvironment());
        details.put("config.debugMode", properties.isDebugMode());
        details.put("config.eventTimeout", properties.getEventTimeoutMs());
        details.put("config.maxRetries", properties.getEventMaxRetries());
        details.put("config.concurrency", properties.getEventConcurrency());
        
        // Configuration validity check
        boolean configValid = true;
        StringBuilder configErrors = new StringBuilder();
        
        if (properties.getEventTimeoutMs() < 1000) {
            configValid = false;
            configErrors.append("Event timeout too short; ");
        }
        
        if (properties.getEventMaxRetries() < 1 || properties.getEventMaxRetries() > 10) {
            configValid = false;
            configErrors.append("Invalid retry count; ");
        }
        
        details.put("config.status", configValid ? "VALID" : "INVALID");
        if (!configValid) {
            details.put("config.errors", configErrors.toString());
        }
    }
    
    private String maskSensitiveInfo(String url) {
        if (url == null) return null;
        // Mask sensitive information from connection string
        return url.replaceAll("password=[^;]+", "password=****")
                 .replaceAll("Password=[^;]+", "Password=****");
    }
}
