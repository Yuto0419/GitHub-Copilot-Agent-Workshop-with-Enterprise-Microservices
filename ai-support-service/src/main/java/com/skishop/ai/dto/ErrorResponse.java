package com.skishop.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Error Response DTO
 * Returns REST API error information in a unified format
 * 
 * Immutable data class using Java 21's Record feature
 * @param timestamp Error occurrence time
 * @param status HTTP status code
 * @param error HTTP error name
 * @param message User-facing error message
 * @param errorCode Internal system error code
 * @param retryable Retryable flag
 * @param path Request path
 * @param details Detailed information (validation errors, etc.)
 * @param traceId Trace ID (for distributed tracing)
 */
@JsonInclude(Include.NON_NULL)
public record ErrorResponse(
    LocalDateTime timestamp,
    Integer status,
    String error,
    String message,
    String errorCode,
    Boolean retryable,
    String path,
    Map<String, Object> details,
    String traceId
) {
    
    /**
     * As an alternative to builder pattern, create instance with basic error information
     */
    public static ErrorResponse of(Integer status, String error, String message) {
        return new ErrorResponse(
            LocalDateTime.now(),
            status,
            error,
            message,
            null,
            false,
            null,
            null,
            null
        );
    }
    
    /**
     * Create a retryable error
     */
    public static ErrorResponse retryable(Integer status, String error, String message) {
        return new ErrorResponse(
            LocalDateTime.now(),
            status,
            error,
            message,
            null,
            true,
            null,
            null,
            null
        );
    }
    
    /**
     * Create an error with detailed information
     */
    public static ErrorResponse withDetails(Integer status, String error, String message, 
                                           Map<String, Object> details) {
        return new ErrorResponse(
            LocalDateTime.now(),
            status,
            error,
            message,
            null,
            false,
            null,
            details,
            null
        );
    }
}
