package com.thefreelancer.microservices.job_proposal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneRejectionDto {
    
    @NotBlank(message = "Rejection reason is required")
    private String reason;
    
    private String feedback; // Additional feedback for improvement
    
    private boolean pauseContract; // Whether to pause the contract
}