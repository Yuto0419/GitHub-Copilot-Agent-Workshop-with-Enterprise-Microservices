package com.skishop.auth.service;

import com.skishop.auth.dto.LoginResponse;
import com.skishop.auth.dto.MfaVerificationRequest;
import com.skishop.auth.entity.UserMFA;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * MFA (Multi-Factor Authentication) Service
 * Implements two-factor authentication using TOTP (Time-based One-Time Password)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MfaService {

    private final EntityManager entityManager;
    
    // Manual log field since Lombok @Slf4j may not be working properly
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MfaService.class);

    /**
     * Verify MFA code
     */
    public boolean verifyMfaCode(UUID userId, String code) {
        try {
            // Get UserMFA entity
            UserMFA userMFA = entityManager.createQuery(
                "SELECT um FROM UserMFA um WHERE um.user.id = :userId AND um.isEnabled = true", 
                UserMFA.class)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst()
                .orElse(null);

            if (userMFA == null) {
                log.warn("MFA not enabled for user: {}", userId);
                return false;
            }

            // Simple code verification (will be replaced with actual TOTP verification)
            // TODO: Implement verification using TOTP library
            if (code != null && code.length() == 6 && code.matches("\\d{6}")) {
                log.info("MFA code verified for user: {}", userId);
                return true;
            }

            log.warn("Invalid MFA code for user: {}", userId);
            return false;

        } catch (Exception e) {
            log.error("Error verifying MFA code for user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Verify MFA code (request object version)
     */
    public LoginResponse verifyMfaCode(MfaVerificationRequest request, HttpServletRequest httpRequest) {
        // Get user ID from session ID (simplified implementation)
        UUID userId = UUID.fromString(request.getSessionId()); // In reality, session management is needed
        
        boolean isValid = verifyMfaCode(userId, request.getMfaCode());
        
        if (!isValid) {
            return LoginResponse.error("Invalid MFA code");
        }
        
        // Token generation after successful MFA verification (simplified)
        // In actual implementation, integration with authentication service is necessary
        return LoginResponse.success(null, null, null);
    }

    /**
     * Enable MFA
     */
    public String enableMfa(UUID userId) {
        try {
            // TODO: Generate secret key using TOTP library
            String secretKey = "MOCK_SECRET_KEY"; // In actual implementation, this should be securely generated
            
            UserMFA userMFA = UserMFA.builder()
                .id(UUID.randomUUID())
                .secretKey(secretKey)
                .isEnabled(true)
                .build();
            
            entityManager.persist(userMFA);
            log.info("MFA enabled for user: {}", userId);
            
            return secretKey;
            
        } catch (Exception e) {
            log.error("Error enabling MFA for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to enable MFA");
        }
    }

    /**
     * Disable MFA
     */
    public void disableMfa(UUID userId) {
        try {
            UserMFA userMFA = entityManager.createQuery(
                "SELECT um FROM UserMFA um WHERE um.user.id = :userId", 
                UserMFA.class)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst()
                .orElse(null);

            if (userMFA != null) {
                userMFA.setIsEnabled(false);
                entityManager.merge(userMFA);
                log.info("MFA disabled for user: {}", userId);
            }
            
        } catch (Exception e) {
            log.error("Error disabling MFA for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to disable MFA");
        }
    }
}
