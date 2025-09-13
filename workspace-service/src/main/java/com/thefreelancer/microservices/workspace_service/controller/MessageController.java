package com.thefreelancer.microservices.workspace_service.controller;

import com.thefreelancer.microservices.workspace_service.dto.*;
import com.thefreelancer.microservices.workspace_service.service.MessageService;
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
@RequestMapping("/api/workspaces/rooms")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Workspace Messages", description = "Real-time messaging operations")
public class MessageController {
    
    private final MessageService messageService;
    
    @Operation(summary = "Send message", description = "Send a message to workspace room")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Message sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid message data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not a room participant"),
        @ApiResponse(responseCode = "404", description = "Room not found")
    })
    @PostMapping("/{roomId}/messages")
    public ResponseEntity<MessageResponseDto> sendMessage(
            @Parameter(description = "ID of the workspace room") @PathVariable String roomId,
            @Valid @RequestBody MessageCreateDto createDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("POST /api/workspaces/rooms/{}/messages - Sending message", roomId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for sending messages");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            String authenticatedUserId = userIdHeader;
            log.info("Sending message to room: {} by user: {}", roomId, authenticatedUserId);
            
            MessageResponseDto message = messageService.sendMessage(roomId, authenticatedUserId, createDto);
            log.info("Message sent successfully with ID: {}", message.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for sending message to room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error sending message to room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(summary = "Get message history", description = "Get paginated message history for workspace room")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not a room participant"),
        @ApiResponse(responseCode = "404", description = "Room not found")
    })
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<MessagePageResponseDto> getMessageHistory(
            @Parameter(description = "ID of the workspace room") @PathVariable String roomId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int limit,
            @Parameter(description = "Get messages before this message ID (cursor pagination)") 
            @RequestParam(required = false) String before,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/workspaces/rooms/{}/messages - Getting message history", roomId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for viewing messages");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            String authenticatedUserId = userIdHeader;
            log.info("Getting message history for room: {} by user: {}", roomId, authenticatedUserId);
            
            MessagePageResponseDto messages = messageService.getMessageHistory(
                roomId, authenticatedUserId, page, limit, before);
            log.info("Retrieved {} messages for room: {}", messages.getMessages().size(), roomId);
            
            return ResponseEntity.ok(messages);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for getting messages from room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error getting messages from room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(summary = "Edit message", description = "Edit a message in workspace room")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Message edited successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid message data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not message sender"),
        @ApiResponse(responseCode = "404", description = "Message or room not found")
    })
    @PutMapping("/{roomId}/messages/{messageId}")
    public ResponseEntity<MessageResponseDto> editMessage(
            @Parameter(description = "ID of the workspace room") @PathVariable String roomId,
            @Parameter(description = "ID of the message to edit") @PathVariable String messageId,
            @Valid @RequestBody MessageUpdateDto updateDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("PUT /api/workspaces/rooms/{}/messages/{} - Editing message", roomId, messageId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for editing messages");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            String authenticatedUserId = userIdHeader;
            log.info("Editing message: {} in room: {} by user: {}", messageId, roomId, authenticatedUserId);
            
            MessageResponseDto message = messageService.editMessage(roomId, messageId, authenticatedUserId, updateDto);
            log.info("Message edited successfully: {}", messageId);
            
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for editing message {} in room {}: {}", messageId, roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error editing message {} in room {}: {}", messageId, roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(summary = "Delete message", description = "Delete a message from workspace room")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Message deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not message sender"),
        @ApiResponse(responseCode = "404", description = "Message or room not found")
    })
    @DeleteMapping("/{roomId}/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @Parameter(description = "ID of the workspace room") @PathVariable String roomId,
            @Parameter(description = "ID of the message to delete") @PathVariable String messageId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("DELETE /api/workspaces/rooms/{}/messages/{} - Deleting message", roomId, messageId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for deleting messages");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            String authenticatedUserId = userIdHeader;
            log.info("Deleting message: {} in room: {} by user: {}", messageId, roomId, authenticatedUserId);
            
            messageService.deleteMessage(roomId, messageId, authenticatedUserId);
            log.info("Message deleted successfully: {}", messageId);
            
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for deleting message {} in room {}: {}", messageId, roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error deleting message {} in room {}: {}", messageId, roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(summary = "Search messages", description = "Search messages in workspace room")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not a room participant"),
        @ApiResponse(responseCode = "404", description = "Room not found")
    })
    @GetMapping("/{roomId}/messages/search")
    public ResponseEntity<MessagePageResponseDto> searchMessages(
            @Parameter(description = "ID of the workspace room") @PathVariable String roomId,
            @Parameter(description = "Search term") @RequestParam String q,
            @Parameter(description = "Message type filter") @RequestParam(required = false) String type,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int limit,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/workspaces/rooms/{}/messages/search - Searching messages", roomId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for searching messages");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            String authenticatedUserId = userIdHeader;
            log.info("Searching messages in room: {} for term: '{}' by user: {}", roomId, q, authenticatedUserId);
            
            MessagePageResponseDto messages = messageService.searchMessages(
                roomId, authenticatedUserId, q, type, page, limit);
            log.info("Found {} messages matching search criteria", messages.getMessages().size());
            
            return ResponseEntity.ok(messages);
        } catch (IllegalArgumentException e) {
            log.error("Invalid search request for room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error searching messages in room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // ===== REAL-TIME CHAT APIS =====
    
    @Operation(summary = "Send typing indicator", description = "Send typing indicator to room participants")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Typing indicator sent successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not a room participant"),
        @ApiResponse(responseCode = "404", description = "Room not found")
    })
    @PostMapping("/{roomId}/typing")
    public ResponseEntity<Void> sendTypingIndicator(
            @Parameter(description = "ID of the workspace room") @PathVariable String roomId,
            @Valid @RequestBody TypingStatusDto typingStatus,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.debug("POST /api/workspaces/rooms/{}/typing - Sending typing indicator", roomId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for typing indicators");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            String authenticatedUserId = userIdHeader;
            
            // Parse and set user ID as Long
            try {
                Long userIdLong = Long.parseLong(authenticatedUserId);
                typingStatus.setUserId(userIdLong);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid user ID: " + authenticatedUserId);
            }
            
            typingStatus.setRoomId(roomId);
            
            // Broadcast typing indicator via WebSocket
            messageService.sendTypingIndicator(roomId, typingStatus);
            
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid typing indicator request for room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error sending typing indicator to room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(summary = "Mark messages as read", description = "Mark messages as read and send read receipts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Messages marked as read successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not a room participant"),
        @ApiResponse(responseCode = "404", description = "Room or messages not found")
    })
    @PostMapping("/{roomId}/read")
    public ResponseEntity<Void> markMessagesAsRead(
            @Parameter(description = "ID of the workspace room") @PathVariable String roomId,
            @Parameter(description = "Message IDs to mark as read") @RequestBody List<String> messageIds,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("POST /api/workspaces/rooms/{}/read - Marking {} messages as read", roomId, messageIds.size());
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for marking messages as read");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            String authenticatedUserId = userIdHeader;
            messageService.markMessagesAsRead(roomId, messageIds, authenticatedUserId);
            
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid read receipt request for room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error marking messages as read in room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(summary = "Get unread message count", description = "Get count of unread messages in room")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Unread count retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not a room participant"),
        @ApiResponse(responseCode = "404", description = "Room not found")
    })
    @GetMapping("/{roomId}/unread-count")
    public ResponseEntity<UnreadCountDto> getUnreadMessageCount(
            @Parameter(description = "ID of the workspace room") @PathVariable String roomId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.debug("GET /api/workspaces/rooms/{}/unread-count - Getting unread message count", roomId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for unread count");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            String authenticatedUserId = userIdHeader;
            long unreadCount = messageService.getUnreadMessageCount(roomId, authenticatedUserId);
            
            UnreadCountDto response = new UnreadCountDto(roomId, unreadCount);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid unread count request for room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error getting unread count for room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Inner DTO for unread count response
    public static class UnreadCountDto {
        public String roomId;
        public long count;
        
        public UnreadCountDto(String roomId, long count) {
            this.roomId = roomId;
            this.count = count;
        }
    }
}
