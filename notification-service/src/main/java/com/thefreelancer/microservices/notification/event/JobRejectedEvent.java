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
public class JobRejectedEvent {
    private Long jobId;
    private Long contractId;
    private Long freelancerId;
    private Long clientId;
    private String jobTitle;
    private String freelancerName;
    private String clientName;
    private LocalDateTime rejectedAt;
    private String feedback;
    private String reasonCode;
    private String notes;
}