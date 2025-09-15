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
public class JobSubmittedEvent {
    private Long jobId;
    private Long freelancerId;
    private Long clientId;
    private String jobTitle;
    private String jobDescription;
    private String freelancerName;
    private String clientName;
    private LocalDateTime submittedAt;
    private String deliverableUrl;
    private String notes;
    private Long amountCents;
    private String currency;
}