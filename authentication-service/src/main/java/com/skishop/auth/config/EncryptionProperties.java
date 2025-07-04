package com.skishop.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Encryption configuration properties
 */
@ConfigurationProperties(prefix = "encryption")
@Data
@Component
public class EncryptionProperties {
    private String password = System.getenv("ENCRYPTION_PASSWORD");
}
