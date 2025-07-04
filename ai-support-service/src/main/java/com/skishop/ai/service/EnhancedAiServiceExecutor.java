package com.skishop.ai.service;

import com.skishop.ai.exception.AiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * AI Service Execution Platform
 * 
 * <p>Provides error handling, retry, and fallback functionality for Azure OpenAI + LangChain4j</p>
 * <p>Leverages the latest Java 21 features</p>
 * 
 * @since 1.0.0
 */
@Service
public class EnhancedAiServiceExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedAiServiceExecutor.class);
    private static final int MAX_INPUT_LENGTH = 8000;
    private static final Duration AI_TIMEOUT = Duration.ofSeconds(30);
    
    /**
     * Fallback message using Java 21's Text Blocks feature
     */
    private static final String FALLBACK_MESSAGE = """
        We apologize, but the AI service is temporarily unavailable.
        Please try again after some time.
        
        If urgent, please contact us through:
        - Phone: 0120-XXX-XXX (Weekdays 9:00-18:00)
        - Email: support@skishop.com
        """;
    
    /**
     * Error type classification using Java 21's sealed interfaces
     */
    public sealed interface ErrorType 
        permits ErrorType.ConnectionError, ErrorType.TimeoutError, 
                ErrorType.ValidationError, ErrorType.ServiceError {
        
        record ConnectionError(String message, Throwable cause) implements ErrorType {}
        record TimeoutError(Duration timeout) implements ErrorType {}
        record ValidationError(String field, String issue) implements ErrorType {}
        record ServiceError(String service, String errorCode) implements ErrorType {}
        
        /**
         * Create AiServiceException based on error type
         */
        default AiServiceException toAiServiceException() {
            if (this instanceof ConnectionError(var message, var cause)) {
                return AiServiceException.connectionFailed(message, cause);
            } else if (this instanceof TimeoutError(var timeout)) {
                return AiServiceException.connectionFailed(
                    "Request timeout after " + timeout.toSeconds() + " seconds", null);
            } else if (this instanceof ValidationError(var field, var issue)) {
                return AiServiceException.validationFailed("Validation failed for " + field + ": " + issue);
            } else if (this instanceof ServiceError(var service, var errorCode)) {
                return AiServiceException.serviceUnavailable(
                    "Service " + service + " error: " + errorCode);
            }
            throw new IllegalStateException("Unknown error type: " + this.getClass());
        }
    }
    
    /**
     * Execute AI service with retry
     * 
     * @param aiServiceCall AI service call function
     * @param inputText Input text
     * @param context Context information
     * @return AI response
     */
    @Retryable(
        retryFor = {AiServiceException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 5000)
    )
    public String executeWithRetry(
            AiServiceCall aiServiceCall,
            String inputText,
            String context) {
        
        logger.debug("Executing AI service call with input length: {}", inputText.length());
        
        try {
            // Input validation (leveraging Java 21's pattern matching)
            validateInput(inputText);
            
            // Execution with timeout (leveraging Java 21's var type inference)
            var future = CompletableFuture.supplyAsync(() -> {
                try {
                    return aiServiceCall.execute(inputText, context);
                } catch (Exception e) {
                    throw mapToAiServiceException(e);
                }
            });
            
            var result = future.get(AI_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            
            // Response validation
            validateResponse(result);
            
            logger.debug("AI service call completed successfully, response length: {}", result.length());
            return result;
            
        } catch (java.util.concurrent.TimeoutException e) {
            logger.error("AI service call timed out after {} seconds", AI_TIMEOUT.toSeconds());
            var timeoutError = new ErrorType.TimeoutError(AI_TIMEOUT);
            throw timeoutError.toAiServiceException();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("AI service call was interrupted", e);
            var connectionError = new ErrorType.ConnectionError("Thread was interrupted", e);
            throw connectionError.toAiServiceException();
        } catch (Exception e) {
            // Java 21's instanceof pattern matching
            if (e instanceof AiServiceException aiEx) {
                throw aiEx;
            }
            throw mapToAiServiceException(e);
        }
    }
    
    /**
     * Fallback processing
     */
    @Recover
    public String fallback(AiServiceException ex, AiServiceCall aiServiceCall, String inputText, String context) {
        logger.error("AI service call failed after retries, falling back. Error: {}", ex.getMessage());
        
        // Record metrics
        recordFailureMetrics(ex);
        
        // Return basic fallback message
        return FALLBACK_MESSAGE;
    }
    
    /**
     * Input validation (leveraging Java 21 features)
     */
    private void validateInput(String inputText) {
        if (inputText == null || inputText.isBlank()) {
            var error = new ErrorType.ValidationError("inputText", "cannot be null or blank");
            throw error.toAiServiceException();
        }
        
        if (inputText.length() > MAX_INPUT_LENGTH) {
            var error = new ErrorType.ValidationError("inputText", 
                "exceeds maximum length of " + MAX_INPUT_LENGTH + " characters");
            throw error.toAiServiceException();
        }
    }
    
    /**
     * Response validation
     */
    private void validateResponse(String response) {
        if (response == null || response.isBlank()) {
            var error = new ErrorType.ServiceError("AI Service", "EMPTY_RESPONSE");
            throw error.toAiServiceException();
        }
    }
    
    /**
     * Exception mapping (leveraging Java 21's Switch Expressions)
     */
    private AiServiceException mapToAiServiceException(Exception e) {
        return switch (e) {
            case IllegalArgumentException ex -> AiServiceException.validationFailed(ex.getMessage());
            case java.net.ConnectException ex -> AiServiceException.connectionFailed("Connection failed", ex);
            case java.net.SocketTimeoutException ex -> AiServiceException.connectionFailed("Socket timeout", ex);
            case RuntimeException ex when ex.getMessage() != null && ex.getMessage().toLowerCase().contains("rate limit") ->
                AiServiceException.rateLimitExceeded("API rate limit exceeded");
            case RuntimeException ex when ex.getMessage() != null && ex.getMessage().toLowerCase().contains("quota") ->
                AiServiceException.quotaExceeded("API quota exceeded");
            default -> AiServiceException.internalError("Unexpected error: " + e.getMessage(), e);
        };
    }
    
    /**
     * Record failure metrics
     */
    private void recordFailureMetrics(AiServiceException ex) {
        // In actual implementation, use metrics libraries like Micrometer
        logger.warn("AI service failure metrics - Error Type: {}, Error Code: {}, Retryable: {}", 
                ex.getClass().getSimpleName(),
                ex.getErrorCode(),
                ex.isRetryable());
    }
    
    /**
     * AI service call functional interface
     */
    @FunctionalInterface
    public interface AiServiceCall {
        String execute(String inputText, String context) throws AiServiceException;
    }
}
