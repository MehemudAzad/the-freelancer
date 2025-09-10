package com.thefreelancer.microservices.workspace_service.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

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
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
@Builder
public class TaskListResponseDto {
    
    private List<TaskResponseDto> tasks;
    private int totalTasks;
    private TaskStats stats;
    
    @Data
    @Builder
    public static class TaskStats {
        private long todoCount;
        private long inProgressCount;
        private long reviewCount;
        private long doneCount;
        private long overdueTasks;
    }
}
