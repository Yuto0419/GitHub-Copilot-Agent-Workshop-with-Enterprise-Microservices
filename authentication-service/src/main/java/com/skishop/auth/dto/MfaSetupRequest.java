package com.skishop.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

/**
 * MFA Setup Request DTO
 * 
 * Request for setting up MFA for a user
 */
@Data
public class MfaSetupRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    private String accountName;
    private String issuer;
    
    // Manual getter methods since Lombok may not be working properly
    public String getUserId() { return userId; }
    public String getAccountName() { return accountName; }
    public String getIssuer() { return issuer; }
    
    public UUID getUserIdAsUUID() {
        return UUID.fromString(userId);
    }
}