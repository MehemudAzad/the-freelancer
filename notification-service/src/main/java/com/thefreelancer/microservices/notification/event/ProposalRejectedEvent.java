package com.thefreelancer.microservices.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalRejectedEvent {
    private Long proposalId;
    private Long jobId;
    private Long freelancerId;
    private String projectName;
    private String freelancerName;
    private String feedback;
}
