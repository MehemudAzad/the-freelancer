package com.thefreelancer.microservices.workspace_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for typing status indicators in direct messaging
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectMessageTypingStatusDto {
    
    private String userId;
    private String conversationId;
    private boolean isTyping;
    private LocalDateTime timestamp;
    
    public DirectMessageTypingStatusDto(String userId, String conversationId, boolean isTyping) {
        this.userId = userId;
        this.conversationId = conversationId;
        this.isTyping = isTyping;
        this.timestamp = LocalDateTime.now();
    }
}
