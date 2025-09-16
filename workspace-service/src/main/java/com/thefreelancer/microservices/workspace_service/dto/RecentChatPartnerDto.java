package com.thefreelancer.microservices.workspace_service.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecentChatPartnerDto {
    private String userId;
    private String name;
    private String handle;
    private String email;
    private long unreadCount;
    private String role;
    private DirectMessageResponseDto lastMessage;
    private LocalDateTime lastActivity;
}
