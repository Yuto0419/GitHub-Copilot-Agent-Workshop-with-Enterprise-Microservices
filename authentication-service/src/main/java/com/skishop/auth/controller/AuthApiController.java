package com.skishop.auth.controller;

import com.skishop.auth.dto.LoginRequest;
import com.skishop.auth.dto.LoginResponse;
import com.skishop.auth.dto.MfaVerificationRequest;
import com.skishop.auth.dto.TokenRefreshRequest;
import com.skishop.auth.dto.TokenRefreshResponse;
import com.skishop.auth.dto.PasswordResetRequest;
import com.skishop.auth.dto.PasswordResetConfirmRequest;
import com.skishop.auth.dto.LogoutResponse;
import com.skishop.auth.dto.OAuthCallbackRequest;
import com.skishop.auth.service.AuthenticationService;
import com.skishop.auth.service.MfaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * Authentication API REST endpoint controller
 * Provides APIs for user authentication, token management, password reset, etc.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication API", description = "Authentication and authorization related APIs")
public class AuthApiController {

    private final AuthenticationService authenticationService;
    private final MfaService mfaService;

    /**
     * User login
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "User authentication by email and password")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Login attempt for user: {}", request.getEmail());
        
        try {
            LoginResponse response = authenticationService.authenticateUser(request, httpRequest);
            log.info("Login successful for user: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for user: {}, error: {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    /**
     * MFA verification
     */
    @PostMapping("/mfa/verify")
    @Operation(summary = "MFA verification", description = "Verification of multi-factor authentication code")
    public ResponseEntity<LoginResponse> verifyMfa(
            @Valid @RequestBody MfaVerificationRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("MFA verification attempt for session: {}", request.getSessionId());
        
        try {
            LoginResponse response = mfaService.verifyMfaCode(request, httpRequest);
            log.info("MFA verification successful for session: {}", request.getSessionId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("MFA verification failed for session: {}, error: {}", request.getSessionId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Token refresh
     */
    @PostMapping("/refresh")
    @Operation(summary = "Token refresh", description = "Update access token using refresh token")
    public ResponseEntity<TokenRefreshResponse> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {
        
        log.info("Token refresh attempt");
        
        try {
            TokenRefreshResponse response = authenticationService.refreshTokenApi(request);
            log.info("Token refresh successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Logout
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "User logout and session invalidation")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LogoutResponse> logout(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("Logout attempt");
        
        try {
            LogoutResponse response = authenticationService.logout(authHeader, request);
            log.info("Logout successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Start OAuth redirect
     */
    @GetMapping("/oauth/{provider}/redirect")
    @Operation(summary = "Start OAuth authentication", description = "Start OAuth authentication with the specified provider")
    public ResponseEntity<Map<String, String>> oauthRedirect(
            @PathVariable String provider,
            @RequestParam(required = false) String redirectUri) {
        
        log.info("OAuth redirect initiated for provider: {}", provider);
        
        try {
            String authUrl = authenticationService.initiateOAuthFlow(provider, redirectUri);
            return ResponseEntity.ok(Map.of("authUrl", authUrl));
        } catch (Exception e) {
            log.error("OAuth redirect failed for provider: {}, error: {}", provider, e.getMessage());
            throw e;
        }
    }

    /**
     * OAuth callback
     */
    @PostMapping("/oauth/{provider}/callback")
    @Operation(summary = "OAuth authentication callback", description = "Callback processing from OAuth provider")
    public ResponseEntity<LoginResponse> oauthCallback(
            @PathVariable String provider,
            @Valid @RequestBody OAuthCallbackRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("OAuth callback received for provider: {}", provider);
        
        try {
            LoginResponse response = authenticationService.handleOAuthCallback(provider, request, httpRequest);
            log.info("OAuth authentication successful for provider: {}", provider);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("OAuth callback failed for provider: {}, error: {}", provider, e.getMessage());
            throw e;
        }
    }

    /**
     * Password reset request
     */
    @PostMapping("/password/reset-request")
    @Operation(summary = "Password reset request", description = "Send email for password reset")
    public ResponseEntity<Map<String, String>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {
        
        log.info("Password reset request for email: {}", request.getEmail());
        
        try {
            authenticationService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok(Map.of("message", "Password reset instructions sent to your email"));
        } catch (Exception e) {
            log.error("Password reset request failed for email: {}, error: {}", request.getEmail(), e.getMessage());
            // For security reasons, return a success response even if there is an error
            return ResponseEntity.ok(Map.of("message", "Password reset instructions sent to your email"));
        }
    }

    /**
     * Execute password reset
     */
    @PostMapping("/password/reset")
    @Operation(summary = "Execute password reset", description = "Reset password using token")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody PasswordResetConfirmRequest request) {
        
        log.info("Password reset execution attempt");
        
        try {
            authenticationService.resetPassword(request.getToken(), request.getNewPassword());
            log.info("Password reset successful");
            return ResponseEntity.ok(Map.of("message", "Password successfully reset"));
        } catch (Exception e) {
            log.error("Password reset failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Token validation
     */
    @PostMapping("/validate")
    @Operation(summary = "Token validation", description = "Validate the validity of the access token")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            Map<String, Object> tokenInfo = authenticationService.validateToken(authHeader);
            return ResponseEntity.ok(tokenInfo);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get current user information
     */
    @GetMapping("/me")
    @Operation(summary = "Current user information", description = "Get information of the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            Map<String, Object> userInfo = authenticationService.getCurrentUserInfo(authHeader);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            log.error("Get current user failed: {}", e.getMessage());
            throw e;
        }
    }
}
