package com.skishop.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * OAuth Callback Request DTO
 * 
 * Data transfer object for handling callbacks from OAuth providers
 */
@Data
public class OAuthCallbackRequest {
    
    @NotBlank(message = "Authorization code is required")
    private String code;
    
    private String state;  // For CSRF protection
    
    private String redirectUri;
    
    private String scope;
}
