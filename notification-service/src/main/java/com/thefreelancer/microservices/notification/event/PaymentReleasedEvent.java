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
public class PaymentReleasedEvent {
    private Long paymentId;
    private Long jobId;
    private Long contractId;
    private Long freelancerId;
    private Long clientId;
    private String jobTitle;
    private Long amountCents;
    private String currency;
    private LocalDateTime releasedAt;
    private String paymentMethod;
    private String transactionId;
    private String freelancerName;
    private String clientName;
}