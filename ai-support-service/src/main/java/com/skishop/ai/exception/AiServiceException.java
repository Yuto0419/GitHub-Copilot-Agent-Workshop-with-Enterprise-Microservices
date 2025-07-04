package com.skishop.ai.exception;

import lombok.Getter;

/**
 * AI Service Exception Class
 * Integrated error handling for LangChain4j + Azure OpenAI
 */
@Getter
public class AiServiceException extends RuntimeException {
    
    private final String errorCode;
    private final String userFriendlyMessage;
    private final boolean retryable;
    
    /**
     * Constructor
     */
    public AiServiceException(String errorCode, String message, String userFriendlyMessage, boolean retryable) {
        super(message);
        this.errorCode = errorCode;
        this.userFriendlyMessage = userFriendlyMessage;
        this.retryable = retryable;
    }
    
    public AiServiceException(String errorCode, String message, String userFriendlyMessage, Throwable cause, boolean retryable) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userFriendlyMessage = userFriendlyMessage;
        this.retryable = retryable;
    }
    
    /**
     * Azure OpenAI API rate limit error
     */
    public static AiServiceException rateLimitExceeded(String details) {
        return new AiServiceException(
            "RATE_LIMIT_EXCEEDED",
            "Azure OpenAI API rate limit exceeded: " + details,
            "Access is currently concentrated. Please wait a moment and try again.",
            true
        );
    }
    
    /**
     * Azure OpenAI connection error
     */
    public static AiServiceException connectionFailed(String details, Throwable cause) {
        return new AiServiceException(
            "CONNECTION_FAILED",
            "Failed to connect to Azure OpenAI: " + details,
            "Cannot connect to AI service. Please check your network connection.",
            cause,
            true
        );
    }
    
    /**
     * Authentication error
     */
    public static AiServiceException authenticationFailed(String details) {
        return new AiServiceException(
            "AUTHENTICATION_FAILED",
            "Azure OpenAI authentication failed: " + details,
            "Authentication failed. Please contact the system administrator.",
            false
        );
    }
    
    /**
     * Input validation error
     */
    public static AiServiceException invalidInput(String details) {
        return new AiServiceException(
            "INVALID_INPUT",
            "Invalid input provided: " + details,
            "There is a problem with the input content. Please check the content and try again.",
            false
        );
    }
    
    /**
     * Content filter error
     */
    public static AiServiceException contentFiltered(String details) {
        return new AiServiceException(
            "CONTENT_FILTERED",
            "Content was filtered by Azure OpenAI: " + details,
            "We apologize. The input content may not be appropriate. Please try a different expression.",
            false
        );
    }
    
    /**
     * Internal service error
     */
    public static AiServiceException internalError(String details, Throwable cause) {
        return new AiServiceException(
            "INTERNAL_ERROR",
            "Internal AI service error: " + details,
            "An error occurred inside the system. Please wait a moment and try again.",
            cause,
            true
        );
    }
    
    /**
     * Validation failure error
     */
    public static AiServiceException validationFailed(String details) {
        return new AiServiceException(
            "VALIDATION_FAILED",
            "Validation failed: " + details,
            "There is a problem with the input content. Please check the content and try again.",
            false
        );
    }
    
    /**
     * Service unavailable error
     */
    public static AiServiceException serviceUnavailable(String details) {
        return new AiServiceException(
            "SERVICE_UNAVAILABLE",
            "AI service is unavailable: " + details,
            "AI service is temporarily unavailable. Please wait a moment and try again.",
            true
        );
    }
    
    /**
     * Quota exceeded error
     */
    public static AiServiceException quotaExceeded(String details) {
        return new AiServiceException(
            "QUOTA_EXCEEDED",
            "API quota exceeded: " + details,
            "API usage limit has been reached. Please wait and try again later.",
            true
        );
    }
}
