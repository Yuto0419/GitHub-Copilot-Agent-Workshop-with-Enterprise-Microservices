package com.skishop.auth.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.skishop.auth.dto.LoginResponse;
import com.skishop.auth.dto.MfaSetupRequest;
import com.skishop.auth.dto.MfaSetupResponse;
import com.skishop.auth.dto.MfaVerificationRequest;
import com.skishop.auth.entity.UserMFA;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
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
    private final GoogleAuthenticator googleAuthenticator;
    
    // Manual log field since Lombok @Slf4j may not be working properly
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MfaService.class);
    
    // Default application name for QR codes
    private static final String DEFAULT_ISSUER = "SkiShop";
    private static final int RECOVERY_CODES_COUNT = 10;
    private static final int RECOVERY_CODE_LENGTH = 8;

    /**
     * Verify MFA code using TOTP
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

            // First check if it's a recovery code
            if (isRecoveryCode(userMFA, code)) {
                log.info("Recovery code used for user: {}", userId);
                userMFA.updateLastUsed();
                return true;
            }

            // Verify TOTP code using GoogleAuth library
            try {
                int numericCode = Integer.parseInt(code);
                boolean isValid = googleAuthenticator.authorize(userMFA.getSecretKey(), numericCode);
                
                if (isValid) {
                    log.info("MFA code verified for user: {}", userId);
                    userMFA.updateLastUsed();
                    entityManager.merge(userMFA);
                    return true;
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid MFA code format for user {}: {}", userId, e.getMessage());
                return false;
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
     * Set up MFA for a user with QR code generation
     */
    public MfaSetupResponse setupMfa(MfaSetupRequest request) {
        try {
            UUID userId = request.getUserIdAsUUID();
            String accountName = request.getAccountName() != null ? request.getAccountName() : "user@skishop.com";
            String issuer = request.getIssuer() != null ? request.getIssuer() : DEFAULT_ISSUER;
            
            // Generate secret key using GoogleAuth
            GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
            String secretKey = key.getKey();
            
            // Generate recovery codes
            List<String> recoveryCodes = generateRecoveryCodes();
            
            // Create or update UserMFA entity
            UserMFA userMFA = UserMFA.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .mfaType("TOTP")
                .secretKey(secretKey)
                .backupCodes(recoveryCodes)
                .isEnabled(true)
                .build();
            
            entityManager.persist(userMFA);
            
            // Generate QR code URL and Base64 image
            String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(issuer, accountName, key);
            String qrCodeBase64 = generateQRCodeBase64(qrCodeUrl);
            
            log.info("MFA setup completed for user: {}", userId);
            
            return MfaSetupResponse.success(
                secretKey, 
                qrCodeUrl, 
                qrCodeBase64, 
                recoveryCodes, 
                issuer, 
                accountName
            );
            
        } catch (Exception e) {
            log.error("Error setting up MFA for user {}: {}", request.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to setup MFA");
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
    
    /**
     * Generate recovery codes
     */
    private List<String> generateRecoveryCodes() {
        List<String> recoveryCodes = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        
        for (int i = 0; i < RECOVERY_CODES_COUNT; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < RECOVERY_CODE_LENGTH; j++) {
                sb.append(random.nextInt(10));
            }
            recoveryCodes.add(sb.toString());
        }
        
        return recoveryCodes;
    }
    
    /**
     * Generate QR code as Base64 encoded image
     */
    private String generateQRCodeBase64(String qrCodeUrl) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeUrl, BarcodeFormat.QR_CODE, 200, 200);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            byte[] qrCodeBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(qrCodeBytes);
            
        } catch (Exception e) {
            log.error("Error generating QR code: {}", e.getMessage());
            throw new RuntimeException("Failed to generate QR code");
        }
    }
    
    /**
     * Check if the provided code is a recovery code
     */
    private boolean isRecoveryCode(UserMFA userMFA, String code) {
        if (userMFA.getBackupCodes() == null || code == null) {
            return false;
        }
        
        // Check if code matches any recovery code
        boolean isValid = userMFA.getBackupCodes().contains(code);
        
        if (isValid) {
            // Remove used recovery code
            userMFA.getBackupCodes().remove(code);
            entityManager.merge(userMFA);
        }
        
        return isValid;
    }
    
    /**
     * Generate new recovery codes for existing MFA setup
     */
    public List<String> regenerateRecoveryCodes(UUID userId) {
        try {
            UserMFA userMFA = entityManager.createQuery(
                "SELECT um FROM UserMFA um WHERE um.user.id = :userId AND um.isEnabled = true", 
                UserMFA.class)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst()
                .orElse(null);

            if (userMFA == null) {
                throw new RuntimeException("MFA not enabled for user");
            }
            
            List<String> newRecoveryCodes = generateRecoveryCodes();
            userMFA.setBackupCodes(newRecoveryCodes);
            entityManager.merge(userMFA);
            
            log.info("Recovery codes regenerated for user: {}", userId);
            return newRecoveryCodes;
            
        } catch (Exception e) {
            log.error("Error regenerating recovery codes for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to regenerate recovery codes");
        }
    }
}
