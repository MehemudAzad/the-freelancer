package com.thefreelancer.microservices.job_proposal.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteReceivedEvent {
    private String inviteId;
    private String clientId;
    private String freelancerId;
    private String jobId;
    private String jobTitle;
    private String freelancerName;
    private String clientName;
    private Long timestamp;
}