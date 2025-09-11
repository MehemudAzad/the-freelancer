package com.thefreelancer.microservices.job_proposal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProposalMilestoneResponseDto {
    
    private Long id;
    private Long proposalId;
    private String title;
    private String description;
    private BigDecimal amountCents;
    private String currency;
    private LocalDate dueDate;
    private Integer orderIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
