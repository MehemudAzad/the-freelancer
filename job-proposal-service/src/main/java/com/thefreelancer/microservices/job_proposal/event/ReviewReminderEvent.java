package com.thefreelancer.microservices.job_proposal.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewReminderEvent {
    private String clientId;
    private String freelancerId;
    private String jobId;
    private String jobTitle;
    private String freelancerName;
    private String contractId;
    private Long timestamp;
    private LocalDateTime completedAt;
}