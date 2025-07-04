package com.skishop.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Security manager for configuration values
 * Proper masking of sensitive information and secure logging
 */
@Component
@Slf4j
public class ConfigurationSecurityManager {
    
    private static final Set<String> SENSITIVE_KEYS = Set.of(
        "password", "secret", "key", "token", "credential", "connectionstring"
    );
    
    /**
     * Mask sensitive values
     */
    public String maskSensitiveValue(String key, String value) {
        if (value == null) return null;
        
        if (isSensitiveKey(key)) {
            return value.length() > 4 ? 
                value.substring(0, 4) + "*".repeat(value.length() - 4) : 
                "*".repeat(value.length());
        }
        return value;
    }
    
    /**
     * Determine if a key is sensitive
     */
    private boolean isSensitiveKey(String key) {
        if (key == null) return false;
        String lowerKey = key.toLowerCase();
        return SENSITIVE_KEYS.stream().anyMatch(lowerKey::contains);
    }
    
    /**
     * Log configuration safely
     */
    public void logConfigurationSafely(Map<String, String> config) {
        config.forEach((key, value) -> {
            String maskedValue = maskSensitiveValue(key, value);
            log.info("Config: {}={}", key, maskedValue);
        });
    }
    
    /**
     * Safely mask connection strings
     */
    public String maskConnectionString(String connectionString) {
        if (connectionString == null) return null;
        
        return connectionString
            .replaceAll("Password=[^;]+", "Password=****")
            .replaceAll("password=[^;]+", "password=****")
            .replaceAll("SharedAccessKey=[^;]+", "SharedAccessKey=****")
            .replaceAll("client_secret=[^&]+", "client_secret=****");
    }
    
    /**
     * Secure validation of JWT secret
     */
    public boolean isValidJwtSecret(String jwtSecret) {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            log.error("JWT secret is null or empty");
            return false;
        }
        
        // Require at least 256 bits (32 characters)
        if (jwtSecret.length() < 32) {
            log.error("JWT secret is too short. Minimum 32 characters required, but got: {}", jwtSecret.length());
            return false;
        }

        // Check for weak passwords
        String lowerSecret = jwtSecret.toLowerCase();
        String[] weakPatterns = {"test", "password", "secret", "123456", "qwerty"};

        for (String pattern : weakPatterns) {
            if (lowerSecret.contains(pattern)) {
                log.warn("JWT secret contains weak pattern: {}", pattern);
                return false;
            }
        }

        log.info("JWT secret validation passed. Length: {} characters", jwtSecret.length());
        return true;
    }
    
    /**
     * Securely get environment variable
     */
    public String getSecureEnvironmentVariable(String varName, String defaultValue) {
        String value = System.getenv(varName);
        if (value == null || value.trim().isEmpty()) {
            log.warn("Environment variable '{}' is not set, using default", varName);
            return defaultValue;
        }
        
        log.info("Environment variable '{}' loaded successfully", varName);
        return value;
    }
}
