package com.thefreelancer.microservices.job_proposal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProposalResponseDto {
    
    private Long id;
    private Long jobId;
    private String jobTitle; // Included for convenience
    private Long freelancerId;
    private String freelancerName; // Included for convenience
    private String coverLetter;
    private BigDecimal proposedRate;
    private Integer deliveryDays;
    private String portfolioLinks;
    private String additionalNotes;
    private String status; // SUBMITTED, ACCEPTED, REJECTED, WITHDRAWN
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
}
