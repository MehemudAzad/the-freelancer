package com.thefreelancer.microservices.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EscrowFundedEvent {
    private String escrowId;
    private String clientId;
    private String freelancerId;
    private String jobId;
    private String jobTitle;
    private String freelancerName;
    private Long amountCents;
    private String currency;
    private String paymentIntentId;
    private Long timestamp;
}