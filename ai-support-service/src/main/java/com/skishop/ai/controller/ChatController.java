package com.skishop.ai.controller;

import com.skishop.ai.dto.ChatMessageRequest;
import com.skishop.ai.dto.ChatMessageResponse;
import com.skishop.ai.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * Chatbot API Controller
 * 
 * <p>AI chat functionality using LangChain4j 1.1.0 + Azure OpenAI</p>
 * 
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat API", description = "AI Chatbot API")
public class ChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    private final ChatService chatService;
    
    /**
     * Constructor
     * 
     * @param chatService Chat service
     */
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }
    
    /**
     * Send chat message
     */
    @PostMapping("/message")
    @Operation(summary = "Send chat message", description = "Process user messages and return AI responses")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @Valid @RequestBody ChatMessageRequest request) {
        
        logger.info("Received chat message from user: {}", request.userId());
        
        // Error handling is already done in ChatService,
        // so the controller just returns the result as is
        // AiServiceException is handled by GlobalExceptionHandler
        ChatMessageResponse response = chatService.processMessage(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Product recommendation chat
     */
    @PostMapping("/recommend")
    @Operation(summary = "Product recommendation chat", description = "AI chat specialized for product recommendations")
    public ResponseEntity<ChatMessageResponse> recommendProducts(
            @Valid @RequestBody ChatMessageRequest request) {
        
        logger.info("Processing product recommendation for user: {}", request.userId());
        
        // Set recommendation flag in request - create new instance since record is immutable
        var contextWithIntent = new HashMap<String, Object>();
        if (request.context() != null) {
            contextWithIntent.putAll(request.context());
        }
        contextWithIntent.put("forcedIntent", "PRODUCT_RECOMMENDATION");
        
        var enhancedRequest = new ChatMessageRequest(
            request.userId(),
            request.content(),
            request.conversationId(),
            request.sessionId(),
            contextWithIntent
        );
        
        ChatMessageResponse response = chatService.processMessage(enhancedRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Technical advice chat
     */
    @PostMapping("/advice")
    @Operation(summary = "Technical advice chat", description = "AI chat specialized for skiing technique advice")
    public ResponseEntity<ChatMessageResponse> provideTechnicalAdvice(
            @Valid @RequestBody ChatMessageRequest request) {
        
        logger.info("Processing technical advice for user: {}", request.userId());
        
        // Set technical advice flag in request - create new instance since record is immutable
        var contextWithIntent = new HashMap<String, Object>();
        if (request.context() != null) {
            contextWithIntent.putAll(request.context());
        }
        contextWithIntent.put("forcedIntent", "TECHNICAL_ADVICE");
        
        var enhancedRequest = new ChatMessageRequest(
            request.userId(),
            request.content(),
            request.conversationId(),
            request.sessionId(),
            contextWithIntent
        );
        
        ChatMessageResponse response = chatService.processMessage(enhancedRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get conversation history
     */
    @GetMapping("/conversations/{userId}")
    @Operation(summary = "Get conversation history", description = "Get conversation history for specified user")
    public ResponseEntity<List<ChatMessageResponse>> getConversationHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "10") int limit) {
        
        logger.info("Getting conversation history for user: {}", userId);
        
        // TODO: ChatSessionRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit))
        // and ChatMessage.findBySessionIdOrderByCreatedAtAsc(sessionId) to get history
        return ResponseEntity.ok(List.of()); // Temporarily return empty list
    }
    
    /**
     * Delete conversation
     */
    @DeleteMapping("/conversations/{conversationId}")
    @Operation(summary = "Delete conversation", description = "Delete specified conversation")
    public ResponseEntity<Void> deleteConversation(@PathVariable String conversationId) {
        
        logger.info("Deleting conversation: {}", conversationId);
        
        // TODO: ChatSessionRepository.deleteById(conversationId) and
        // related ChatMessage CASCADE delete or manual delete
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Submit chat feedback
     */
    @PostMapping("/feedback")
    @Operation(summary = "Chat feedback", description = "Submit user feedback for chat")
    public ResponseEntity<Void> submitFeedback(
            @RequestParam String conversationId,
            @RequestParam int rating,
            @RequestParam(required = false) String comment) {
        
        logger.info("Received feedback for conversation: {} with rating: {}", conversationId, rating);
        
        // TODO: Create ChatFeedback entity and save with repository
        // ChatFeedback.builder().conversationId(conversationId).rating(rating).comment(comment).build()
        return ResponseEntity.ok().build();
    }
}
