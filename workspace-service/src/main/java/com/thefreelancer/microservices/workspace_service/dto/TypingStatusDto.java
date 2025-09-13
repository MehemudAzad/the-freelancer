package com.thefreelancer.microservices.workspace_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for typing status indicators in real-time chat
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingStatusDto {
    
    private String userId;
    private String roomId;
    private boolean typing;
    private long timestamp;
    
    public TypingStatusDto(String userId, String roomId, boolean typing) {
        this.userId = userId;
        this.roomId = roomId;
        this.typing = typing;
        this.timestamp = System.currentTimeMillis();
    }
}
