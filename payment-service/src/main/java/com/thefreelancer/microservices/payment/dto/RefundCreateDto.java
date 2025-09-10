package com.thefreelancer.microservices.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RefundCreateDto {
    
    @NotNull(message = "Escrow ID is required")
    private String escrowId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Long amountCents;
    
    private String reason;
}
