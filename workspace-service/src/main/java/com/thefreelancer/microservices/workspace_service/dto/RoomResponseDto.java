package com.thefreelancer.microservices.workspace_service.dto;

import com.thefreelancer.microservices.workspace_service.model.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponseDto {
    
    private Long id;
    private Long contractId;
    private String jobTitle;
    private Long clientId;
    private Long freelancerId;
    private Room.RoomStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Statistics for the room
    private Long messageCount;
    private Long fileCount;
    private Long taskCount;
    private Long eventCount;
}
