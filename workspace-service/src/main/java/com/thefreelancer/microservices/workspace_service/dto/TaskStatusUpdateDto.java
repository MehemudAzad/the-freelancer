package com.thefreelancer.microservices.workspace_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusUpdateDto {
    
    @NotBlank(message = "Status is required")
    private String status; // TODO, IN_PROGRESS, REVIEW, COMPLETED, BLOCKED
}