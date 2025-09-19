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
public class JobRejectedEvent {
    private Long contractId;
    private Long jobId;
    private Long clientId;
    private Long freelancerId;
    private String jobTitle;
    private String rejectionReason;
    private String rejectionFeedback;
    private LocalDateTime rejectedAt;
}