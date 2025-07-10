package com.skishop.auth.service;

import com.skishop.auth.dto.MfaSetupRequest;
import com.skishop.auth.dto.MfaSetupResponse;
import com.skishop.auth.entity.UserMFA;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MfaServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private GoogleAuthenticator googleAuthenticator;

    @Mock
    private TypedQuery<UserMFA> typedQuery;

    @InjectMocks
    private MfaService mfaService;

    private UUID testUserId;
    private UserMFA testUserMFA;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUserMFA = UserMFA.builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .secretKey("TESTSECRETKEY123")
                .mfaType("TOTP")
                .isEnabled(true)
                .backupCodes(Arrays.asList("12345678", "87654321"))
                .build();
    }

    @Test
    void verifyMfaCode_ValidTotpCode_ShouldReturnTrue() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(UserMFA.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.of(testUserMFA));
        when(googleAuthenticator.authorize(eq("TESTSECRETKEY123"), eq(123456))).thenReturn(true);

        // Act
        boolean result = mfaService.verifyMfaCode(testUserId, "123456");

        // Assert
        assertTrue(result);
        verify(googleAuthenticator).authorize("TESTSECRETKEY123", 123456);
        verify(entityManager).merge(testUserMFA);
    }

    @Test
    void verifyMfaCode_ValidRecoveryCode_ShouldReturnTrue() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(UserMFA.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.of(testUserMFA));

        // Act
        boolean result = mfaService.verifyMfaCode(testUserId, "12345678");

        // Assert
        assertTrue(result);
        // Verify recovery code was removed
        assertFalse(testUserMFA.getBackupCodes().contains("12345678"));
        verify(entityManager).merge(testUserMFA);
    }

    @Test
    void verifyMfaCode_InvalidCode_ShouldReturnFalse() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(UserMFA.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.of(testUserMFA));
        when(googleAuthenticator.authorize(eq("TESTSECRETKEY123"), eq(999999))).thenReturn(false);

        // Act
        boolean result = mfaService.verifyMfaCode(testUserId, "999999");

        // Assert
        assertFalse(result);
        verify(googleAuthenticator).authorize("TESTSECRETKEY123", 999999);
    }

    @Test
    void verifyMfaCode_MfaNotEnabled_ShouldReturnFalse() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(UserMFA.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.empty());

        // Act
        boolean result = mfaService.verifyMfaCode(testUserId, "123456");

        // Assert
        assertFalse(result);
        verify(googleAuthenticator, never()).authorize(anyString(), anyInt());
    }

    @Test
    void setupMfa_ValidRequest_ShouldReturnSetupResponse() {
        // Arrange
        MfaSetupRequest request = new MfaSetupRequest();
        request.setUserId(testUserId.toString());
        request.setAccountName("test@skishop.com");
        request.setIssuer("SkiShop");

        GoogleAuthenticatorKey mockKey = mock(GoogleAuthenticatorKey.class);
        when(mockKey.getKey()).thenReturn("TESTSECRETKEY123");
        when(googleAuthenticator.createCredentials()).thenReturn(mockKey);

        // Act
        MfaSetupResponse response = mfaService.setupMfa(request);

        // Assert
        assertNotNull(response);
        assertEquals("TESTSECRETKEY123", response.getSecretKey());
        assertEquals("SkiShop", response.getIssuer());
        assertEquals("test@skishop.com", response.getAccountName());
        assertNotNull(response.getQrCodeBase64());
        assertNotNull(response.getRecoveryCodes());
        assertEquals(10, response.getRecoveryCodes().size());
        
        verify(entityManager).persist(any(UserMFA.class));
    }

    @Test
    void setupMfa_WithDefaultValues_ShouldUseDefaults() {
        // Arrange
        MfaSetupRequest request = new MfaSetupRequest();
        request.setUserId(testUserId.toString());

        GoogleAuthenticatorKey mockKey = mock(GoogleAuthenticatorKey.class);
        when(mockKey.getKey()).thenReturn("TESTSECRETKEY123");
        when(googleAuthenticator.createCredentials()).thenReturn(mockKey);

        // Act
        MfaSetupResponse response = mfaService.setupMfa(request);

        // Assert
        assertNotNull(response);
        assertEquals("SkiShop", response.getIssuer());
        assertEquals("user@skishop.com", response.getAccountName());
    }

    @Test
    void regenerateRecoveryCodes_ValidUser_ShouldReturnNewCodes() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(UserMFA.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.of(testUserMFA));

        // Act
        List<String> newCodes = mfaService.regenerateRecoveryCodes(testUserId);

        // Assert
        assertNotNull(newCodes);
        assertEquals(10, newCodes.size());
        // Verify all codes are 8 digits
        newCodes.forEach(code -> {
            assertEquals(8, code.length());
            assertTrue(code.matches("\\d{8}"));
        });
        verify(entityManager).merge(testUserMFA);
    }

    @Test
    void regenerateRecoveryCodes_MfaNotEnabled_ShouldThrowException() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(UserMFA.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            mfaService.regenerateRecoveryCodes(testUserId);
        });
    }

    @Test
    void disableMfa_ExistingMfa_ShouldDisable() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(UserMFA.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.of(testUserMFA));

        // Act
        mfaService.disableMfa(testUserId);

        // Assert
        assertFalse(testUserMFA.isEnabled());
        verify(entityManager).merge(testUserMFA);
    }
}