package com.thefreelancer.microservices.auth.controller;

import com.thefreelancer.microservices.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/internal")
@RequiredArgsConstructor
@Slf4j
public class InternalController {
    
    private final UserService userService;
    
    /**
     * Internal endpoint for other services to update user's Stripe account ID
     */
    @PutMapping("/users/email/{email}/stripe-account")
    public ResponseEntity<String> updateUserStripeAccount(
            @PathVariable String email, 
            @RequestBody UpdateStripeAccountRequest request) {
        
        log.info("Internal request to update stripe account ID for user: {}", email);
        
        try {
            boolean updated = userService.updateStripeAccountId(email, request.getStripeAccountId());
            
            if (updated) {
                log.info("Successfully updated stripe account ID for user: {}", email);
                return ResponseEntity.ok("Stripe account ID updated successfully");
            } else {
                log.warn("Failed to update stripe account ID - user not found: {}", email);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error updating stripe account ID for user: {}", email, e);
            return ResponseEntity.internalServerError().body("Internal server error");
        }
    }
    
    // DTO for request body
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