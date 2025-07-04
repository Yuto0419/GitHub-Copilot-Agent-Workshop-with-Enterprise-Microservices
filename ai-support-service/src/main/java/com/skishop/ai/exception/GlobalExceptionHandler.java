package com.skishop.ai.exception;

import com.skishop.ai.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * Global Exception Handler
 * 
 * <p>Unified error response processing for REST API</p>
 * 
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * AI Service Exception Handling
     */
    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ErrorResponse> handleAiServiceException(
            AiServiceException ex, WebRequest request) {
        
        logger.error("AI Service Exception: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);
        
        HttpStatus status = getHttpStatusForAiError(ex.getErrorCode());
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getUserFriendlyMessage(),
                ex.getErrorCode(),
                ex.isRetryable(),
                request.getDescription(false).replace("uri=", ""),
                null,
                null
        );
        
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    /**
     * Validation Exception Handling
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        var validationErrors = new HashMap<String, Object>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            var fieldName = ((FieldError) error).getField();
            var errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        
        logger.warn("Validation failed: {}", validationErrors);
        
        ErrorResponse errorResponse = ErrorResponse.withDetails(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "There is a problem with the input values.",
                validationErrors
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * General Exception Handling
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        
        logger.error("Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An error occurred inside the system. Please contact the administrator."
        );
        
        return ResponseEntity.internalServerError().body(errorResponse);
    }
    
    /**
     * Determine HTTP status based on AI error code
     */
    private HttpStatus getHttpStatusForAiError(String errorCode) {
        return switch (errorCode) {
            case "RATE_LIMIT_EXCEEDED" -> HttpStatus.TOO_MANY_REQUESTS;
            case "AUTHENTICATION_FAILED" -> HttpStatus.UNAUTHORIZED;
            case "INVALID_INPUT", "CONTENT_FILTERED" -> HttpStatus.BAD_REQUEST;
            case "CONNECTION_FAILED", "INTERNAL_ERROR" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
