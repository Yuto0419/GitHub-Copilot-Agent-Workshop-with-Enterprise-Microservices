package com.skishop.ai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Retry functionality configuration
 * 
 * <p>Enables Spring Retry to enhance error handling for AI services</p>
 * 
 * <h3>Target errors:</h3>
 * <ul>
 *   <li>Temporary connection errors with Azure OpenAI API</li>
 *   <li>Rate limit 429 errors</li>
 *   <li>Timeout errors</li>
 *   <li>Service unavailable (503) errors</li>
 * </ul>
 * 
 * <p>Retry configuration is defined in each service class using {@code @Retryable} annotation</p>
 * 
 * @since 1.0.0
 * @see org.springframework.retry.annotation.Retryable
 * @see com.skishop.ai.service.EnhancedAiServiceExecutor
 */
@Configuration
@EnableRetry
public class RetryConfig {
    // Basic Spring Retry configuration is sufficient with annotations
    // If more advanced configuration is needed, customize here
    
    /*
     * Future extension examples:
     * - Custom RetryTemplate Bean definition
     * - Exponential backoff strategy configuration
     * - Retry statistics collection setup
     * - Fine-grained control of retry target exceptions
     */
}
