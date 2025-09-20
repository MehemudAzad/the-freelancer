package com.thefreelancer.microservices.job_proposal.dto;

import com.thefreelancer.microservices.job_proposal.model.Contract;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractStatusUpdateDto {
    
    @NotNull(message = "Status is required")
    private Contract.ContractStatus status;
    
    private String reason; // Optional reason for status change
}
