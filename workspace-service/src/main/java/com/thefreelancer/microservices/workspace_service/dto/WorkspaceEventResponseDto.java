package com.thefreelancer.microservices.workspace_service.dto;

import com.thefreelancer.microservices.workspace_service.model.WorkspaceEvent.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceEventResponseDto {
    private Long id;
    private Long roomId;
    private String title;
    private String description;
    private EventType eventType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
