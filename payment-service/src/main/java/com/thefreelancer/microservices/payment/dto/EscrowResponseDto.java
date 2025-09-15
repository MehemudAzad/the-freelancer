package com.thefreelancer.microservices.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EscrowResponseDto {
    
    private String id;
    private Long jobId;
    private String paymentIntentId;
    private Long amountCents;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // For client payment confirmation
    private String clientSecret;
}
