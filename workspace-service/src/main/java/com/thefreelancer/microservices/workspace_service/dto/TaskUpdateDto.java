package com.thefreelancer.microservices.workspace_service.dto;

import lombok.Data;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class TaskUpdateDto {
    
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    private String status; // TODO, IN_PROGRESS, REVIEW, DONE
    
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    
    private String assigneeId;
    
    private LocalDateTime dueDate;
    
    private Integer orderIndex;
}
