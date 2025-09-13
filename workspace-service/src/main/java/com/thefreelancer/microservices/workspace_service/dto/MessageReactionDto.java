package com.thefreelancer.microservices.workspace_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for message reactions/emojis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageReactionDto {
    
    private String messageId;
    private String userId;
    private String emoji;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageReactionSummaryDto {
        private String emoji;
        private int count;
        private List<String> userIds; // Users who reacted with this emoji
    }
}
