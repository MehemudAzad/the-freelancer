package com.thefreelancer.microservices.payment.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.thefreelancer.microservices.payment.dto.ConnectedAccountDto;
import com.thefreelancer.microservices.payment.service.StripeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Account Management", description = "APIs for managing Stripe connected accounts for freelancers")
public class AccountController {

    private final StripeService stripeService;

    /**
     * Create Stripe Express account for freelancer
     */
    @Operation(
        summary = "Create Stripe Express account",
        description = "Creates a Stripe Express connected account for a freelancer to receive payments"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Connected account created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Stripe API error")
    })
    @PostMapping("/create")
    public ResponseEntity<ConnectedAccountDto> createConnectedAccount(
            @Valid @RequestBody ConnectedAccountDto accountDto) {
        
        log.info("Creating Stripe Express account for email: {}", accountDto.getEmail());
        
        try {
            Account account = stripeService.createConnectedAccount(
                accountDto.getEmail(), 
                accountDto.getCountry()
            );
            
            ConnectedAccountDto response = ConnectedAccountDto.builder()
                .accountId(account.getId())
                .email(account.getEmail())
                .country(account.getCountry())
                .chargesEnabled(account.getChargesEnabled())
                .payoutsEnabled(account.getPayoutsEnabled())
                .detailsSubmitted(account.getDetailsSubmitted())
                .type(account.getType())
                .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (StripeException e) {
            log.error("Stripe error creating account: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error creating connected account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Create account link for onboarding
     */
    @PostMapping("/{accountId}/onboarding-link")
    public ResponseEntity<AccountLinkDto> createAccountLink(
            @PathVariable String accountId,
            @RequestParam String refreshUrl,
            @RequestParam String returnUrl) {
        
        log.info("Creating account link for account: {}", accountId);
        
        try {
            AccountLink link = stripeService.createAccountLink(accountId, refreshUrl, returnUrl);
            
            AccountLinkDto response = AccountLinkDto.builder()
                .url(link.getUrl())
                .expiresAt(link.getExpiresAt())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (StripeException e) {
            log.error("Stripe error creating account link: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error creating account link", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get account information
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<ConnectedAccountDto> getAccount(@PathVariable String accountId) {
        log.info("Getting account information for: {}", accountId);
        
        try {
            Account account = Account.retrieve(accountId);
            
            ConnectedAccountDto response = ConnectedAccountDto.builder()
                .accountId(account.getId())
                .email(account.getEmail())
                .country(account.getCountry())
                .chargesEnabled(account.getChargesEnabled())
                .payoutsEnabled(account.getPayoutsEnabled())
                .detailsSubmitted(account.getDetailsSubmitted())
                .type(account.getType())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (StripeException e) {
            log.error("Stripe error retrieving account: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error retrieving account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // DTOs
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AccountLinkDto {
        private String url;
        private Long expiresAt;
    }
}
