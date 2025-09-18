package com.thefreelancer.microservices.job_proposal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractCreateDto {
    
    @NotNull(message = "Job ID is required")
    private Long jobId;
    
    // Proposal ID is optional for invite-based contracts
    private Long proposalId;
    
    // Client ID and Freelancer ID for invite-based contracts
    private Long clientId;
    private Long freelancerId;
    
    // Total amount is optional - can be derived from job budget or proposal
    @Positive(message = "Total amount must be positive")
    private Long totalAmountCents;
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    private Map<String, Object> terms;
}
