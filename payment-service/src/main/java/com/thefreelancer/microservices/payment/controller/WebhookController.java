package com.thefreelancer.microservices.payment.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import com.thefreelancer.microservices.payment.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "APIs for handling external webhook events from payment providers")
public class WebhookController {

    private final WebhookService webhookService;
    
    @Value("${stripe.webhook.endpoint-secret}")
    private String endpointSecret;

    /**
     * Handle Stripe webhook events
     */
    @Operation(
        summary = "Handle Stripe webhook events",
        description = "Receives and processes webhook events from Stripe for payment status updates"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid signature or payload"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @Parameter(description = "Webhook payload from Stripe") @RequestBody String payload,
            @Parameter(description = "Stripe signature header for verification") @RequestHeader("Stripe-Signature") String sigHeader) {
        
        log.info("Received Stripe webhook with signature: {}", sigHeader);
        
        try {
            // Verify webhook signature
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            
            log.info("Processing Stripe event: {} with ID: {}", event.getType(), event.getId());
            
            // Handle the event
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    webhookService.handlePaymentIntentSucceeded(event);
                    break;
                    
                case "payment_intent.payment_failed":
                    webhookService.handlePaymentIntentFailed(event);
                    break;
                    
                case "payment_intent.canceled":
                    webhookService.handlePaymentIntentCanceled(event);
                    break;
                    
                case "transfer.created":
                    webhookService.handleTransferCreated(event);
                    break;
                    
                case "transfer.reversed":
                    webhookService.handleTransferReversed(event);
                    break;
                    
                case "transfer.updated":
                    webhookService.handleTransferUpdated(event);
                    break;
                    
                case "charge.dispute.created":
                    webhookService.handleChargeDisputeCreated(event);
                    break;
                    
                case "account.updated":
                    webhookService.handleAccountUpdated(event);
                    break;
                    
                case "invoice.payment_succeeded":
                    webhookService.handleInvoicePaymentSucceeded(event);
                    break;
                    
                case "invoice.payment_failed":
                    webhookService.handleInvoicePaymentFailed(event);
                    break;
                    
                default:
                    log.info("Unhandled event type: {}", event.getType());
                    break;
            }
            
            return ResponseEntity.ok("Webhook processed successfully");
            
        } catch (SignatureVerificationException e) {
            log.error("Invalid signature for webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
            
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook processing failed");
        }
    }
    
    /**
     * Health check for webhook endpoint
     */
    @GetMapping("/stripe/health")
    public ResponseEntity<String> webhookHealth() {
        return ResponseEntity.ok("Webhook endpoint is healthy");
    }
}
