package com.thefreelancer.microservices.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class EscrowCreateDto {
    
    @NotNull(message = "Milestone ID is required")
    private Long milestoneId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Long amountCents;
    
    @NotNull(message = "Currency is required")
    private String currency;
    
    @NotNull(message = "Payment method ID is required")
    private String paymentMethodId;
    
    private String description;
}
