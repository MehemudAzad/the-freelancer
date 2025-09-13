package com.thefreelancer.microservices.workspace_service.service;

import com.thefreelancer.microservices.workspace_service.dto.*;
import com.thefreelancer.microservices.workspace_service.mapper.MessageMapper;
import com.thefreelancer.microservices.workspace_service.model.Message;
import com.thefreelancer.microservices.workspace_service.model.Room;
import com.thefreelancer.microservices.workspace_service.repository.MessageRepository;
import com.thefreelancer.microservices.workspace_service.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Transactional
    public MessageResponseDto sendMessage(String roomId, String senderId, MessageCreateDto createDto) {
        log.info("Sending message to room: {} from user: {}", roomId, senderId);
        
        // Validate room exists and user has access
        Long roomIdLong = Long.parseLong(roomId);
        Room room = roomRepository.findById(roomIdLong)
            .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
        
        validateRoomAccess(room, senderId);
        
        // Create message entity
        Message message = messageMapper.toEntity(createDto, roomId, senderId);
        message.setRoom(room);
        
        // Handle reply-to message
        if (createDto.getReplyToId() != null) {
            Message replyToMessage = messageRepository.findById(createDto.getReplyToId())
                .orElseThrow(() -> new IllegalArgumentException("Reply-to message not found: " + createDto.getReplyToId()));
            
            if (!replyToMessage.getRoomId().equals(roomId)) {
                throw new IllegalArgumentException("Cannot reply to message from different room");
            }
            
            message.setReplyToMessage(replyToMessage);
            message.setReplyToId(createDto.getReplyToId());
        }
        
        Message savedMessage = messageRepository.save(message);
        
        // Convert to response DTO
        MessageResponseDto messageResponse = messageMapper.toResponseDto(savedMessage);
        
        // Publish WebSocket event for real-time updates
        try {
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/messages", messageResponse);
            log.debug("Broadcasted message {} to WebSocket topic for room {}", savedMessage.getId(), roomId);
        } catch (Exception e) {
            log.error("Failed to broadcast message via WebSocket: {}", e.getMessage(), e);
        }
        
        log.info("Message sent successfully with ID: {}", savedMessage.getId());
        return messageResponse;
    }
    
    @Transactional(readOnly = true)
    public MessagePageResponseDto getMessageHistory(String roomId, String userId, 
                                                  int page, int size, String beforeMessageId) {
        log.info("Getting message history for room: {} by user: {}", roomId, userId);
        
        // Validate room access
        Long roomIdLong = Long.parseLong(roomId);
        Room room = roomRepository.findById(roomIdLong)
            .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
        validateRoomAccess(room, userId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage;
        
        if (beforeMessageId != null) {
            // Cursor-based pagination
            messagePage = messageRepository.findByRoomIdBeforeMessageOrderByCreatedAtDesc(
                roomId, beforeMessageId, pageable);
        } else {
            // Regular pagination
            messagePage = messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable);
        }
        
        List<MessageResponseDto> messages = messageMapper.toResponseDtoList(messagePage.getContent());
        
        return MessagePageResponseDto.builder()
            .messages(messages)
            .currentPage(messagePage.getNumber())
            .pageSize(messagePage.getSize())
            .totalElements(messagePage.getTotalElements())
            .totalPages(messagePage.getTotalPages())
            .hasNext(messagePage.hasNext())
            .hasPrevious(messagePage.hasPrevious())
            .nextCursor(messages.isEmpty() ? null : messages.get(messages.size() - 1).getId())
            .previousCursor(messages.isEmpty() ? null : messages.get(0).getId())
            .build();
    }
    
    @Transactional
    public MessageResponseDto editMessage(String roomId, String messageId, String userId, 
                                        MessageUpdateDto updateDto) {
        log.info("Editing message: {} in room: {} by user: {}", messageId, roomId, userId);
        
        // Validate room access
        Long roomIdLong = Long.parseLong(roomId);
        Room room = roomRepository.findById(roomIdLong)
            .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
        validateRoomAccess(room, userId);
        
        // Find and validate message
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        
        if (!message.getRoomId().equals(roomId)) {
            throw new IllegalArgumentException("Message does not belong to this room");
        }
        
        if (!message.getSenderId().equals(userId)) {
            throw new IllegalArgumentException("Cannot edit message sent by another user");
        }
        
        if (message.getMessageType() == Message.MessageType.SYSTEM) {
            throw new IllegalArgumentException("Cannot edit system messages");
        }
        
        // Update message
        message.setContent(updateDto.getContent());
        message.setEditedAt(LocalDateTime.now());
        
        Message savedMessage = messageRepository.save(message);
        
        // TODO: Publish WebSocket event for real-time updates
        
        log.info("Message edited successfully: {}", messageId);
        return messageMapper.toResponseDto(savedMessage);
    }
    
    @Transactional
    public void deleteMessage(String roomId, String messageId, String userId) {
        log.info("Deleting message: {} in room: {} by user: {}", messageId, roomId, userId);
        
        // Validate room access
        Long roomIdLong = Long.parseLong(roomId);
        Room room = roomRepository.findById(roomIdLong)
            .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
        validateRoomAccess(room, userId);
        
        // Find and validate message
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        
        if (!message.getRoomId().equals(roomId)) {
            throw new IllegalArgumentException("Message does not belong to this room");
        }
        
        if (!message.getSenderId().equals(userId)) {
            throw new IllegalArgumentException("Cannot delete message sent by another user");
        }
        
        if (message.getMessageType() == Message.MessageType.SYSTEM) {
            throw new IllegalArgumentException("Cannot delete system messages");
        }
        
        messageRepository.delete(message);
        
        // TODO: Publish WebSocket event for real-time updates
        
        log.info("Message deleted successfully: {}", messageId);
    }
    
    @Transactional(readOnly = true)
    public MessagePageResponseDto searchMessages(String roomId, String userId, String searchTerm, 
                                                String messageType, int page, int size) {
        log.info("Searching messages in room: {} for term: '{}' by user: {}", roomId, searchTerm, userId);
        
        // Validate room access
        Long roomIdLong = Long.parseLong(roomId);
        Room room = roomRepository.findById(roomIdLong)
            .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
        validateRoomAccess(room, userId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage;
        
        if (messageType != null && !messageType.isEmpty()) {
            Message.MessageType type = Message.MessageType.valueOf(messageType.toUpperCase());
            messagePage = messageRepository.searchByRoomIdAndTypeAndContent(roomId, type, searchTerm, pageable);
        } else {
            messagePage = messageRepository.searchByRoomIdAndContent(roomId, searchTerm, pageable);
        }
        
        List<MessageResponseDto> messages = messageMapper.toResponseDtoList(messagePage.getContent());
        
        log.info("Found {} messages matching search criteria", messages.size());
        
        return MessagePageResponseDto.builder()
            .messages(messages)
            .currentPage(messagePage.getNumber())
            .pageSize(messagePage.getSize())
            .totalElements(messagePage.getTotalElements())
            .totalPages(messagePage.getTotalPages())
            .hasNext(messagePage.hasNext())
            .hasPrevious(messagePage.hasPrevious())
            .build();
    }
    
    @Transactional
    public MessageResponseDto createSystemMessage(String roomId, String content) {
        log.info("Creating system message in room: {}", roomId);
        
        Long roomIdLong = Long.parseLong(roomId);
        Room room = roomRepository.findById(roomIdLong)
            .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
        
        Message systemMessage = Message.builder()
            .room(room)
            .roomId(roomId)
            .senderId("SYSTEM")
            .content(content)
            .messageType(Message.MessageType.SYSTEM)
            .build();
        
        Message savedMessage = messageRepository.save(systemMessage);
        
        // TODO: Publish WebSocket event for real-time updates
        
        log.info("System message created with ID: {}", savedMessage.getId());
        return messageMapper.toResponseDto(savedMessage);
    }
    
    // ===== REAL-TIME CHAT METHODS =====
    
    public void sendTypingIndicator(String roomId, TypingStatusDto typingStatus) {
        log.debug("Broadcasting typing indicator for user {} in room {}", typingStatus.getUserId(), roomId);
        
        // Validate room access
        Long roomIdLong = Long.parseLong(roomId);
        Room room = roomRepository.findById(roomIdLong)
            .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
        validateRoomAccess(room, typingStatus.getUserId());
        
        // Set timestamp
        typingStatus.setTimestamp(System.currentTimeMillis());
        
        // Broadcast via WebSocket
        try {
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/typing", typingStatus);
            log.debug("Broadcasted typing indicator for user {} in room {}", typingStatus.getUserId(), roomId);
        } catch (Exception e) {
            log.error("Failed to broadcast typing indicator: {}", e.getMessage(), e);
        }
    }
    
    @Transactional
    public void markMessagesAsRead(String roomId, List<String> messageIds, String userId) {
        log.info("Marking {} messages as read for user {} in room {}", messageIds.size(), userId, roomId);
        
        // Validate room access
        Long roomIdLong = Long.parseLong(roomId);
        Room room = roomRepository.findById(roomIdLong)
            .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
        validateRoomAccess(room, userId);
        
        // For each message, broadcast read receipt
        for (String messageId : messageIds) {
            try {
                // Verify message exists and belongs to this room
                Message message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
                
                if (!message.getRoomId().equals(roomId)) {
                    throw new IllegalArgumentException("Message does not belong to this room");
                }
                
                // Don't mark own messages as read
                if (message.getSenderId().equals(userId)) {
                    continue;
                }
                
                // Broadcast read receipt via WebSocket
                MessageReadReceiptDto readReceipt = MessageReadReceiptDto.builder()
                    .messageId(messageId)
                    .userId(userId)
                    .readAt(LocalDateTime.now())
                    .build();
                
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/read", readReceipt);
                log.debug("Broadcasted read receipt for message {} by user {}", messageId, userId);
                
            } catch (Exception e) {
                log.error("Failed to process read receipt for message {}: {}", messageId, e.getMessage(), e);
            }
        }
    }
    
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(String roomId, String userId) {
        log.debug("Getting unread message count for user {} in room {}", userId, roomId);
        
        // Validate room access
        Long roomIdLong = Long.parseLong(roomId);
        Room room = roomRepository.findById(roomIdLong)
            .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
        validateRoomAccess(room, userId);
        
        // For now, return 0 as we don't have read tracking in database yet
        // In a full implementation, you would track read receipts in a separate table
        // and count messages sent after the user's last read timestamp
        
        // Example implementation:
        // return messageRepository.countUnreadMessages(roomId, userId, lastReadTimestamp);
        
        return 0L; // Placeholder
    }
    
    private void validateRoomAccess(Room room, String userId) {
        if (!room.getClientId().equals(userId) && !room.getFreelancerId().equals(userId)) {
            throw new IllegalArgumentException("User does not have access to this room");
        }
        
        if (room.getStatus() == Room.RoomStatus.LOCKED) {
            throw new IllegalArgumentException("Room is locked");
        }
    }
}
