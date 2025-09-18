package com.thefreelancer.microservices.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripeAccountStatusDto {
    
    private Long userId;
    private boolean hasStripeAccount;
    private String stripeAccountId; // Optional: can be null if not registered
    
    public static StripeAccountStatusDto registered(Long userId, String stripeAccountId) {
        return StripeAccountStatusDto.builder()
                .userId(userId)
                .hasStripeAccount(true)
                .stripeAccountId(stripeAccountId)
                .build();
    }
    
    public static StripeAccountStatusDto notRegistered(Long userId) {
        return StripeAccountStatusDto.builder()
                .userId(userId)
                .hasStripeAccount(false)
                .stripeAccountId(null)
                .build();
    }
}