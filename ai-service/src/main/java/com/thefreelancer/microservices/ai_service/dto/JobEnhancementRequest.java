package com.thefreelancer.microservices.ai_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request DTO for job description enhancement
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobEnhancementRequest {
    
    @NotBlank(message = "Job title is required")
    @Size(max = 200, message = "Job title must not exceed 200 characters")
    private String title;
    
    @NotBlank(message = "Job description is required")
    @Size(max = 5000, message = "Job description must not exceed 5000 characters")
    private String description;
    
    @Size(max = 100, message = "Budget range must not exceed 100 characters")
    private String budgetRange;
    
    @Size(max = 20, message = "Maximum 20 skills allowed")
    private List<String> requiredSkills;
    
    @Size(max = 50, message = "Job category must not exceed 50 characters")
    private String category;
    
    @Size(max = 50, message = "Experience level must not exceed 50 characters")
    private String experienceLevel;
    
    @Size(max = 50, message = "Project duration must not exceed 50 characters")
    private String projectDuration;
    
    private Boolean remoteWork;
    
    // Additional context for enhancement
    @Size(max = 1000, message = "Additional context must not exceed 1000 characters")
    private String additionalContext;
}