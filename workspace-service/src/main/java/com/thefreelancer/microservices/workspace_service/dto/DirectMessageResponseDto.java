package com.thefreelancer.microservices.workspace_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectMessageResponseDto {
    
    private String id;
    private String senderId;
    private String senderName; // We'll populate this from user service or cache
    private String receiverId;
    private String receiverName; // We'll populate this from user service or cache
    private String content;
    private String messageType;
    private String replyToId;
    private DirectMessageResponseDto replyToMessage; // Nested message for thread context
    private List<MessageAttachmentDto> attachments;
    private boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime editedAt;
    private LocalDateTime createdAt;
    private boolean isSystemMessage;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageAttachmentDto {
        private String filename;
        private String url;
        private String contentType;
        private Long fileSize;
    }
}
