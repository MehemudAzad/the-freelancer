package com.thefreelancer.microservices.workspace_service.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

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