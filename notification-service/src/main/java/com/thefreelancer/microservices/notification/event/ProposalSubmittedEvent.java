package com.thefreelancer.microservices.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalSubmittedEvent {
    private Long proposalId;
    private Long jobId;
    private Long freelancerId;
    private Long clientId;
    private String jobTitle;
    private String freelancerName;
    private String freelancerHandle;
    private LocalDateTime submittedAt;
    private String proposalCoverLetter;
    
    // Additional context
    private Long totalBudget;
    private String currency;
    private Integer deliveryDays;
}
