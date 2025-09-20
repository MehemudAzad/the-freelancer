package com.thefreelancer.microservices.ai_service.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RAG-based chatbot service with Redis conversational memory for The Freelancer platform
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {
    
    private final ChatClient chatClient;
    private final KnowledgeBaseService knowledgeBaseService;
    private final RedisConversationService redisConversationService;
    
    /**
     * Generate a response using RAG (Retrieval Augmented Generation) - Legacy method for backward compatibility
     */
    public String generateResponse(String userQuery) {
        // Generate a random session for legacy calls
        String tempSessionId = "legacy_" + UUID.randomUUID().toString().substring(0, 8);
        Map<String, Object> response = generateResponseWithMemory(userQuery, tempSessionId, null);
        return (String) response.get("response");
    }
    
    /**
     * Generate a response using RAG with Redis conversational memory
     */
    public Map<String, Object> generateResponseWithMemory(String userQuery, String sessionId, String userId) {
        try {
            // Step 1: Extend conversation TTL (reset expiration on activity)
            redisConversationService.extendConversationTTL(sessionId);
            
            // Step 2: Retrieve conversation history from Redis (last 8 messages for context)
            List<Map<String, Object>> history = redisConversationService.getConversationHistory(sessionId, 8);
            
            // Step 3: Retrieve relevant knowledge using RAG
            List<Map<String, Object>> relevantContent = knowledgeBaseService.searchSimilarContent(userQuery, 3);
            
            // Step 4: Build conversation context for LLM
            List<Message> messages = buildConversationMessages(history, relevantContent, userQuery);
            
            // Step 5: Generate response using ChatClient with full context
            String response = chatClient.prompt()
                .messages(messages)
                .call()
                .content();
            
            // Step 6: Store the conversation in Redis
            // Clean the retrieved content to avoid serialization issues
            List<Map<String, Object>> cleanedContent = cleanKnowledgeContent(relevantContent);
            Map<String, Object> metadata = Map.of(
                "retrievedKnowledge", cleanedContent,
                "sessionId", sessionId,
                "knowledgeCount", relevantContent.size()
            );
            
            // Add user message and AI response to Redis
            redisConversationService.addMessage(sessionId, "user", userQuery, null);
            redisConversationService.addMessage(sessionId, "assistant", response, metadata);
            
            log.info("Generated response with Redis memory for session: {}", sessionId);
            
            return Map.of(
                "response", response,
                "sessionId", sessionId,
                "userId", userId != null ? userId : "anonymous",
                "timestamp", String.valueOf(System.currentTimeMillis()),
                "hasHistory", !history.isEmpty(),
                "knowledgeUsed", relevantContent.size(),
                "contextMessages", history.size()
            );
            
        } catch (Exception e) {
            log.error("Failed to generate response with Redis memory for query: {}", userQuery, e);
            return Map.of(
                "response", "I'm sorry, I'm experiencing some technical difficulties. Please try again later or contact our support team.",
                "error", true,
                "timestamp", String.valueOf(System.currentTimeMillis())
            );
        }
    }
    
    private List<Message> buildConversationMessages(List<Map<String, Object>> history, 
                                                  List<Map<String, Object>> relevantContent, 
                                                  String currentQuery) {
        
        List<Message> messages = new ArrayList<>();
        
        // System message with knowledge base context
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("You are a helpful assistant for The Freelancer platform. ");
        contextBuilder.append("Use the following knowledge base and conversation history to provide helpful responses.\n\n");
        
        if (!relevantContent.isEmpty()) {
            contextBuilder.append("**Knowledge Base Context:**\n");
            for (Map<String, Object> content : relevantContent) {
                contextBuilder.append("- ").append(content.get("title")).append(": ")
                            .append(content.get("content")).append("\n");
            }
            contextBuilder.append("\n");
        }
        
        contextBuilder.append("**Guidelines:**\n");
        contextBuilder.append("- Provide helpful, accurate information about The Freelancer platform\n");
        contextBuilder.append("- Reference previous conversation context when relevant\n");
        contextBuilder.append("- If you don't have specific information, direct users to support\n");
        contextBuilder.append("- Be conversational and remember what was discussed earlier\n");
        contextBuilder.append("- Keep responses concise but informative\n");
        
        messages.add(new SystemMessage(contextBuilder.toString()));
        
        // Add conversation history from Redis
        for (Map<String, Object> historyItem : history) {
            String role = (String) historyItem.get("role");
            String content = (String) historyItem.get("content");
            
            if ("user".equals(role)) {
                messages.add(new UserMessage(content));
            } else if ("assistant".equals(role)) {
                messages.add(new AssistantMessage(content));
            }
        }
        
        // Add current user query
        messages.add(new UserMessage(currentQuery));
        
        return messages;
    }
    
    /**
     * Generate a fallback response when the main RAG process fails
     */
    private String generateFallbackResponse(String userQuery) {
        try {
            // Try to generate a basic response without context
            String basicPrompt = """
                You are a helpful assistant for The Freelancer platform. 
                The user asked: "%s"
                
                Provide a helpful response about The Freelancer platform. If you don't have specific information, 
                guide them to contact support or explore the platform features.
                """.formatted(userQuery);
                
            return chatClient.prompt()
                .user(basicPrompt)
                .call()
                .content();
                
        } catch (Exception e) {
            log.error("Fallback response generation also failed", e);
            return createStaticFallbackResponse(userQuery);
        }
    }
    
    /**
     * Create a static fallback response when all else fails
     */
    private String createStaticFallbackResponse(String userQuery) {
        String lowerQuery = userQuery.toLowerCase();
        
        if (lowerQuery.contains("job") || lowerQuery.contains("post")) {
            return "To post a job on The Freelancer platform, sign up as a client, click 'Post a Job', select a template, define your requirements, and publish. If you need more help, please contact our support team.";
        } else if (lowerQuery.contains("freelancer") || lowerQuery.contains("work")) {
            return "As a freelancer, you can create a profile, showcase your skills, browse available jobs, and submit proposals. Start by completing your profile and creating gigs to attract clients.";
        } else if (lowerQuery.contains("payment") || lowerQuery.contains("money")) {
            return "We use a secure escrow system where funds are held safely and released when milestones are completed. This protects both clients and freelancers.";
        } else if (lowerQuery.contains("help") || lowerQuery.contains("support")) {
            return "I'm here to help you with The Freelancer platform! You can ask me about posting jobs, finding freelancers, payments, or any other platform features. For technical issues, please contact our support team.";
        } else {
            return "I'm here to help you with The Freelancer platform! You can ask me about posting jobs, finding freelancers, payments, milestones, or any other platform features. If you need specific technical assistance, please contact our support team.";
        }
    }
    
    /**
     * Get suggested questions for users
     */
    public List<String> getSuggestedQuestions() {
        return List.of(
            "How do I post a job?",
            "How do I get started as a freelancer?",
            "How do payments work?",
            "What are milestones?",
            "How do I find the right freelancer?",
            "How do I create a good proposal?",
            "What features does the workspace have?",
            "How do I handle disputes?",
            "What are the platform fees?",
            "How do I build a good profile?"
        );
    }
    
    /**
     * Get conversation information for a session
     */
    public Map<String, Object> getConversationInfo(String sessionId) {
        try {
            Map<String, Object> stats = redisConversationService.getConversationStats(sessionId);
            List<Map<String, Object>> recentMessages = redisConversationService.getConversationHistory(sessionId, 5);
            
            return Map.of(
                "sessionId", sessionId,
                "stats", stats,
                "recentMessages", recentMessages.size(),
                "hasActiveConversation", redisConversationService.hasConversationHistory(sessionId)
            );
        } catch (Exception e) {
            log.error("Failed to get conversation info for session: {}", sessionId, e);
            return Map.of("error", "Failed to load conversation info");
        }
    }
    
    /**
     * Clear conversation for a session
     */
    public void clearConversation(String sessionId) {
        try {
            redisConversationService.clearConversation(sessionId);
            log.info("Cleared conversation for session: {}", sessionId);
        } catch (Exception e) {
            log.error("Failed to clear conversation for session: {}", sessionId, e);
        }
    }
    
    /**
     * Clean knowledge content to avoid Redis serialization issues
     */
    private List<Map<String, Object>> cleanKnowledgeContent(List<Map<String, Object>> content) {
        List<Map<String, Object>> cleaned = new ArrayList<>();
        
        for (Map<String, Object> item : content) {
            Map<String, Object> cleanedItem = new HashMap<>();
            
            // Only keep serializable fields
            if (item.get("title") != null) {
                cleanedItem.put("title", item.get("title").toString());
            }
            if (item.get("content") != null) {
                cleanedItem.put("content", item.get("content").toString());
            }
            if (item.get("content_type") != null) {
                cleanedItem.put("content_type", item.get("content_type").toString());
            }
            if (item.get("similarity_score") != null) {
                cleanedItem.put("similarity_score", item.get("similarity_score"));
            }
            
            // Clean tags array if present
            Object tagsObj = item.get("tags");
            if (tagsObj != null) {
                try {
                    if (tagsObj instanceof String) {
                        cleanedItem.put("tags", tagsObj.toString());
                    } else if (tagsObj instanceof List) {
                        List<String> tagsList = new ArrayList<>();
                        for (Object tag : (List<?>) tagsObj) {
                            if (tag != null) {
                                tagsList.add(tag.toString());
                            }
                        }
                        cleanedItem.put("tags", tagsList);
                    }
                } catch (Exception e) {
                    log.debug("Failed to process tags, skipping: {}", e.getMessage());
                }
            }
            
            cleaned.add(cleanedItem);
        }
        
        return cleaned;
    }
}