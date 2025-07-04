package com.skishop.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * Logout Response DTO
 * 
 * Response for successful logout
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogoutResponse {
    
    private boolean success;
    private String message;
    private String logoutUrl;  // Azure Entra ID logout URL
    
    /**
     * Create success response
     */
    public static LogoutResponse success() {
        return LogoutResponse.builder()
                .success(true)
                .message("Successfully logged out")
                .build();
    }
    
    /**
     * Create Azure AD logout success response
     */
    public static LogoutResponse success(String logoutUrl) {
        return LogoutResponse.builder()
                .success(true)
                .message("Successfully logged out")
                .logoutUrl(logoutUrl)
                .build();
    }
    
    /**
     * Create error response
     */
    public static LogoutResponse error(String message) {
        return LogoutResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
