package com.thefreelancer.microservices.job_proposal.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractCreatedEvent {
    private Long contractId;
    private Long jobId;
    private Long proposalId;
    private Long clientId;
    private Long freelancerId;
    private String jobTitle;
    private String clientName;
    private String freelancerName;
    private LocalDateTime createdAt;
    private String contractTerms;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long totalBudget;
    private String currency;
}