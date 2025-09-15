package com.thefreelancer.microservices.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalAcceptedEvent {
    private Long proposalId;
    private Long jobId;
    private Long freelancerId;
    private Long clientId;
    private String projectName;
    private String freelancerName;
}
