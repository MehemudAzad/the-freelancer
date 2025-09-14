package com.thefreelancer.microservices.workspace_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RecentChatPartnerDto {
    private String userId;
    private String name;
    private String handle;
    private String email;
    private long unreadCount;
    private DirectMessageResponseDto lastMessage;
    private LocalDateTime lastActivity;
}
