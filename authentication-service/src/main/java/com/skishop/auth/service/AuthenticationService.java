package com.skishop.auth.service;

import com.skishop.auth.dto.*;
import com.skishop.auth.entity.User;
import com.skishop.auth.entity.UserSession;
import com.skishop.auth.exception.AuthenticationException;
import com.skishop.auth.exception.InvalidTokenException;
import com.skishop.auth.repository.UserRepository;
import com.skishop.auth.repository.UserSessionRepository;
import com.skishop.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

/**
 * Authentication Service
 * Responsible for login, logout, token refresh, and MFA verification
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SecurityLogService securityLogService;
    private final MfaService mfaService;
    private final EventPublishingService eventPublishingService;

    /**
     * User login
     */
    public AuthenticationResponse authenticate(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Authentication attempt for email: {}", request.getEmail());
        
        // User search
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        // Account status check
        if (!user.isActive()) {
            securityLogService.logAuthenticationAttempt(user.getId(), "LOGIN_FAILED", 
                ipAddress, userAgent, "Account inactive");
            throw new AuthenticationException("Account is inactive");
        }

        if (user.isLocked()) {
            securityLogService.logAuthenticationAttempt(user.getId(), "LOGIN_FAILED", 
                ipAddress, userAgent, "Account locked");
            throw new AuthenticationException("Account is locked");
        }

        // Password validation
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user, ipAddress, userAgent);
            throw new AuthenticationException("Invalid credentials");
        }

        // MFA requirement check
        if (user.isMfaEnabled()) {
            if (request.getMfaCode() == null || request.getMfaCode().isEmpty()) {
                // Generate temporary token for MFA required state
                String tempToken = jwtUtil.generateTempToken(user.getId(), "MFA_REQUIRED");
                return AuthenticationResponse.builder()
                    .tempToken(tempToken)
                    .mfaRequired(true)
                    .build();
            }

            // MFA verification
            if (!mfaService.verifyMfaCode(user.getId(), request.getMfaCode())) {
                securityLogService.logAuthenticationAttempt(user.getId(), "MFA_FAILED", 
                    ipAddress, userAgent, "Invalid MFA code");
                throw new AuthenticationException("Invalid MFA code");
            }
        }

        // Handle successful login
        return handleSuccessfulLogin(user, ipAddress, userAgent);
    }

    /**
     * MFA verification
     */
    public AuthenticationResponse verifyMfa(MfaVerificationRequest request, String ipAddress, String userAgent) {
        log.info("MFA verification attempt");

        // Temporary token validation
        if (!jwtUtil.validateTempToken(request.getTempToken())) {
            throw new InvalidTokenException("Invalid temporary token");
        }

        UUID userId = UUID.fromString(jwtUtil.getUserIdFromToken(request.getTempToken()));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AuthenticationException("User not found"));

        // MFA verification
        if (!mfaService.verifyMfaCode(userId, request.getMfaCode())) {
            securityLogService.logAuthenticationAttempt(userId, "MFA_FAILED", 
                ipAddress, userAgent, "Invalid MFA code");
            throw new AuthenticationException("Invalid MFA code");
        }

        // Login success handling
        return handleSuccessfulLogin(user, ipAddress, userAgent);
    }

    /**
     * Token refresh
     */
    public AuthenticationResponse refreshToken(TokenRefreshRequest request) {
        log.info("Token refresh attempt");

        String refreshToken = request.getRefreshToken();
        
        // Refresh token validation
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        UUID userId = UUID.fromString(jwtUtil.getUserIdFromToken(refreshToken));
        
        // Session verification
        UserSession session = userSessionRepository.findByRefreshToken(refreshToken)
            .orElseThrow(() -> new InvalidTokenException("Session not found"));

        if (session.getExpiresAt().isBefore(Instant.now())) {
            userSessionRepository.delete(session);
            throw new InvalidTokenException("Session expired");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AuthenticationException("User not found"));

        // Generate new tokens
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId().toString());

        // Update session
        session.setRefreshToken(newRefreshToken);
        session.setExpiresAt(Instant.now().plusSeconds(7L * 24 * 3600)); // 7 days
        userSessionRepository.save(session);

        return AuthenticationResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .tokenType("Bearer")
            .expiresIn(3600L)
            .user(convertToUserDto(user))
            .build();
    }

    /**
     * Token refresh (for API)
     */
    public TokenRefreshResponse refreshTokenApi(TokenRefreshRequest request) {
        AuthenticationResponse authResponse = refreshToken(request);
        
        return TokenRefreshResponse.success(
                authResponse.getAccessToken(),
                authResponse.getRefreshToken(),
                authResponse.getTokenType(),
                authResponse.getExpiresIn(),
                Instant.now().plusSeconds(authResponse.getExpiresIn())
        );
    }

    /**
     * Logout
     */
    public void logout(String refreshToken, String ipAddress, String userAgent) {
        log.info("Logout attempt");

        try {
            UUID userId = UUID.fromString(jwtUtil.getUserIdFromToken(refreshToken));
            
            // Delete session
            userSessionRepository.findByRefreshToken(refreshToken)
                .ifPresent(userSessionRepository::delete);

            // Record security log
            securityLogService.logAuthenticationAttempt(userId, "LOGOUT", 
                ipAddress, userAgent, "User logout");

            // Publish logout event
            eventPublishingService.publishUserEvent("USER_LOGOUT", userId, "User logged out");

        } catch (Exception e) {
            log.warn("Error during logout: {}", e.getMessage());
        }
    }

    /**
     * Invalidate all sessions
     */
    public void logoutAllSessions(UUID userId) {
        log.info("Logout all sessions for user: {}", userId);
        
        userSessionRepository.deleteByUserId(userId);
        
        // Publish event
        eventPublishingService.publishUserEvent("USER_LOGOUT_ALL", userId, "All sessions invalidated");
    }

    /**
     * Handle successful login
     */
    private AuthenticationResponse handleSuccessfulLogin(User user, String ipAddress, String userAgent) {
        // Reset login failure count
        user.setFailedLoginAttempts(0);
        user.setLastLogin(Instant.now());
        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId().toString());

        // Create session
        UserSession session = UserSession.builder()
            .id(UUID.randomUUID())
            .userId(user.getId())
            .refreshToken(refreshToken)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .createdAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(7L * 24 * 3600)) // 7 days
            .build();
        userSessionRepository.save(session);

        // Record security log
        securityLogService.logAuthenticationAttempt(user.getId(), "LOGIN_SUCCESS", 
            ipAddress, userAgent, "Successful login");

        // Publish login event
        eventPublishingService.publishUserEvent("USER_LOGIN", user.getId(), "User logged in");

        return AuthenticationResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(3600L)
            .user(convertToUserDto(user))
            .build();
    }

    /**
     * Handle failed login
     */
    private void handleFailedLogin(User user, String ipAddress, String userAgent) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        
        // Account lock check (lock after 5 failed attempts)
        if (user.getFailedLoginAttempts() >= 5) {
            user.setLocked(true);
            user.setLockedAt(Instant.now());
            securityLogService.logAuthenticationAttempt(user.getId(), "ACCOUNT_LOCKED", 
                ipAddress, userAgent, "Account locked due to failed attempts");
        }
        
        userRepository.save(user);
        
        securityLogService.logAuthenticationAttempt(user.getId(), "LOGIN_FAILED", 
            ipAddress, userAgent, "Invalid password");
    }

    /**
     * Convert User to UserDto
     */
    private UserDto convertToUserDto(User user) {
        return UserDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(user.getRole().name())
            .mfaEnabled(user.isMfaEnabled())
            .createdAt(user.getCreatedAt())
            .lastLogin(user.getLastLogin())
            .build();
    }

    /**
     * Implementation of Spring Security UserDetailsService interface
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getEmail())
            .password(user.getPasswordHash())
            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
            .accountExpired(false)
            .accountLocked(user.isLocked())
            .credentialsExpired(false)
            .disabled(!user.isActive())
            .build();
    }

    /**
     * User login (for new API endpoint)
     */
    public LoginResponse authenticateUser(LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        AuthenticationResponse authResponse = authenticate(request, ipAddress, userAgent);
        
        // Convert AuthenticationResponse to LoginResponse
        if (authResponse.isMfaRequired()) {
            return LoginResponse.mfaRequired(UUID.randomUUID().toString(), authResponse.getTempToken());
        }
        
        LoginResponse.TokenInfo tokenInfo = LoginResponse.TokenInfo.builder()
                .accessToken(authResponse.getAccessToken())
                .refreshToken(authResponse.getRefreshToken())
                .tokenType(authResponse.getTokenType())
                .expiresIn(authResponse.getExpiresIn())
                .expiresAt(Instant.now().plusSeconds(authResponse.getExpiresIn()))
                .build();
                
        LoginResponse.SessionInfo sessionInfo = LoginResponse.SessionInfo.builder()
                .sessionId(UUID.randomUUID()) // Get appropriate session ID
                .expiresAt(Instant.now().plusSeconds(7L * 24 * 3600))
                .deviceInfo(request.getDeviceInfo() != null ? request.getDeviceInfo().toString() : null)
                .build();
        
        return LoginResponse.success(authResponse.getUser(), tokenInfo, sessionInfo);
    }



    /**
     * Logout (for new API endpoint)
     */
    public LogoutResponse logout(String authHeader, HttpServletRequest request) {
        String token = extractTokenFromHeader(authHeader);
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        logout(token, ipAddress, userAgent);
        return LogoutResponse.success();
    }

    /**
     * Start OAuth flow
     */
    public String initiateOAuthFlow(String provider, String redirectUri) {
        // Generate OAuth URL for each provider
        switch (provider.toLowerCase()) {
            case "azure", "microsoft":
                return "/oauth2/authorization/azure";
            case "google":
                return "/oauth2/authorization/google";
            default:
                throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        }
    }

    /**
     * Handle OAuth callback
     */
    public LoginResponse handleOAuthCallback(String provider, OAuthCallbackRequest request, HttpServletRequest httpRequest) {
        // TODO: Implement provider-specific OAuth processing
        // Currently a placeholder implementation
        throw new UnsupportedOperationException("OAuth callback processing not implemented yet");
    }

    /**
     * Password reset request
     */
    public void requestPasswordReset(String email) {
        // TODO: Implement password reset functionality
        log.info("Password reset requested for email: {}", email);
        // Implement email sending process
    }

    /**
     * Execute password reset
     */
    public void resetPassword(String token, String newPassword) {
        // TODO: Implement password reset functionality
        log.info("Password reset execution");
        // Implement token validation and password update process
    }

    /**
     * Token validation
     */
    public java.util.Map<String, Object> validateToken(String authHeader) {
        String token = extractTokenFromHeader(authHeader);
        
        if (!jwtUtil.validateToken(token)) {
            throw new InvalidTokenException("Invalid token");
        }
        
        String userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        
        return java.util.Map.of(
                "valid", true,
                "userId", userId,
                "role", role,
                "expiresAt", jwtUtil.getExpirationFromToken(token)
        );
    }

    /**
     * Get current user information
     */
    public java.util.Map<String, Object> getCurrentUserInfo(String authHeader) {
        String token = extractTokenFromHeader(authHeader);
        String userId = jwtUtil.getUserIdFromToken(token);
        
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new AuthenticationException("User not found"));
        
        UserDto userDto = convertToUserDto(user);
        
        return java.util.Map.of(
                "user", userDto,
                "permissions", Collections.singletonList("ROLE_" + user.getRole().name())
        );
    }

    /**
     * Extract token from Authorization header
     */
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Invalid authorization header");
        }
        return authHeader.substring(7);
    }

    /**
     * Get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
