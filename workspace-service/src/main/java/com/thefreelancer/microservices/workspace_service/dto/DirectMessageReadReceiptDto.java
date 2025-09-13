package com.thefreelancer.microservices.workspace_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for direct message read receipts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectMessageReadReceiptDto {
    
    private String messageId;
    private String conversationId;
    private Long readerId; // User who read the message
    private LocalDateTime readAt;
}
