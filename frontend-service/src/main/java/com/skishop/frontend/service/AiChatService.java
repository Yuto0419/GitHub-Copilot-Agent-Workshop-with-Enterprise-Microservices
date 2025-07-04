package com.skishop.frontend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.UUID;

/**
 * Service class responsible for integration with AI Support Service
 */
@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);

    @Value("${app.ai-support-service.base-url:http://localhost:8084}")
    private String aiServiceBaseUrl;

    private final RestTemplate restTemplate;

    public AiChatService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Send message to AI Support Service
     */
    public Map<String, Object> sendMessage(String userId, String message, String conversationId) {
        return sendMessage(userId, message, conversationId, null);
    }

    /**
     * Send message to AI Support Service (with session ID)
     */
    public Map<String, Object> sendMessage(String userId, String message, String conversationId, String sessionId) {
        try {
            String url = aiServiceBaseUrl + "/api/v1/chat/message";
            
            // Build request body in ChatMessageRequest format
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userId", userId != null ? userId : generateTempUserId());
            requestBody.put("content", message);  // Using "content" instead of "message"
            requestBody.put("conversationId", conversationId);
            requestBody.put("sessionId", sessionId);
            
            // Add context information
            Map<String, Object> context = new HashMap<>();
            context.put("channel", "web-chat");
            context.put("timestamp", System.currentTimeMillis());
            context.put("userAgent", "frontend-service");
            requestBody.put("context", context);

            // Set HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Call AI Support Service
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                request, 
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Process response in ChatMessageResponse format
                Map<String, Object> responseBody = response.getBody();
                Map<String, Object> frontendResponse = new HashMap<>();
                
                // Convert for frontend
                frontendResponse.put("message", responseBody.get("content"));
                frontendResponse.put("conversationId", responseBody.get("conversationId"));
                frontendResponse.put("messageId", responseBody.get("messageId"));
                frontendResponse.put("timestamp", responseBody.get("timestamp"));
                frontendResponse.put("intent", responseBody.get("intent"));
                frontendResponse.put("confidence", responseBody.get("confidence"));
                frontendResponse.put("context", responseBody.get("context"));
                
                return frontendResponse;
            } else {
                log.warn("AI service returned non-successful status: {}", response.getStatusCode());
                return createFallbackResponse(message);
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error calling AI service: {} - {}", e.getStatusCode(), e.getMessage());
            return createFallbackResponse(message);
        } catch (Exception e) {
            log.error("Unexpected error calling AI service", e);
            return createFallbackResponse(message);
        }
    }

    /**
     * Get user conversation history
     */
    public Map<String, Object> getConversations(String userId) {
        try {
            String url = aiServiceBaseUrl + "/api/v1/chat/conversations/" + userId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                request, 
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (Map<String, Object>) response.getBody();
            } else {
                log.warn("AI service returned non-successful status for conversations: {}", response.getStatusCode());
                return createEmptyConversationsResponse();
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error getting conversations from AI service: {}", e.getMessage());
            return createEmptyConversationsResponse();
        } catch (Exception e) {
            log.error("Unexpected error getting conversations from AI service", e);
            return createEmptyConversationsResponse();
        }
    }

    /**
     * Fallback response when AI service is unavailable
     */
    private Map<String, Object> createFallbackResponse(String userMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("conversationId", generateTempConversationId());
        response.put("messageId", generateTempMessageId());
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        response.put("content", generateFallbackText(userMessage)); // Using "content" field
        response.put("intent", "FALLBACK");
        response.put("confidence", 0.5);
        
        Map<String, Object> context = new HashMap<>();
        context.put("conversationState", "FALLBACK_MODE");
        context.put("serviceFallback", true);
        response.put("context", context);
        
        // Adding "message" field for frontend compatibility
        response.put("message", generateFallbackText(userMessage));

        return response;
    }

    /**
     * Generate fallback text
     */
    private String generateFallbackText(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        
        if (lowerMessage.contains("ski")) {
            return "I see you're asking about ski equipment! I'm sorry, but the AI assistant is temporarily unavailable. You can browse ski products directly from our product page.";
        } else if (lowerMessage.contains("snowboard")) {
            return "I'd like to answer your questions about snowboard equipment, but our system is currently under maintenance. Please check our snowboard product page or contact us by phone.";
        } else if (lowerMessage.contains("size")) {
            return "For sizing information, please refer to the size guide on each product page. For more detailed assistance, please contact our store staff.";
        } else if (lowerMessage.contains("price") || lowerMessage.contains("cost")) {
            return "For pricing information, please check the product pages for the latest prices. Sale information is also updated regularly.";
        } else {
            return "Thank you for your question. The AI assistant is temporarily unavailable, but please use our product pages or contact form for assistance.";
        }
    }

    /**
     * Empty conversation history response
     */
    private Map<String, Object> createEmptyConversationsResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("conversations", Arrays.asList());
        response.put("totalCount", 0);
        response.put("serviceFallback", true);
        return response;
    }

    /**
     * Generate temporary user ID
     */
    private String generateTempUserId() {
        return "guest-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generate temporary conversation ID
     */
    private String generateTempConversationId() {
        return "conv-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generate temporary message ID
     */
    private String generateTempMessageId() {
        return "msg-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
