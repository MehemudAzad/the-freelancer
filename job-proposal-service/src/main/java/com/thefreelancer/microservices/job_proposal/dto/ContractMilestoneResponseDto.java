package com.thefreelancer.microservices.job_proposal.dto;

import com.thefreelancer.microservices.job_proposal.model.ContractMilestone;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractMilestoneResponseDto {
    
    private Long id;
    private Long contractId;
    private String title;
    private String description;
    private BigInteger amountCents;
    private ContractMilestone.MilestoneStatus status;
    private LocalDate dueDate;
    private Integer orderIndex;
    private LocalDateTime submittedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime rejectedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
