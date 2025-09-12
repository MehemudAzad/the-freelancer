package com.thefreelancer.microservices.payment.service;

import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class StripeService {
    
    @Value("${stripe.platform-fee-percentage:5}")
    private int platformFeePercentage;
    
    /**
     * Create a Stripe Express account for freelancers
     */
    public Account createConnectedAccount(String email, String country) throws StripeException {
        log.info("Creating Stripe Express account for email: {}", email);
        
        AccountCreateParams params = AccountCreateParams.builder()
            .setType(AccountCreateParams.Type.EXPRESS)
            .setEmail(email)
            .setCountry(country)
            .setCapabilities(
                AccountCreateParams.Capabilities.builder()
                    .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                        .setRequested(true).build())
                    .build())
            .build();
        
        Account account = Account.create(params);
        log.info("Created Stripe account: {}", account.getId());
        return account;
    }
    
    /**
     * Create Payment Intent for escrow (hold money) - FIXED VERSION
     */
    public PaymentIntent createEscrowPaymentIntent(Long milestoneId, Long amountCents, String currency, String customerId, String paymentMethodId) throws StripeException {
        log.info("Creating escrow payment intent for milestone: {} amount: {}", milestoneId, amountCents);
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("milestone_id", milestoneId.toString());
        metadata.put("type", "escrow");
        
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amountCents)
            .setCurrency(currency)
            .setCustomer(customerId)  // Client's Stripe customer ID
            .setPaymentMethod(paymentMethodId)  // Client's payment method
            .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.AUTOMATIC)
            .setConfirm(true)  // Charge immediately
            .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.AUTOMATIC)  // Capture the payment
            .putAllMetadata(metadata)
            .build();
        
        PaymentIntent intent = PaymentIntent.create(params);
        log.info("Created and charged payment intent: {}", intent.getId());
        return intent;
    }

    /**
     * Create a customer for the client if they don't have one
     */
    public Customer createCustomer(String email, String name) throws StripeException {
        log.info("Creating Stripe customer for: {}", email);
        
        CustomerCreateParams params = CustomerCreateParams.builder()
            .setEmail(email)
            .setName(name)
            .build();
        
        Customer customer = Customer.create(params);
        log.info("Created customer: {}", customer.getId());
        return customer;
    }

    /**
     * Attach payment method to customer
     */
    public PaymentMethod attachPaymentMethodToCustomer(String paymentMethodId, String customerId) throws StripeException {
        log.info("Attaching payment method {} to customer {}", paymentMethodId, customerId);
        
        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
        PaymentMethodAttachParams params = PaymentMethodAttachParams.builder()
            .setCustomer(customerId)
            .build();
        
        PaymentMethod attachedPm = paymentMethod.attach(params);
        log.info("Attached payment method successfully");
        return attachedPm;
    }

    /**
     * Capture payment intent (when funding escrow)
     */
    public PaymentIntent capturePaymentIntent(String paymentIntentId) throws StripeException {
        log.info("Capturing payment intent: {}", paymentIntentId);
        
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
        PaymentIntent capturedIntent = intent.capture();
        
        log.info("Captured payment intent: {}", paymentIntentId);
        return capturedIntent;
    }
    
    /**
     * Transfer money to freelancer (release escrow)
     */
    public Transfer releaseEscrowToFreelancer(String destinationAccountId, Long amountCents, String currency, Long milestoneId) throws StripeException {
        log.info("Releasing escrow to freelancer: {} amount: {}", destinationAccountId, amountCents);
        
        // Calculate platform fee
        Long platformFee = calculatePlatformFee(amountCents);
        Long freelancerAmount = amountCents - platformFee;
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("milestone_id", milestoneId.toString());
        metadata.put("type", "escrow_release");
        metadata.put("platform_fee", platformFee.toString());
        
        TransferCreateParams params = TransferCreateParams.builder()
            .setAmount(freelancerAmount)
            .setCurrency(currency)
            .setDestination(destinationAccountId)
            .putAllMetadata(metadata)
            .build();
        
        Transfer transfer = Transfer.create(params);
        log.info("Created transfer: {}", transfer.getId());
        return transfer;
    }
    
    /**
     * Refund payment
     */
    public Refund createRefund(String paymentIntentId, Long amountCents, String reason) throws StripeException {
        log.info("Creating refund for payment intent: {} amount: {}", paymentIntentId, amountCents);
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("reason", reason != null ? reason : "Refund requested");
        
        RefundCreateParams params = RefundCreateParams.builder()
            .setPaymentIntent(paymentIntentId)
            .setAmount(amountCents)
            .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
            .putAllMetadata(metadata)
            .build();
        
        Refund refund = Refund.create(params);
        log.info("Created refund: {}", refund.getId());
        return refund;
    }
    
    /**
     * Calculate platform fee
     */
    public Long calculatePlatformFee(Long amountCents) {
        return Math.round(amountCents * platformFeePercentage / 100.0);
    }
    
    /**
     * Create account link for onboarding
     */
    public AccountLink createAccountLink(String accountId, String refreshUrl, String returnUrl) throws StripeException {
        log.info("Creating account link for account: {}", accountId);
        
        AccountLinkCreateParams params = AccountLinkCreateParams.builder()
            .setAccount(accountId)
            .setRefreshUrl(refreshUrl)
            .setReturnUrl(returnUrl)
            .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
            .build();
        
        AccountLink link = AccountLink.create(params);
        log.info("Created account link: {}", link.getUrl());
        return link;
    }
}
