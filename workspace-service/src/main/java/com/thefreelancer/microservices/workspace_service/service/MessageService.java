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
        
        // TODO: Publish WebSocket event for real-time updates
        // TODO: Send push notification if other user is offline
        
        log.info("Message sent successfully with ID: {}", savedMessage.getId());
        return messageMapper.toResponseDto(savedMessage);
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
    
    private void validateRoomAccess(Room room, String userId) {
        if (!room.getClientId().equals(userId) && !room.getFreelancerId().equals(userId)) {
            throw new IllegalArgumentException("User does not have access to this room");
        }
        
        if (room.getStatus() == Room.RoomStatus.LOCKED) {
            throw new IllegalArgumentException("Room is locked");
        }
    }
}
