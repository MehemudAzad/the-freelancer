package com.thefreelancer.microservices.ai_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Redis-based conversation service for managing chat history with TTL
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisConversationService {
    
    private final RedisTemplate<String, List<Map<String, Object>>> conversationRedisTemplate;
    
    private static final String CONVERSATION_KEY_PREFIX = "chat:session:";
    private static final Duration CONVERSATION_TTL = Duration.ofHours(2); // 2 hours of inactivity
    private static final int MAX_MESSAGES_IN_CONTEXT = 10; // Keep last 10 messages for context
    
    /**
     * Add a message to the conversation history
     */
    public void addMessage(String sessionId, String role, String content, Map<String, Object> metadata) {
        try {
            String key = getConversationKey(sessionId);
            
            // Get existing conversation or create new list
            List<Map<String, Object>> messages = conversationRedisTemplate.opsForValue().get(key);
            if (messages == null) {
                messages = new ArrayList<>();
            }
            
            // Create message object
            Map<String, Object> message = new HashMap<>();
            message.put("role", role);
            message.put("content", content);
            message.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            if (metadata != null) {
                message.put("metadata", metadata);
            }
            
            // Add new message
            messages.add(message);
            
            // Keep only the last N messages to avoid memory bloat
            if (messages.size() > MAX_MESSAGES_IN_CONTEXT * 2) { // Keep double for safety
                messages = messages.subList(messages.size() - MAX_MESSAGES_IN_CONTEXT, messages.size());
            }
            
            // Save back to Redis with TTL
            conversationRedisTemplate.opsForValue().set(key, messages, CONVERSATION_TTL);
            
            log.debug("Added {} message to session: {} (total: {} messages)", role, sessionId, messages.size());
            
        } catch (Exception e) {
            log.error("Failed to add message to Redis conversation for session: {}", sessionId, e);
        }
    }
    
    /**
     * Get conversation history for context
     */
    public List<Map<String, Object>> getConversationHistory(String sessionId, int limit) {
        try {
            String key = getConversationKey(sessionId);
            List<Map<String, Object>> messages = conversationRedisTemplate.opsForValue().get(key);
            
            if (messages == null || messages.isEmpty()) {
                return new ArrayList<>();
            }
            
            // Return the last N messages for context
            int fromIndex = Math.max(0, messages.size() - limit);
            List<Map<String, Object>> contextMessages = messages.subList(fromIndex, messages.size());
            
            log.debug("Retrieved {} messages for session: {} (requested: {})", 
                    contextMessages.size(), sessionId, limit);
            
            return contextMessages;
            
        } catch (Exception e) {
            log.error("Failed to get conversation history from Redis for session: {}", sessionId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Check if session has conversation history
     */
    public boolean hasConversationHistory(String sessionId) {
        try {
            String key = getConversationKey(sessionId);
            Boolean exists = conversationRedisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Failed to check conversation existence for session: {}", sessionId, e);
            return false;
        }
    }
    
    /**
     * Clear conversation history for a session
     */
    public void clearConversation(String sessionId) {
        try {
            String key = getConversationKey(sessionId);
            conversationRedisTemplate.delete(key);
            log.info("Cleared conversation history for session: {}", sessionId);
        } catch (Exception e) {
            log.error("Failed to clear conversation for session: {}", sessionId, e);
        }
    }
    
    /**
     * Get conversation statistics
     */
    public Map<String, Object> getConversationStats(String sessionId) {
        try {
            String key = getConversationKey(sessionId);
            List<Map<String, Object>> messages = conversationRedisTemplate.opsForValue().get(key);
            
            if (messages == null) {
                return Map.of(
                    "exists", false,
                    "messageCount", 0,
                    "ttlMinutes", 0
                );
            }
            
            // Get TTL
            Long ttlSeconds = conversationRedisTemplate.getExpire(key);
            long ttlMinutes = ttlSeconds != null ? ttlSeconds / 60 : 0;
            
            return Map.of(
                "exists", true,
                "messageCount", messages.size(),
                "ttlMinutes", ttlMinutes,
                "lastMessage", messages.get(messages.size() - 1).get("timestamp")
            );
            
        } catch (Exception e) {
            log.error("Failed to get conversation stats for session: {}", sessionId, e);
            return Map.of("exists", false, "error", e.getMessage());
        }
    }
    
    /**
     * Extend conversation TTL (reset expiration)
     */
    public void extendConversationTTL(String sessionId) {
        try {
            String key = getConversationKey(sessionId);
            if (conversationRedisTemplate.hasKey(key)) {
                conversationRedisTemplate.expire(key, CONVERSATION_TTL);
                log.debug("Extended TTL for conversation session: {}", sessionId);
            }
        } catch (Exception e) {
            log.error("Failed to extend TTL for session: {}", sessionId, e);
        }
    }
    
    private String getConversationKey(String sessionId) {
        return CONVERSATION_KEY_PREFIX + sessionId;
    }
}