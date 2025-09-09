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
        if (message.getAttachments() != null) {
            try {
                List<MessageCreateDto.MessageAttachmentDto> attachments = 
                    objectMapper.readValue(message.getAttachments(), 
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
            .senderName(getSenderName(message.getSenderId())) // TODO: Implement user lookup
            .content(message.getContent())
            .messageType(message.getMessageType().name())
            .replyToId(message.getReplyToId())
            .replyToMessage(message.getReplyToMessage() != null ? 
                toResponseDto(message.getReplyToMessage()) : null)
            .attachments(attachmentDtos)
            .editedAt(message.getEditedAt())
            .createdAt(message.getCreatedAt())
            .isSystemMessage("SYSTEM".equals(message.getSenderId()))
            .build();
    }
    
    public Message toEntity(MessageCreateDto createDto, String roomId, String senderId) {
        String attachmentsJson = null;
        if (createDto.getAttachments() != null && !createDto.getAttachments().isEmpty()) {
            try {
                attachmentsJson = objectMapper.writeValueAsString(createDto.getAttachments());
            } catch (Exception e) {
                // Log error and continue without attachments
                System.err.println("Error serializing message attachments: " + e.getMessage());
            }
        }
        
        return Message.builder()
            .roomId(roomId)
            .senderId(senderId)
            .content(createDto.getContent())
            .messageType(Message.MessageType.valueOf(createDto.getMessageType().toUpperCase()))
            .replyToId(createDto.getReplyToId())
            .attachments(attachmentsJson)
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
    
    private String getSenderName(String senderId) {
        // TODO: Implement user service lookup or cache
        if ("SYSTEM".equals(senderId)) {
            return "System";
        }
        return "User " + senderId; // Temporary placeholder
    }
}
