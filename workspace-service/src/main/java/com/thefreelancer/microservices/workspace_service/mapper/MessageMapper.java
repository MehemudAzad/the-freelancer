package com.thefreelancer.microservices.workspace_service.mapper;

import com.thefreelancer.microservices.workspace_service.dto.MessageCreateDto;
import com.thefreelancer.microservices.workspace_service.dto.MessageResponseDto;
import com.thefreelancer.microservices.workspace_service.model.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MessageMapper {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public MessageResponseDto toResponseDto(Message message) {
        if (message == null) {
            return null;
        }
        
    List<MessageResponseDto.MessageAttachmentDto> attachmentDtos = null;
        if (message.getAttachments() != null && !message.getAttachments().isNull()) {
            try {
                List<MessageCreateDto.MessageAttachmentDto> attachments = 
                    objectMapper.convertValue(message.getAttachments(), 
                        new TypeReference<List<MessageCreateDto.MessageAttachmentDto>>() {});

                attachmentDtos = attachments.stream()
                    .map(this::toAttachmentResponseDto)
                    .collect(Collectors.toList());
            } catch (Exception e) {
                // Log error and continue without attachments
                System.err.println("Error parsing message attachments: " + e.getMessage());
            }
        }
        
        return MessageResponseDto.builder()
            .id(message.getId())
            .roomId(message.getRoomId())
            .senderId(message.getSenderId())
            .senderName(message.getSenderName())
            .content(message.getContent())
            .messageType(message.getMessageType().name())
            .replyToId(message.getReplyToId())
            .replyToMessage(message.getReplyToMessage() != null ? 
                toResponseDto(message.getReplyToMessage()) : null)
            .attachments(attachmentDtos)
            .editedAt(message.getEditedAt())
            .createdAt(message.getCreatedAt())
            .isSystemMessage(message.getMessageType() == Message.MessageType.SYSTEM)
            .build();
    }
    
    public Message toEntity(MessageCreateDto createDto, Long roomId, Long senderId) {
        com.fasterxml.jackson.databind.JsonNode attachmentsNode = null;
        if (createDto.getAttachments() != null && !createDto.getAttachments().isEmpty()) {
            try {
                attachmentsNode = objectMapper.valueToTree(createDto.getAttachments());
            } catch (Exception e) {
                // Log error and continue without attachments
                System.err.println("Error serializing message attachments: " + e.getMessage());
            }
        }
        
        return Message.builder()
            .roomId(roomId)
            .senderId(senderId)
            .senderName(createDto.getSenderName())
            .content(createDto.getContent())
            .messageType(Message.MessageType.valueOf(createDto.getMessageType().toUpperCase()))
            .replyToId(createDto.getReplyToId())
            .attachments(attachmentsNode)
            .build();
    }
    
    public List<MessageResponseDto> toResponseDtoList(List<Message> messages) {
        return messages.stream()
            .map(this::toResponseDto)
            .collect(Collectors.toList());
    }
    
    private MessageResponseDto.MessageAttachmentDto toAttachmentResponseDto(
            MessageCreateDto.MessageAttachmentDto attachmentDto) {
        return MessageResponseDto.MessageAttachmentDto.builder()
            .filename(attachmentDto.getFilename())
            .url(attachmentDto.getUrl())
            .contentType(attachmentDto.getContentType())
            .fileSize(attachmentDto.getFileSize())
            .build();
    }
}
