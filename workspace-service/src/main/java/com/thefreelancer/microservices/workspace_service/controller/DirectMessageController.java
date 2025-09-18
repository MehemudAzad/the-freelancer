package com.thefreelancer.microservices.workspace_service.controller;

import com.thefreelancer.microservices.workspace_service.dto.*;
import com.thefreelancer.microservices.workspace_service.service.DirectMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/direct-messages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Direct Messages", description = "Direct messaging operations between users")
public class DirectMessageController {
    
    private final DirectMessageService directMessageService;
    private final ChatWebSocketController chatWebSocketController;

    @Operation(summary = "Get recent chat partners", description = "Get top 10 users you've chatted with recently")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recent chat partners retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/recent")
    public ResponseEntity<List<RecentChatPartnerDto>> getRecentChatPartners(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        log.info("GET /api/direct-messages/recent - Getting recent chat partners for user {}", userIdHeader);
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for getting recent chat partners");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            List<RecentChatPartnerDto> response = directMessageService.getRecentChatPartners(userIdHeader);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting recent chat partners: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Send direct message", description = "Send a direct message to another user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Message sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid message data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "404", description = "Receiver not found")
    })
    @PostMapping
    public ResponseEntity<DirectMessageResponseDto> sendMessage(
            @Valid @RequestBody DirectMessageCreateDto createDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("POST /api/direct-messages - Sending direct message from {} to {}", 
            userIdHeader, createDto.getReceiverId());
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for sending direct messages");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            DirectMessageResponseDto response = directMessageService.sendMessage(createDto, userIdHeader);
            
            // Broadcast the message to both sender and receiver via WebSocket
            chatWebSocketController.sendDirectMessageToUser(userIdHeader, response);
            chatWebSocketController.sendDirectMessageToUser(createDto.getReceiverId().toString(), response);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for sending direct message: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error sending direct message: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Get conversation messages", description = "Get paginated messages from a conversation between two users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not part of conversation")
    })
    @GetMapping("/conversations/{otherUserId}")
    public ResponseEntity<DirectMessagePageResponseDto> getConversationMessages(
            @Parameter(description = "ID of the other user in the conversation") @PathVariable String otherUserId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Message ID to paginate before") @RequestParam(required = false) String before,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/direct-messages/conversations/{} - Getting conversation messages", otherUserId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for getting conversation messages");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            DirectMessagePageResponseDto response = directMessageService.getConversationMessages(
                userIdHeader, otherUserId, page, size, before);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting conversation messages: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Get user conversations", description = "Get all conversations for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conversations retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponseDto>> getUserConversations(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/direct-messages/conversations - Getting conversations for user {}", userIdHeader);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for getting conversations");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            List<ConversationResponseDto> response = directMessageService.getUserConversations(userIdHeader);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting user conversations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Mark conversation as read", description = "Mark all messages in a conversation as read")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Conversation marked as read"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not part of conversation")
    })
    @PutMapping("/conversations/{otherUserId}/read")
    public ResponseEntity<Void> markConversationAsRead(
            @Parameter(description = "ID of the other user in the conversation") @PathVariable String otherUserId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("PUT /api/direct-messages/conversations/{}/read - Marking conversation as read", otherUserId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for marking conversation as read");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            directMessageService.markConversationAsRead(userIdHeader, otherUserId);
            
            // Broadcast read status to the other user via WebSocket
            chatWebSocketController.sendDirectMessageReadStatus(otherUserId, userIdHeader);
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error marking conversation as read: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Mark message as read", description = "Mark a specific message as read")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Message marked as read"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not the receiver"),
        @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @PutMapping("/{messageId}/read")
    public ResponseEntity<Void> markMessageAsRead(
            @Parameter(description = "ID of the message") @PathVariable String messageId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("PUT /api/direct-messages/{}/read - Marking message as read", messageId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for marking message as read");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            directMessageService.markMessageAsRead(messageId, userIdHeader);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for marking message as read: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error marking message as read: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Update message", description = "Update content of a sent message")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Message updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid update data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not the sender"),
        @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @PutMapping("/{messageId}")
    public ResponseEntity<DirectMessageResponseDto> updateMessage(
            @Parameter(description = "ID of the message") @PathVariable String messageId,
            @Valid @RequestBody DirectMessageUpdateDto updateDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("PUT /api/direct-messages/{} - Updating message", messageId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for updating message");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            DirectMessageResponseDto response = directMessageService.updateMessage(messageId, userIdHeader, updateDto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for updating message: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating message: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Delete message", description = "Delete a sent message")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Message deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not the sender"),
        @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @Parameter(description = "ID of the message") @PathVariable String messageId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("DELETE /api/direct-messages/{} - Deleting message", messageId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for deleting message");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            directMessageService.deleteMessage(messageId, userIdHeader);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for deleting message: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error deleting message: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Search conversation messages", description = "Search messages in a specific conversation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not part of conversation")
    })
    @GetMapping("/conversations/{otherUserId}/search")
    public ResponseEntity<DirectMessagePageResponseDto> searchConversationMessages(
            @Parameter(description = "ID of the other user in the conversation") @PathVariable String otherUserId,
            @Parameter(description = "Search term") @RequestParam String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/direct-messages/conversations/{}/search - Searching messages with term: {}", 
            otherUserId, q);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for searching messages");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            DirectMessagePageResponseDto response = directMessageService.searchConversationMessages(
                userIdHeader, otherUserId, q, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching conversation messages: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Get unread message count", description = "Get total number of unread direct messages for user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Unread count retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadMessageCount(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/direct-messages/unread-count - Getting unread count for user {}", userIdHeader);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for getting unread count");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            long unreadCount = directMessageService.getUnreadMessageCount(userIdHeader);
            return ResponseEntity.ok(unreadCount);
        } catch (Exception e) {
            log.error("Error getting unread message count: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Send typing indicator", description = "Send typing indicator to another user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Typing indicator sent"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PostMapping("/conversations/{otherUserId}/typing")
    public ResponseEntity<Void> sendTypingIndicator(
            @Parameter(description = "ID of the other user in the conversation") @PathVariable String otherUserId,
            @Parameter(description = "Whether user is typing") @RequestParam boolean isTyping,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.debug("POST /api/direct-messages/conversations/{}/typing - Sending typing indicator", otherUserId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for sending typing indicator");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            directMessageService.sendTypingIndicator(userIdHeader, otherUserId, isTyping);
            
            // Broadcast typing indicator via WebSocket
            chatWebSocketController.sendDirectMessageTypingToUser(otherUserId, userIdHeader, isTyping);
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error sending typing indicator: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
