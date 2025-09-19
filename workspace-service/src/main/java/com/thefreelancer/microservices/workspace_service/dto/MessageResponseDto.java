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
public class MessageResponseDto {
    
    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderName; // We'll populate this from user service or cache
    private String content;
    private String messageType;
    private Long replyToId;
    private List<MessageAttachmentDto> attachments;
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
