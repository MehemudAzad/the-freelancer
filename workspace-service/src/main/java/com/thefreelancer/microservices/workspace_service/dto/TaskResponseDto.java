package com.thefreelancer.microservices.workspace_service.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;


@Data
@Builder
public class TaskResponseDto {
    
    private Long id;
    private Long roomId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String assigneeId;
    private String createdBy;
    private Integer orderIndex;
    private LocalDate dueDate;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

