package com.thefreelancer.microservices.ai_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImproveProposalRequest {
    @NotBlank(message = "Proposal content is required")
    @Size(max = 10000, message = "Proposal content must not exceed 10000 characters")
    private String proposalContent;
    
    @Size(max = 5000, message = "Job description must not exceed 5000 characters")
    private String jobDescription; // Optional: for context
    
    @Size(max = 200, message = "Job title must not exceed 200 characters")
    private String jobTitle; // Optional: for context
}
