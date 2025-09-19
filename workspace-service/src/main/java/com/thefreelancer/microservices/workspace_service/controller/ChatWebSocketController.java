package com.thefreelancer.microservices.workspace_service.controller;

import com.thefreelancer.microservices.workspace_service.dto.*;
import com.thefreelancer.microservices.workspace_service.service.DirectMessageService;
import com.thefreelancer.microservices.workspace_service.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for real-time chat functionality
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final MessageService messageService;
    private final DirectMessageService directMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle incoming chat messages
     */
    @MessageMapping("/room/{roomId}/sendMessage")
    @SendTo("/topic/room/{roomId}/messages")
    public MessageResponseDto sendMessage(
            @DestinationVariable String roomId,
            @Payload MessageCreateDto messageDto,
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            // Extract user information from session
            if (headerAccessor.getSessionAttributes() == null) {
                log.warn("Session attributes not found");
                throw new IllegalArgumentException("Authentication required");
            }
            
            String userId = (String) headerAccessor.getSessionAttributes().get("userId");
            
            if (userId == null) {
                log.warn("User ID not found in session attributes");
                throw new IllegalArgumentException("Authentication required");
            }
            
            log.info("Received WebSocket message for room: {} from user: {}", roomId, userId);
            
            // Parse and set sender ID from authenticated user
            try {
                Long userIdLong = Long.parseLong(userId);
                messageDto.setSenderId(userIdLong);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid user ID: " + userId);
            }
            
            // Parse roomId to Long
            Long roomIdLong;
            try {
                roomIdLong = Long.parseLong(roomId);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid room ID: " + roomId);
            }
            
            // Send message through service (this also saves to database)
            MessageResponseDto response = messageService.sendMessage(roomIdLong, userId, messageDto);
            
            log.info("Message sent successfully via WebSocket: {}", response.getId());
            return response;
            
        } catch (Exception e) {
            log.error("Error processing WebSocket message: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Handle typing indicators
     */
    @MessageMapping("/room/{roomId}/typing")
    @SendTo("/topic/room/{roomId}/typing")
    public TypingStatusDto handleTyping(
            @DestinationVariable String roomId,
            @Payload TypingStatusDto typingStatus,
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            if (headerAccessor.getSessionAttributes() != null) {
                String userId = (String) headerAccessor.getSessionAttributes().get("userId");
                
                if (userId != null) {
                    try {
                        Long userIdLong = Long.parseLong(userId);
                        typingStatus.setUserId(userIdLong);
                        typingStatus.setRoomId(roomId);
                        
                        log.debug("User {} typing status: {} in room: {}", userId, typingStatus.isTyping(), roomId);
                        return typingStatus;
                    } catch (Exception e) {
                        log.warn("Invalid user ID: {}", userId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error handling typing indicator: {}", e.getMessage(), e);
        }
        
        return typingStatus;
    }

    /**
     * Send message to specific room (used by REST API)
     */
    public void sendMessageToRoom(String roomId, MessageResponseDto message) {
        log.info("Broadcasting message to room: {} via WebSocket", roomId);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/messages", message);
    }

    /**
     * Send typing indicator to specific room
     */
    public void sendTypingToRoom(String roomId, TypingStatusDto typingStatus) {
        log.debug("Broadcasting typing status to room: {} via WebSocket", roomId);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/typing", typingStatus);
    }

    /**
     * Send message read receipt
     */
    public void sendMessageRead(String roomId, String messageId, String userId) {
        log.debug("Broadcasting read receipt for message: {} in room: {}", messageId, roomId);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/read", 
            new MessageReadDto(messageId, userId, System.currentTimeMillis()));
    }

    /**
     * Send user online/offline status
     */
    public void sendUserStatus(String roomId, String userId, boolean online) {
        log.debug("Broadcasting user status: {} is {} in room: {}", userId, online ? "online" : "offline", roomId);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/status", 
            new UserStatusDto(userId, online, System.currentTimeMillis()));
    }

    /**
     * Send direct message to a specific user (used by REST API and WebSocket)
     */
    public void sendDirectMessageToUser(String userId, DirectMessageResponseDto message) {
        log.info("Broadcasting direct message to user: {} via WebSocket", userId);
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/directMessages", message);
    }

    /**
     * Send direct message read status to a specific user
     */
    public void sendDirectMessageReadStatus(String userId, String readByUserId) {
        log.debug("Broadcasting read status to user: {} - read by: {}", userId, readByUserId);
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/directMessageRead", 
            new DirectMessageReadStatusDto(readByUserId, System.currentTimeMillis()));
    }

    /**
     * Send typing indicator for direct messages to a specific user
     */
    public void sendDirectMessageTypingToUser(String userId, String typingUserId, boolean isTyping) {
        log.debug("Broadcasting typing indicator to user: {} - {} is typing: {}", userId, typingUserId, isTyping);
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/directMessageTyping", 
            new DirectMessageTypingStatusWebSocketDto(typingUserId, isTyping, System.currentTimeMillis()));
    }

    // Direct Message WebSocket Handlers
    
    /**
     * Handle incoming direct messages
     */
    @MessageMapping("/directMessage/{receiverId}")
    public void sendDirectMessage(
            @DestinationVariable String receiverId,
            @Payload DirectMessageCreateDto messageDto,
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            // Extract user information from session
            if (headerAccessor.getSessionAttributes() == null) {
                log.warn("Session attributes not found");
                return;
            }
            
            String userId = (String) headerAccessor.getSessionAttributes().get("userId");
            
            if (userId == null) {
                log.warn("User ID not found in session attributes for direct message");
                return;
            }
            
            log.info("Received WebSocket direct message from user: {} to user: {}", userId, receiverId);
            
            // Parse and set receiver ID 
            try {
                Long receiverIdLong = Long.parseLong(receiverId);
                messageDto.setReceiverId(receiverIdLong);
            } catch (Exception e) {
                log.warn("Invalid receiver ID: {}", receiverId);
                return;
            }
            
            DirectMessageResponseDto response = directMessageService.sendMessage(messageDto, userId);
            
            // Broadcast to both sender and receiver
            sendDirectMessageToUser(userId, response);
            sendDirectMessageToUser(receiverId, response);
            
            log.info("Direct message sent successfully via WebSocket: {}", response.getId());
            
        } catch (Exception e) {
            log.error("Error processing WebSocket direct message: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle typing indicators for direct messages
     */
    @MessageMapping("/directMessage/{receiverId}/typing")
    public void handleDirectMessageTyping(
            @DestinationVariable String receiverId,
            @Payload DirectMessageTypingStatusDto typingStatus,
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            if (headerAccessor.getSessionAttributes() == null) {
                return;
            }
            
            String userId = (String) headerAccessor.getSessionAttributes().get("userId");
            
            if (userId != null) {
                directMessageService.sendTypingIndicator(userId, receiverId, typingStatus.isTyping());
                log.debug("Direct message typing indicator sent from {} to {}: {}", 
                    userId, receiverId, typingStatus.isTyping());
            }
        } catch (Exception e) {
            log.error("Error handling direct message typing indicator: {}", e.getMessage(), e);
        }
    }

    /**
     * Mark direct message conversation as read
     */
    @MessageMapping("/directMessage/{senderId}/markRead")
    public void markDirectMessageConversationAsRead(
            @DestinationVariable String senderId,
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            if (headerAccessor.getSessionAttributes() == null) {
                return;
            }
            
            String userId = (String) headerAccessor.getSessionAttributes().get("userId");
            
            if (userId != null) {
                directMessageService.markConversationAsRead(userId, senderId);
                log.info("Direct message conversation marked as read by {} from {}", userId, senderId);
            }
        } catch (Exception e) {
            log.error("Error marking direct message conversation as read: {}", e.getMessage(), e);
        }
    }

    // Inner DTOs for WebSocket events
    public static class MessageReadDto {
        public String messageId;
        public String userId;
        public long timestamp;
        
        public MessageReadDto(String messageId, String userId, long timestamp) {
            this.messageId = messageId;
            this.userId = userId;
            this.timestamp = timestamp;
        }
    }
    
    public static class UserStatusDto {
        public String userId;
        public boolean online;
        public long timestamp;
        
        public UserStatusDto(String userId, boolean online, long timestamp) {
            this.userId = userId;
            this.online = online;
            this.timestamp = timestamp;
        }
    }

    public static class DirectMessageReadStatusDto {
        public String readByUserId;
        public long timestamp;
        
        public DirectMessageReadStatusDto(String readByUserId, long timestamp) {
            this.readByUserId = readByUserId;
            this.timestamp = timestamp;
        }
    }

    public static class DirectMessageTypingStatusWebSocketDto {
        public String userId;
        public boolean isTyping;
        public long timestamp;
        
        public DirectMessageTypingStatusWebSocketDto(String userId, boolean isTyping, long timestamp) {
            this.userId = userId;
            this.isTyping = isTyping;
            this.timestamp = timestamp;
        }
    }
}
