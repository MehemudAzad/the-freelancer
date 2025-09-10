package com.thefreelancer.microservices.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PayoutResponseDto {
    
    private String id;
    private Long milestoneId;
    private String transferId;
    private String destinationAccountId;
    private Long amountCents;
    private Long feeCents;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
}
