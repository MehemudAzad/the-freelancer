package com.thefreelancer.microservices.job_proposal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteCreateDto {
    
    @NotNull(message = "Job ID is required")
    private Long jobId;
    
    private Long clientId; // Will be set from authenticated user
    
    @NotNull(message = "Freelancer ID is required")
    private Long freelancerId;
}