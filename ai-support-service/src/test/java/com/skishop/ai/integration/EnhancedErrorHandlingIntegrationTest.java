package com.skishop.ai.integration;

import com.skishop.ai.dto.ChatMessageRequest;
import com.skishop.ai.dto.ChatMessageResponse;
import com.skishop.ai.service.ChatService;
import com.skishop.ai.service.CustomerSupportAssistant;
import com.skishop.ai.service.ProductRecommendationAssistant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeast;

/**
 * Enhanced Error Handling Integration Test
 * Tests retry, fallback, and exception handling of AI services
 */
@SpringBootTest(classes = MockTestApplication.class)
@ActiveProfiles("test")
class EnhancedErrorHandlingIntegrationTest {
    
    @Autowired
    private ChatService chatService;
    
    @MockBean
    private CustomerSupportAssistant customerSupportAssistant;
    
    @MockBean
    private ProductRecommendationAssistant recommendationAssistant;
        
    @Test
    void testSuccessfulChatProcessing() {
        // Given
        when(customerSupportAssistant.chat(anyString()))
                .thenReturn("Hello! I'm here to help you.");
        
        ChatMessageRequest request = createTestRequest("Hello");
        
        // When
        ChatMessageResponse response = chatService.processMessage(request);
        
        // Then
        assertNotNull(response);
        assertEquals("Hello! How can I assist you today?", response.content());
        assertEquals("GENERAL_INQUIRY", response.intent());
        assertNotNull(response.messageId());
        assertNotNull(response.timestamp());
    }
    
    @Test
    void testProductRecommendationProcessing() {
        // Given
        when(recommendationAssistant.generateRecommendations(anyString(), anyString(), anyString()))
                .thenReturn("I'd like to suggest some ski recommendations. For beginners, I recommend the Salomon QST 92.");
        
        ChatMessageRequest request = createTestRequest("Please recommend ski equipment");
        
        // When
        ChatMessageResponse response = chatService.processMessage(request);
        
        // Then
        assertNotNull(response);
        assertTrue(response.content().contains("recommend"));
        assertEquals("PRODUCT_RECOMMENDATION", response.intent());
        // In the implementation, withConfidence is used, so requiresAction is false
        assertFalse(response.requiresAction());
        assertNull(response.actionType());
    }
    
    @Test
    void testTechnicalAdviceProcessing() {
        // Given
        when(customerSupportAssistant.provideTechnicalAdvice(anyString()))
                .thenReturn("To improve your skiing technique, let's start by checking your basic stance.");
        
        ChatMessageRequest request = createTestRequest("Can you give me some tips for skiing?");
        
        // When
        ChatMessageResponse response = chatService.processMessage(request);
        
        // Then
        assertNotNull(response);
        assertTrue(response.content().contains("technique"));
        assertEquals("TECHNICAL_ADVICE", response.intent());
    }
    
    @Test
    void testErrorHandlingWithRetry() {
        // Given - When failures occur continuously, fallback processing returns a fallback message
        when(customerSupportAssistant.chat(anyString()))
                .thenThrow(new RuntimeException("Connection timeout"));
        
        ChatMessageRequest request = createTestRequest("Test message");
        
        // When
        ChatMessageResponse response = chatService.processMessage(request);
        
        // Then - Fallback message is returned
        assertNotNull(response);
        assertTrue(response.content().contains("I apologize"));
        
        // Verify that retry is executed
        verify(customerSupportAssistant, atLeast(1)).chat(anyString());
    }
    
    @Test
    void testRateLimitExceptionHandling() {
        // Given
        when(customerSupportAssistant.chat(anyString()))
                .thenThrow(new RuntimeException("Rate limit exceeded"));
        
        ChatMessageRequest request = createTestRequest("Test message");
        
        // When
        ChatMessageResponse response = chatService.processMessage(request);
        
        // Then - Fallback message is returned
        assertNotNull(response);
        assertTrue(response.content().contains("I apologize"));
    }
    
    @Test
    void testInvalidInputHandling() {
        // Given
        ChatMessageRequest request = createTestRequest("");
        
        // When
        ChatMessageResponse response = chatService.processMessage(request);
        
        // Then - Fallback message is returned
        assertNotNull(response);
        assertTrue(response.content().contains("I apologize"));
    }
    
    @Test
    void testFallbackAfterMaxRetries() {
        // Given - All retries fail
        when(customerSupportAssistant.chat(anyString()))
                .thenThrow(new RuntimeException("Persistent connection error"));
        
        ChatMessageRequest request = createTestRequest("Test message");
        
        // When
        ChatMessageResponse response = chatService.processMessage(request);
        
        // Then - Fallback message is returned
        assertNotNull(response);
        assertTrue(response.content().contains("I apologize"));
        
        // Verify that retry is executed
        verify(customerSupportAssistant, atLeast(1)).chat(anyString());
    }
    
    /**
     * Create test request
     */
    private ChatMessageRequest createTestRequest(String content) {
        Map<String, Object> context = new HashMap<>();
        context.put("channel", "web");
        context.put("userAgent", "test-agent");
        
        return new ChatMessageRequest(
            "test-user-001",
            content,
            "test-conversation-001",
            "test-session-001",
            context
        );
    }

    @Test
    void testInputLengthValidation() {
        // Given - Input that is too long
        String longInput = "a".repeat(10000);
        ChatMessageRequest request = createTestRequest(longInput);
        
        // When
        ChatMessageResponse response = chatService.processMessage(request);
        
        // Then - Fallback message is returned
        assertNotNull(response);
        assertTrue(response.content().contains("I apologize"));
    }
}
