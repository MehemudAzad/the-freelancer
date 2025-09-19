package com.thefreelancer.microservices.job_proposal.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalAcceptedEvent {
    private Long proposalId;
    private Long jobId;
    private Long freelancerId;
    private Long clientId;
    private String projectName;
    private String freelancerName;
}