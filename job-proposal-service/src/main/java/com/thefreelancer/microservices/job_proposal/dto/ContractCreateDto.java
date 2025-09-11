package com.thefreelancer.microservices.job_proposal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractCreateDto {
    
    @NotNull(message = "Job ID is required")
    private Long jobId;
    
    @NotNull(message = "Proposal ID is required")
    private Long proposalId;
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    private Map<String, Object> terms;
}
