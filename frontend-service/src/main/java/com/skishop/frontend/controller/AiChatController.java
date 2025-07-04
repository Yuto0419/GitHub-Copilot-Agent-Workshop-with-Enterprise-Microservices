package com.skishop.frontend.controller;

import com.skishop.frontend.service.AiChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Controller for AI consultation chat feature.
 */
@Controller
public class AiChatController {

    private static final Logger log = LoggerFactory.getLogger(AiChatController.class);
    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    /**
     * Display AI consultation chat screen
     */
    @GetMapping("/ai-chat")
    public String aiChat(Model model) {
        model.addAttribute("pageTitle", "AI Chat - Azure SkiShop");
        model.addAttribute("isAdminPage", false);
        
        // Alert variables initialization
        model.addAttribute("success", "");
        model.addAttribute("error", "");
        model.addAttribute("info", "");
        model.addAttribute("warning", "");
        
        return "ai-chat/chat";
    }

    /**
     * Send AI consultation chat message
     */
    @PostMapping("/api/ai-chat/send")
    @ResponseBody
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> request) {
        try {
            // Receive request in ChatMessageRequest format
            String userId = (String) request.get("userId");
            String content = (String) request.get("content");
            String conversationId = (String) request.get("conversationId");
            String sessionId = (String) request.get("sessionId");
            
            // Support "message" field for backward compatibility
            if (content == null) {
                content = (String) request.get("message");
            }

            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message is empty"));
            }

            // Set default user ID
            if (userId == null || userId.trim().isEmpty()) {
                userId = "anonymous-user";
            }

            // Call AI support service API
            Map<String, Object> response = aiChatService.sendMessage(userId, content, conversationId, sessionId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("AI chat message processing failed", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Sorry, the AI service is currently unavailable. Please try again later.",
                    "message", "We apologize, but the AI assistant is currently unavailable. If you need immediate assistance, please view products directly from the product page or contact us by phone.",
                    "content", "We apologize, but the AI assistant is currently unavailable. If you need immediate assistance, please view products directly from the product page or contact us by phone."
                ));
        }
    }

    /**
     * Get conversation history
     */
    @GetMapping("/api/ai-chat/conversations")
    @ResponseBody
    public ResponseEntity<?> getConversations(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "User ID is required"));
            }

            Map<String, Object> conversations = aiChatService.getConversations(userId);
            return ResponseEntity.ok(conversations);
            
        } catch (Exception e) {
            log.error("Failed to get conversations", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to retrieve conversation history"));
        }
    }
}
