package com.thefreelancer.microservices.payment.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;
    
    /**
     * Update user's Stripe account ID in the auth service
     */
    public boolean updateUserStripeAccountId(String userEmail, String stripeAccountId) {
        try {
            log.debug("Calling Auth Service to update stripe account ID for user: {}", userEmail);
            
            UpdateStripeAccountRequest request = new UpdateStripeAccountRequest(stripeAccountId);
            
            String response = webClientBuilder
                .baseUrl(authServiceUrl)
                .build()
                .put()
                .uri("/api/auth/internal/users/email/{email}/stripe-account", userEmail)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(WebClientResponseException.class,
                    ex -> {
                        log.error("Error updating stripe account ID for user {}: {}", userEmail, ex.getMessage());
                        return Mono.empty();
                    })
                .onErrorResume(Exception.class,
                    ex -> {
                        log.error("Error calling Auth Service for user: {}", userEmail, ex);
                        return Mono.empty();
                    })
                .block();
            
            if (response != null) {
                log.debug("Successfully updated stripe account ID for user: {}", userEmail);
                return true;
            } else {
                log.warn("Failed to update stripe account ID for user: {}", userEmail);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error updating stripe account ID for user: {}", userEmail, e);
            return false;
        }
    }
    
    // Inner class for request body
    public static class UpdateStripeAccountRequest {
        private String stripeAccountId;
        
        public UpdateStripeAccountRequest() {}
        
        public UpdateStripeAccountRequest(String stripeAccountId) {
            this.stripeAccountId = stripeAccountId;
        }
        
        public String getStripeAccountId() {
            return stripeAccountId;
        }
        
        public void setStripeAccountId(String stripeAccountId) {
            this.stripeAccountId = stripeAccountId;
        }
    }
}