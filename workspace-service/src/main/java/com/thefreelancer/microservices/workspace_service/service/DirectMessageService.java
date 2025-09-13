package com.thefreelancer.microservices.workspace_service.service;

import com.thefreelancer.microservices.workspace_service.dto.*;
import com.thefreelancer.microservices.workspace_service.mapper.DirectMessageMapper;
import com.thefreelancer.microservices.workspace_service.model.DirectMessage;
import com.thefreelancer.microservices.workspace_service.repository.DirectMessageRepository;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectMessageService {
    
    private final DirectMessageRepository directMessageRepository;
    private final DirectMessageMapper directMessageMapper;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Transactional
    public DirectMessageResponseDto sendMessage(DirectMessageCreateDto createDto, String senderId) {
        log.info("Sending direct message from user: {} to user: {}", senderId, createDto.getReceiverId());
        
        // Validate that sender is not trying to message themselves
        if (senderId.equals(createDto.getReceiverId())) {
            throw new IllegalArgumentException("Cannot send message to yourself");
        }
        
        // Create message entity
        DirectMessage message = directMessageMapper.toEntity(createDto, senderId);
        
        // Handle reply-to message
        if (createDto.getReplyToId() != null) {
            DirectMessage replyToMessage = directMessageRepository.findById(createDto.getReplyToId())
                .orElseThrow(() -> new IllegalArgumentException("Reply-to message not found: " + createDto.getReplyToId()));
            
            // Validate that reply-to message is part of the same conversation
            if (!isPartOfSameConversation(replyToMessage, senderId, createDto.getReceiverId())) {
                throw new IllegalArgumentException("Reply-to message is not part of this conversation");
            }
            
            message.setReplyToMessage(replyToMessage);
        }
        
        // Save message
        DirectMessage savedMessage = directMessageRepository.save(message);
        log.info("Direct message sent successfully with ID: {}", savedMessage.getId());
        
        // Convert to response DTO
        DirectMessageResponseDto messageResponse = directMessageMapper.toResponseDto(savedMessage);
        
        // TODO: Populate sender and receiver names from user service
        
        // Broadcast message via WebSocket to both users
        try {
            // Send to receiver
            messagingTemplate.convertAndSend(
                "/topic/user/" + createDto.getReceiverId() + "/messages", 
                messageResponse
            );
            
            // Send to sender for confirmation and multi-device sync
            messagingTemplate.convertAndSend(
                "/topic/user/" + senderId + "/messages", 
                messageResponse
            );
            
            log.info("Direct message broadcasted via WebSocket to users: {} and {}", 
                senderId, createDto.getReceiverId());
        } catch (Exception e) {
            log.error("Failed to broadcast direct message via WebSocket: {}", e.getMessage(), e);
        }
        
        return messageResponse;
    }
    
    @Transactional(readOnly = true)
    public DirectMessagePageResponseDto getConversationMessages(String userId, String otherUserId, 
                                                         int page, int size, String beforeMessageId) {
        log.info("Getting conversation messages between users: {} and {}, page: {}, size: {}", 
            userId, otherUserId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<DirectMessage> messagesPage;
        
        if (beforeMessageId != null) {
            messagesPage = directMessageRepository.findConversationMessagesBeforeMessage(
                userId, otherUserId, beforeMessageId, pageable);
        } else {
            messagesPage = directMessageRepository.findConversationMessages(
                userId, otherUserId, pageable);
        }
        
        List<DirectMessageResponseDto> messageResponses = directMessageMapper.toResponseDtos(
            messagesPage.getContent());
        
        // TODO: Populate sender and receiver names from user service
        
        return DirectMessagePageResponseDto.builder()
            .messages(messageResponses)
            .currentPage(page)
            .pageSize(size)
            .totalPages(messagesPage.getTotalPages())
            .totalElements(messagesPage.getTotalElements())
            .hasNext(messagesPage.hasNext())
            .hasPrevious(messagesPage.hasPrevious())
            .build();
    }
    
    @Transactional(readOnly = true)
    public List<ConversationResponseDto> getUserConversations(String userId) {
        log.info("Getting conversations for user: {}", userId);
        
        // Get latest messages for each conversation
        List<DirectMessage> latestMessages = directMessageRepository.findLatestMessagesForUserConversations(userId);
        
        return latestMessages.stream()
            .map(message -> {
                String otherUserId = message.getOtherParticipant(userId);
                long unreadCount = directMessageRepository.countUnreadMessagesFromSender(userId, otherUserId);
                
                return ConversationResponseDto.builder()
                    .conversationId(message.getConversationId())
                    .otherParticipantId(otherUserId)
                    // TODO: Populate name and handle from user service
                    .otherParticipantName("User " + otherUserId) // Temporary
                    .otherParticipantHandle("@user" + otherUserId) // Temporary
                    .lastMessage(directMessageMapper.toResponseDto(message))
                    .unreadCount(unreadCount)
                    .lastActivity(message.getCreatedAt())
                    .participantIds(List.of(userId, otherUserId))
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void markConversationAsRead(String receiverId, String senderId) {
        log.info("Marking conversation as read for receiver: {} from sender: {}", receiverId, senderId);
        
        int updatedCount = directMessageRepository.markMessagesAsReadBetweenUsers(receiverId, senderId);
        log.info("Marked {} messages as read", updatedCount);
        
        // Broadcast read receipt via WebSocket
        try {
            DirectMessageReadReceiptDto readReceipt = DirectMessageReadReceiptDto.builder()
                .conversationId(generateConversationId(receiverId, senderId))
                .readerId(receiverId)
                .readAt(LocalDateTime.now())
                .build();
            
            // Notify the sender that their messages were read
            messagingTemplate.convertAndSend(
                "/topic/user/" + senderId + "/read", 
                readReceipt
            );
            
            log.info("Read receipt broadcasted via WebSocket to user: {}", senderId);
        } catch (Exception e) {
            log.error("Failed to broadcast read receipt via WebSocket: {}", e.getMessage(), e);
        }
    }
    
    @Transactional
    public void markMessageAsRead(String messageId, String receiverId) {
        log.info("Marking message as read: {} by user: {}", messageId, receiverId);
        
        int updatedCount = directMessageRepository.markMessageAsRead(messageId, receiverId);
        if (updatedCount == 0) {
            throw new IllegalArgumentException("Message not found or not owned by user");
        }
        
        // Find the message to get sender info for WebSocket notification
        directMessageRepository.findById(messageId)
            .ifPresent(message -> {
                try {
                    DirectMessageReadReceiptDto readReceipt = DirectMessageReadReceiptDto.builder()
                        .messageId(messageId)
                        .conversationId(message.getConversationId())
                        .readerId(receiverId)
                        .readAt(LocalDateTime.now())
                        .build();
                    
                    // Notify the sender that their message was read
                    messagingTemplate.convertAndSend(
                        "/topic/user/" + message.getSenderId() + "/read", 
                        readReceipt
                    );
                    
                    log.info("Read receipt broadcasted via WebSocket to user: {}", message.getSenderId());
                } catch (Exception e) {
                    log.error("Failed to broadcast read receipt via WebSocket: {}", e.getMessage(), e);
                }
            });
    }
    
    @Transactional
    public DirectMessageResponseDto updateMessage(String messageId, String userId, DirectMessageUpdateDto updateDto) {
        log.info("Updating message: {} by user: {}", messageId, userId);
        
        DirectMessage message = directMessageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        
        // Validate that user is the sender
        if (!message.getSenderId().equals(userId)) {
            throw new IllegalArgumentException("Cannot update message sent by another user");
        }
        
        // Update message content
        message = directMessageMapper.updateEntity(message, updateDto.getContent());
        DirectMessage updatedMessage = directMessageRepository.save(message);
        
        DirectMessageResponseDto messageResponse = directMessageMapper.toResponseDto(updatedMessage);
        
        // Broadcast updated message via WebSocket
        try {
            messagingTemplate.convertAndSend(
                "/topic/user/" + message.getReceiverId() + "/messages", 
                messageResponse
            );
            messagingTemplate.convertAndSend(
                "/topic/user/" + message.getSenderId() + "/messages", 
                messageResponse
            );
            
            log.info("Updated message broadcasted via WebSocket");
        } catch (Exception e) {
            log.error("Failed to broadcast updated message via WebSocket: {}", e.getMessage(), e);
        }
        
        return messageResponse;
    }
    
    @Transactional
    public void deleteMessage(String messageId, String userId) {
        log.info("Deleting message: {} by user: {}", messageId, userId);
        
        DirectMessage message = directMessageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        
        // Validate that user is the sender
        if (!message.getSenderId().equals(userId)) {
            throw new IllegalArgumentException("Cannot delete message sent by another user");
        }
        
        directMessageRepository.delete(message);
        log.info("Message deleted successfully: {}", messageId);
        
        // Broadcast deletion via WebSocket
        try {
            Map<String, Object> deletionNotification = Map.of(
                "type", "MESSAGE_DELETED",
                "messageId", messageId,
                "conversationId", message.getConversationId(),
                "deletedBy", userId,
                "deletedAt", LocalDateTime.now()
            );
            
            messagingTemplate.convertAndSend(
                "/topic/user/" + message.getReceiverId() + "/messages", 
                deletionNotification
            );
            messagingTemplate.convertAndSend(
                "/topic/user/" + message.getSenderId() + "/messages", 
                deletionNotification
            );
            
            log.info("Message deletion broadcasted via WebSocket");
        } catch (Exception e) {
            log.error("Failed to broadcast message deletion via WebSocket: {}", e.getMessage(), e);
        }
    }
    
    @Transactional(readOnly = true)
    public DirectMessagePageResponseDto searchConversationMessages(String userId, String otherUserId, 
                                                            String searchTerm, int page, int size) {
        log.info("Searching conversation messages between users: {} and {} with term: {}", 
            userId, otherUserId, searchTerm);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<DirectMessage> messagesPage = directMessageRepository.searchConversationMessages(
            userId, otherUserId, searchTerm, pageable);
        
        List<DirectMessageResponseDto> messageResponses = directMessageMapper.toResponseDtos(
            messagesPage.getContent());
        
        return DirectMessagePageResponseDto.builder()
            .messages(messageResponses)
            .currentPage(page)
            .pageSize(size)
            .totalPages(messagesPage.getTotalPages())
            .totalElements(messagesPage.getTotalElements())
            .hasNext(messagesPage.hasNext())
            .hasPrevious(messagesPage.hasPrevious())
            .build();
    }
    
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(String userId) {
        return directMessageRepository.countTotalUnreadMessages(userId);
    }
    
    public void sendTypingIndicator(String senderId, String receiverId, boolean isTyping) {
        log.debug("Sending typing indicator from user: {} to user: {}, typing: {}", 
            senderId, receiverId, isTyping);
        
        try {
            DirectMessageTypingStatusDto typingStatus = DirectMessageTypingStatusDto.builder()
                .conversationId(generateConversationId(senderId, receiverId))
                .userId(senderId)
                .isTyping(isTyping)
                .timestamp(LocalDateTime.now())
                .build();
            
            messagingTemplate.convertAndSend(
                "/topic/user/" + receiverId + "/typing", 
                typingStatus
            );
            
            log.debug("Typing indicator broadcasted via WebSocket");
        } catch (Exception e) {
            log.error("Failed to broadcast typing indicator via WebSocket: {}", e.getMessage(), e);
        }
    }
    
    private boolean isPartOfSameConversation(DirectMessage message, String userId1, String userId2) {
        return (message.getSenderId().equals(userId1) && message.getReceiverId().equals(userId2)) ||
               (message.getSenderId().equals(userId2) && message.getReceiverId().equals(userId1));
    }
    
    private String generateConversationId(String userId1, String userId2) {
        // Always use lexicographically smaller ID first for consistent conversation ID
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }
}
