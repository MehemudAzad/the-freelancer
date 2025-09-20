package com.thefreelancer.microservices.ai_service.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thefreelancer.microservices.ai_service.service.ChatbotService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for RAG-based chatbot interactions with Redis conversational memory
 */
@RestController
@RequestMapping("/api/ai/chatbot")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Chatbot with Redis Memory", description = "RAG-based chatbot with conversational memory")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ChatbotController {
    
    private final ChatbotService chatbotService;
    
    @Operation(
        summary = "Send message to chatbot with conversation memory",
        description = "Chat with AI assistant that remembers previous conversation context using Redis cache"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Response generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request - message is required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        try {
            String userMessage = (String) request.get("message");
            String sessionId = (String) request.get("sessionId");
            String userId = (String) request.get("userId"); // Optional
            
            // Validate input
            if (userMessage == null || userMessage.trim().isEmpty()) {
                log.warn("Empty message received in chat request");
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "Message is required",
                        "timestamp", Instant.now().toString()
                    ));
            }
            
            // Trim and limit message length for safety
            userMessage = userMessage.trim();
            if (userMessage.length() > 1000) {
                userMessage = userMessage.substring(0, 1000);
                log.info("Truncated long message to 1000 characters");
            }
            
            // Generate session ID if not provided
            if (sessionId == null || sessionId.trim().isEmpty()) {
                if (userId != null) {
                    // For authenticated users, use user-based session
                    sessionId = "user_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8);
                } else {
                    // For anonymous users, generate random session
                    sessionId = "anon_" + UUID.randomUUID().toString();
                }
            }
            
            log.info("Received chat message: {} for session: {}", userMessage, sessionId);
            
            // Generate response with memory
            Map<String, Object> response = chatbotService.generateResponseWithMemory(userMessage, sessionId, userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in chatbot chat endpoint", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "I'm experiencing some technical difficulties. Please try again later.",
                    "timestamp", Instant.now().toString(),
                    "status", "error"
                ));
        }
    }
    
    @Operation(
        summary = "Get suggested questions",
        description = "Get a list of suggested questions users can ask the chatbot"
    )
    @ApiResponse(responseCode = "200", description = "Suggested questions retrieved successfully")
    @GetMapping("/suggestions")
    public ResponseEntity<Map<String, Object>> getSuggestions() {
        try {
            List<String> suggestions = chatbotService.getSuggestedQuestions();
            
            return ResponseEntity.ok(Map.of(
                "suggestions", suggestions,
                "timestamp", Instant.now().toString(),
                "status", "success"
            ));
            
        } catch (Exception e) {
            log.error("Error getting chatbot suggestions", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Failed to get suggestions",
                    "timestamp", Instant.now().toString(),
                    "status", "error"
                ));
        }
    }
    
    @Operation(
        summary = "Get conversation info",
        description = "Get information about the current Redis-cached conversation"
    )
    @GetMapping("/conversation/{sessionId}")
    public ResponseEntity<Map<String, Object>> getConversationInfo(
            @Parameter(description = "Session ID") @PathVariable String sessionId) {
        try {
            Map<String, Object> conversationInfo = chatbotService.getConversationInfo(sessionId);
            return ResponseEntity.ok(conversationInfo);
        } catch (Exception e) {
            log.error("Error getting conversation info for session: {}", sessionId, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to load conversation info"));
        }
    }
    
    @Operation(
        summary = "Clear conversation",
        description = "Clear the Redis-cached conversation history for a session"
    )
    @DeleteMapping("/conversation/{sessionId}")
    public ResponseEntity<Map<String, String>> clearConversation(
            @Parameter(description = "Session ID") @PathVariable String sessionId) {
        try {
            chatbotService.clearConversation(sessionId);
            return ResponseEntity.ok(Map.of(
                "message", "Conversation cleared successfully",
                "sessionId", sessionId
            ));
        } catch (Exception e) {
            log.error("Error clearing conversation for session: {}", sessionId, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to clear conversation"));
        }
    }
    
    @Operation(
        summary = "Health check for chatbot service",
        description = "Check if the chatbot service is operational"
    )
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            // Test basic functionality with memory
            String testSessionId = "health_check_" + System.currentTimeMillis();
            Map<String, Object> testResponse = chatbotService.generateResponseWithMemory("Hello", testSessionId, null);
            boolean isHealthy = testResponse != null && testResponse.get("response") != null;
            
            // Clean up test session
            chatbotService.clearConversation(testSessionId);
            
            return ResponseEntity.ok(Map.of(
                "status", isHealthy ? "UP" : "DOWN",
                "timestamp", Instant.now().toString(),
                "service", "AI Service with Redis Memory",
                "message", isHealthy ? "Service is operational" : "Service is experiencing issues"
            ));
            
        } catch (Exception e) {
            log.error("Health check failed", e);
            return ResponseEntity.status(503)
                .body(Map.of(
                    "status", "DOWN",
                    "timestamp", Instant.now().toString(),
                    "service", "AI Service with Redis Memory",
                    "message", "Service is not operational",
                    "error", e.getMessage()
                ));
        }
    }
}