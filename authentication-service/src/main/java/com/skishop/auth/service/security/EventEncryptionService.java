package com.skishop.auth.service.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.stereotype.Service;

/**
 * Event data encryption service
 * 
 * Encrypts and decrypts event payloads containing sensitive data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventEncryptionService {
    
    private final StringEncryptor encryptor;
    private final ObjectMapper objectMapper;
    
    /**
     * Encrypt payload
     */
    public String encryptPayload(Object payload) throws JsonProcessingException {
        try {
            String json = objectMapper.writeValueAsString(payload);
            String encrypted = encryptor.encrypt(json);
            log.debug("Payload encrypted successfully, original size: {}, encrypted size: {}", 
                     json.length(), encrypted.length());
            return encrypted;
        } catch (Exception e) {
            log.error("Failed to encrypt payload: {}", e.getMessage(), e);
            throw new RuntimeException("Payload encryption failed", e);
        }
    }
    
    /**
     * Decrypt payload
     */
    public <T> T decryptPayload(String encryptedPayload, Class<T> clazz) 
            throws JsonProcessingException {
        try {
            String decrypted = encryptor.decrypt(encryptedPayload);
            T result = objectMapper.readValue(decrypted, clazz);
            log.debug("Payload decrypted successfully for type: {}", clazz.getSimpleName());
            return result;
        } catch (Exception e) {
            log.error("Failed to decrypt payload for type {}: {}", clazz.getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Payload decryption failed", e);
        }
    }
    
    /**
     * Mask sensitive information
     */
    public String maskSensitiveData(String data) {
        if (data == null || data.length() <= 4) {
            return "*".repeat(data != null ? data.length() : 0);
        }
        return data.substring(0, 2) + "*".repeat(data.length() - 4) + data.substring(data.length() - 2);
    }
    
    /**
     * Check if payload is already encrypted
     */
    public boolean isEncrypted(String payload) {
        try {
            // Encrypted data is usually Base64 encoded and not in JSON format
            objectMapper.readTree(payload);
            return false; // If it can be parsed as JSON, it is not encrypted
        } catch (JsonProcessingException e) {
            return true; // If JSON parsing fails, it is likely encrypted
        }
    }
}
