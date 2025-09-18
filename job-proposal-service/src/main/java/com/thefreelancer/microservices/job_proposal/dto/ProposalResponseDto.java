package com.thefreelancer.microservices.job_proposal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProposalResponseDto {
    
    private Long id;
    private Long jobId;
    private String jobTitle; // Optional job title for display
    private Long freelancerId;
    private String coverLetter;
    private BigDecimal proposedRate;
    private Integer deliveryDays;
    private String portfolioLinks;
    private String additionalNotes;
    private String status; // SUBMITTED, ACCEPTED, REJECTED, WITHDRAWN, etc.
    private String createdAt; // ISO date string
    private String updatedAt; // ISO date string
    private Long contractId; // Populated when proposal is accepted and contract is created
    
    // Freelancer information (populated by backend)
    private FreelancerInfoDto freelancerInfo;
}
