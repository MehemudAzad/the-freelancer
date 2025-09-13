package com.thefreelancer.microservices.workspace_service.mapper;

import com.thefreelancer.microservices.workspace_service.dto.DirectMessageCreateDto;
import com.thefreelancer.microservices.workspace_service.dto.DirectMessageResponseDto;
import com.thefreelancer.microservices.workspace_service.model.DirectMessage;
import com.thefreelancer.microservices.workspace_service.model.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DirectMessageMapper {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public DirectMessageResponseDto toResponseDto(DirectMessage message) {
        if (message == null) {
            return null;
        }
        
        List<DirectMessageResponseDto.MessageAttachmentDto> attachmentDtos = null;
        if (message.getAttachments() != null && !message.getAttachments().isNull()) {
            try {
                List<DirectMessageCreateDto.MessageAttachmentDto> attachments = 
                    objectMapper.convertValue(message.getAttachments(), 
                        new TypeReference<List<DirectMessageCreateDto.MessageAttachmentDto>>() {});

                attachmentDtos = attachments.stream()
                    .map(this::toResponseAttachmentDto)
                    .collect(Collectors.toList());
            } catch (Exception e) {
                log.warn("Failed to parse message attachments for message {}: {}", 
                    message.getId(), e.getMessage());
            }
        }
        
        return DirectMessageResponseDto.builder()
            .id(message.getId())
            .senderId(message.getSenderId())
            .receiverId(message.getReceiverId())
            .content(message.getContent())
            .messageType(message.getMessageType().name())
            .replyToId(message.getReplyToId())
            .replyToMessage(toResponseDto(message.getReplyToMessage()))
            .attachments(attachmentDtos)
            .isRead(message.getIsRead())
            .readAt(message.getReadAt())
            .editedAt(message.getEditedAt())
            .createdAt(message.getCreatedAt())
            .isSystemMessage(message.getMessageType() == Message.MessageType.SYSTEM)
            .build();
    }
    
    public DirectMessage toEntity(DirectMessageCreateDto createDto, Long senderId) {
        if (createDto == null) {
            return null;
        }
        
        // convert attachments list to JsonNode for jsonb column
        com.fasterxml.jackson.databind.JsonNode attachmentsNode = null;
        if (createDto.getAttachments() != null && !createDto.getAttachments().isEmpty()) {
            try {
                attachmentsNode = objectMapper.valueToTree(createDto.getAttachments());
            } catch (Exception e) {
                log.warn("Failed to convert message attachments to JsonNode: {}", e.getMessage());
            }
        }
        
        DirectMessage.DirectMessageBuilder builder = DirectMessage.builder()
            .senderId(senderId)
            .receiverId(createDto.getReceiverId())
            .content(createDto.getContent())
            .messageType(Message.MessageType.valueOf(createDto.getMessageType()))
            .replyToId(createDto.getReplyToId())
            .attachments(attachmentsNode);
        
        return builder.build();
    }

    private Long parseReplyToId(String replyToId) {
        if (replyToId == null) return null;
        try {
            return Long.valueOf(replyToId);
        } catch (NumberFormatException e) {
            log.warn("Invalid replyToId '{}', ignoring: {}", replyToId, e.getMessage());
            return null;
        }
    }
    
    public DirectMessage updateEntity(DirectMessage existingMessage, String newContent) {
        if (existingMessage == null) {
            return null;
        }
        
        existingMessage.setContent(newContent);
        existingMessage.markAsEdited();
        
        return existingMessage;
    }
    
    private DirectMessageResponseDto.MessageAttachmentDto toResponseAttachmentDto(
            DirectMessageCreateDto.MessageAttachmentDto createDto) {
        return DirectMessageResponseDto.MessageAttachmentDto.builder()
            .filename(createDto.getFilename())
            .url(createDto.getUrl())
            .contentType(createDto.getContentType())
            .fileSize(createDto.getFileSize())
            .build();
    }
    
    public List<DirectMessageResponseDto> toResponseDtos(List<DirectMessage> messages) {
        if (messages == null) {
            return null;
        }
        
        return messages.stream()
            .map(this::toResponseDto)
            .collect(Collectors.toList());
    }
}
