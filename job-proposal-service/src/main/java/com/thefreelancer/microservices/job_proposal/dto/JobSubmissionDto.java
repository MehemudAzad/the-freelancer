package com.thefreelancer.microservices.job_proposal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSubmissionDto {
    
    @NotBlank(message = "Submission description is required")
    private String description;
    
    private List<String> deliverableUrls; // URLs to files, repos, or preview links
    
    private String notes; // Additional notes from freelancer
}