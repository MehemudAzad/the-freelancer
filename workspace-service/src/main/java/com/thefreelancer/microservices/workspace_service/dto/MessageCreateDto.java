package com.thefreelancer.microservices.workspace_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreateDto {
    
    private Long senderId; // Set by WebSocket controller or extracted from auth headers
    
    @NotBlank(message = "Sender name is required")
    private String senderName;
    
    @NotBlank(message = "Message content is required")
    @Size(max = 5000, message = "Message content cannot exceed 5000 characters")
    private String content;
    
    @Builder.Default
    private String messageType = "TEXT"; // TEXT, FILE, IMAGE, VIDEO, DOCUMENT, SYSTEM
    
    private Long replyToId; // For threaded conversations (message ID)
    
    private List<MessageAttachmentDto> attachments; // File attachments
    
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
