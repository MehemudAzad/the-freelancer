package com.thefreelancer.microservices.workspace_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for message read receipts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageReadReceiptDto {
    
    private String messageId;
    private String userId;
    private LocalDateTime readAt;
}
