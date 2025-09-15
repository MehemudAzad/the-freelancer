package com.thefreelancer.microservices.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class EscrowCreateDto {
    
    @NotNull(message = "Job ID is required")
    private Long jobId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Long amountCents;
    
    @NotNull(message = "Currency is required")
    private String currency;
    
    // Client's payment information
    private String clientStripeCustomerId;  // Optional if clientEmail provided
    
    @NotNull(message = "Payment method ID is required")
    private String paymentMethodId;
    
    private String description;
    
    // Optional: For clients who don't have a Stripe customer yet
    private String clientEmail;
    private String clientName;
}
