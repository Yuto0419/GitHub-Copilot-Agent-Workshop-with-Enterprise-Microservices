package com.skishop.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Token Refresh Response DTO
 * 
 * Response for successful token refresh
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenRefreshResponse {
    
    private boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private Instant expiresAt;
    
    /**
     * Create success response
     */
    public static TokenRefreshResponse success(String accessToken, String refreshToken, 
                                               String tokenType, Long expiresIn, Instant expiresAt) {
        return TokenRefreshResponse.builder()
                .success(true)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .expiresIn(expiresIn)
                .expiresAt(expiresAt)
                .build();
    }
    
    /**
     * Create error response
     */
    public static TokenRefreshResponse error(String message) {
        return TokenRefreshResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
