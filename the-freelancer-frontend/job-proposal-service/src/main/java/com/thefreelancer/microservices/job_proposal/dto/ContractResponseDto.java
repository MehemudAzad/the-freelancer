package com.thefreelancer.microservices.job_proposal.dto;

import com.thefreelancer.microservices.job_proposal.model.Contract;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponseDto {
    
    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long proposalId;
    private Long clientId;
    private Long freelancerId;
    private BigInteger totalAmountCents;
    private Contract.ContractStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Summary information
    private int totalMilestones;
    private int completedMilestones;
    private int activeMilestones;
    
    // Optional: Include milestones in response
    private List<ContractMilestoneResponseDto> milestones;
}
