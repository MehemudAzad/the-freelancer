package com.thefreelancer.microservices.payment.service;

import com.stripe.model.*;
import com.thefreelancer.microservices.payment.model.Escrow;
import com.thefreelancer.microservices.payment.model.Ledger;
import com.thefreelancer.microservices.payment.model.Payout;
import com.thefreelancer.microservices.payment.repository.EscrowRepository;
import com.thefreelancer.microservices.payment.repository.LedgerRepository;
import com.thefreelancer.microservices.payment.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final EscrowRepository escrowRepository;
    private final PayoutRepository payoutRepository;
    private final LedgerRepository ledgerRepository;

    /**
     * Handle successful payment intent (escrow funding completed)
     */
    @Transactional
    public void handlePaymentIntentSucceeded(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElseThrow(() -> new RuntimeException("Failed to deserialize payment intent"));

            String milestoneIdStr = paymentIntent.getMetadata().get("milestone_id");
            
            if (milestoneIdStr != null) {
                Long milestoneId = Long.parseLong(milestoneIdStr);
                
                // Update escrow status to HELD
                escrowRepository.findByMilestoneId(milestoneId)
                    .ifPresentOrElse(
                        escrow -> {
                            escrow.setStatus(Escrow.EscrowStatus.HELD);
                            escrow.setUpdatedAt(LocalDateTime.now());
                            escrowRepository.save(escrow);
                            log.info("Updated escrow status to HELD for milestone: {}", milestoneId);
                        },
                        () -> log.warn("Escrow not found for milestone: {}", milestoneId)
                    );
                
                // Create ledger entry
                createLedgerEntry(
                    Ledger.LedgerType.CHARGE,
                    paymentIntent.getId(),
                    null,
                    paymentIntent.getAmount(),
                    paymentIntent.getCurrency(),
                    Map.of("milestone_id", milestoneIdStr, "event_type", "payment_intent.succeeded")
                );
            }
            
        } catch (Exception e) {
            log.error("Error handling payment_intent.succeeded", e);
        }
    }

    /**
     * Handle failed payment intent
     */
    @Transactional
    public void handlePaymentIntentFailed(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElseThrow(() -> new RuntimeException("Failed to deserialize payment intent"));

            String milestoneIdStr = paymentIntent.getMetadata().get("milestone_id");
            
            if (milestoneIdStr != null) {
                Long milestoneId = Long.parseLong(milestoneIdStr);
                
                // Update escrow status to FAILED
                escrowRepository.findByMilestoneId(milestoneId)
                    .ifPresentOrElse(
                        escrow -> {
                            escrow.setStatus(Escrow.EscrowStatus.FAILED);
                            escrow.setUpdatedAt(LocalDateTime.now());
                            escrowRepository.save(escrow);
                            log.warn("Updated escrow status to FAILED for milestone: {}", milestoneId);
                        },
                        () -> log.warn("Escrow not found for milestone: {}", milestoneId)
                    );
                
                // Create ledger entry
                createLedgerEntry(
                    Ledger.LedgerType.CHARGE,
                    paymentIntent.getId(),
                    null,
                    paymentIntent.getAmount(),
                    paymentIntent.getCurrency(),
                    Map.of("milestone_id", milestoneIdStr, "event_type", "payment_intent.failed", "failure_reason", paymentIntent.getLastPaymentError() != null ? paymentIntent.getLastPaymentError().getMessage() : "Unknown")
                );
            }
            
        } catch (Exception e) {
            log.error("Error handling payment_intent.failed", e);
        }
    }

    /**
     * Handle canceled payment intent
     */
    @Transactional
    public void handlePaymentIntentCanceled(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElseThrow(() -> new RuntimeException("Failed to deserialize payment intent"));

            String milestoneIdStr = paymentIntent.getMetadata().get("milestone_id");
            
            if (milestoneIdStr != null) {
                Long milestoneId = Long.parseLong(milestoneIdStr);
                
                // Update escrow status to CANCELED
                escrowRepository.findByMilestoneId(milestoneId)
                    .ifPresentOrElse(
                        escrow -> {
                            escrow.setStatus(Escrow.EscrowStatus.CANCELED);
                            escrow.setUpdatedAt(LocalDateTime.now());
                            escrowRepository.save(escrow);
                            log.info("Updated escrow status to CANCELED for milestone: {}", milestoneId);
                        },
                        () -> log.warn("Escrow not found for milestone: {}", milestoneId)
                    );
            }
            
        } catch (Exception e) {
            log.error("Error handling payment_intent.canceled", e);
        }
    }

    /**
     * Handle transfer creation (payout initiated)
     */
    @Transactional
    public void handleTransferCreated(Event event) {
        try {
            Transfer transfer = (Transfer) event.getDataObjectDeserializer()
                .getObject().orElseThrow(() -> new RuntimeException("Failed to deserialize transfer"));

            String milestoneIdStr = transfer.getMetadata().get("milestone_id");
            
            if (milestoneIdStr != null) {
                Long milestoneId = Long.parseLong(milestoneIdStr);
                
                // Create or update payout record
                Payout payout = new Payout();
                payout.setId(UUID.randomUUID().toString());
                payout.setMilestoneId(milestoneId);
                payout.setTransferId(transfer.getId());
                payout.setDestinationAccountId(transfer.getDestination());
                payout.setAmountCents(transfer.getAmount());
                payout.setCurrency(transfer.getCurrency());
                payout.setStatus(Payout.PayoutStatus.PAID); // Transfer created means it's being processed/paid
                payout.setCreatedAt(LocalDateTime.now());
                
                payoutRepository.save(payout);
                log.info("Created payout record for transfer: {} with status PAID", transfer.getId());
                
                // Update escrow status to RELEASED since transfer is being processed
                escrowRepository.findByMilestoneId(milestoneId)
                    .ifPresent(escrow -> {
                        escrow.setStatus(Escrow.EscrowStatus.RELEASED);
                        escrow.setUpdatedAt(LocalDateTime.now());
                        escrowRepository.save(escrow);
                        log.info("Updated escrow status to RELEASED for milestone: {}", milestoneId);
                    });
                
                // Create ledger entry
                createLedgerEntry(
                    Ledger.LedgerType.TRANSFER,
                    transfer.getId(),
                    transfer.getDestination(),
                    transfer.getAmount(),
                    transfer.getCurrency(),
                    Map.of("milestone_id", milestoneIdStr, "event_type", "transfer.created")
                );
            }
            
        } catch (Exception e) {
            log.error("Error handling transfer.created", e);
        }
    }

        /**
     * Handle transfer reversed (formerly transfer.paid - repurposed for reversals)
     */
    @Transactional
    public void handleTransferReversed(Event event) {
        try {
            Transfer transfer = (Transfer) event.getDataObjectDeserializer()
                .getObject().orElseThrow(() -> new RuntimeException("Failed to deserialize transfer"));

            // Update payout status to FAILED due to reversal
            payoutRepository.findByTransferId(transfer.getId())
                .ifPresentOrElse(
                    payout -> {
                        payout.setStatus(Payout.PayoutStatus.FAILED);
                        payout.setUpdatedAt(LocalDateTime.now());
                        payoutRepository.save(payout);
                        log.warn("Updated payout status to FAILED due to reversal for transfer: {}", transfer.getId());
                        
                        // Update escrow status back to HELD
                        escrowRepository.findByMilestoneId(payout.getMilestoneId())
                            .ifPresent(escrow -> {
                                escrow.setStatus(Escrow.EscrowStatus.HELD);
                                escrow.setUpdatedAt(LocalDateTime.now());
                                escrowRepository.save(escrow);
                                log.warn("Updated escrow status back to HELD due to transfer reversal for milestone: {}", payout.getMilestoneId());
                            });
                    },
                    () -> log.warn("Payout not found for transfer: {}", transfer.getId())
                );
            
        } catch (Exception e) {
            log.error("Error handling transfer.reversed", e);
        }
    }

    /**
     * Handle transfer failure (payout failed)
     */
    @Transactional
    public void handleTransferFailed(Event event) {
        try {
            Transfer transfer = (Transfer) event.getDataObjectDeserializer()
                .getObject().orElseThrow(() -> new RuntimeException("Failed to deserialize transfer"));

            // Update payout status
            payoutRepository.findByTransferId(transfer.getId())
                .ifPresentOrElse(
                    payout -> {
                        payout.setStatus(Payout.PayoutStatus.FAILED);
                        payout.setUpdatedAt(LocalDateTime.now());
                        payoutRepository.save(payout);
                        log.error("Updated payout status to FAILED for transfer: {}", transfer.getId());
                    },
                    () -> log.warn("Payout not found for transfer: {}", transfer.getId())
                );
            
        } catch (Exception e) {
            log.error("Error handling transfer.failed", e);
        }
    }

    /**
     * Handle transfer updated (description or metadata changes)
     */
    @Transactional
    public void handleTransferUpdated(Event event) {
        try {
            Transfer transfer = (Transfer) event.getDataObjectDeserializer()
                .getObject().orElseThrow(() -> new RuntimeException("Failed to deserialize transfer"));

            log.info("Transfer updated: {} with description: {}", transfer.getId(), transfer.getDescription());
            
            // Usually no action needed for metadata updates, just log for audit trail
            createLedgerEntry(
                Ledger.LedgerType.TRANSFER,
                transfer.getId(),
                transfer.getDestination(),
                0L, // No amount change for updates
                transfer.getCurrency(),
                Map.of("event_type", "transfer.updated", "transfer_id", transfer.getId())
            );
            
        } catch (Exception e) {
            log.error("Error handling transfer.updated", e);
        }
    }

    /**
     * Handle charge dispute creation
     */
    @Transactional
    public void handleChargeDisputeCreated(Event event) {
        try {
            Dispute dispute = (Dispute) event.getDataObjectDeserializer()
                .getObject().orElseThrow(() -> new RuntimeException("Failed to deserialize dispute"));

            log.warn("Charge dispute created: {} for amount: {}", dispute.getId(), dispute.getAmount());
            
            // Create ledger entry for dispute
            createLedgerEntry(
                Ledger.LedgerType.DISPUTE,
                dispute.getId(),
                null,
                dispute.getAmount(),
                dispute.getCurrency(),
                Map.of("event_type", "charge.dispute.created", "reason", dispute.getReason())
            );
            
        } catch (Exception e) {
            log.error("Error handling charge.dispute.created", e);
        }
    }

    /**
     * Handle account updates (for connected accounts)
     */
    @Transactional
    public void handleAccountUpdated(Event event) {
        try {
            Account account = (Account) event.getDataObjectDeserializer()
                .getObject().orElseThrow(() -> new RuntimeException("Failed to deserialize account"));

            log.info("Account updated: {} with charges enabled: {} and payouts enabled: {}", 
                account.getId(), account.getChargesEnabled(), account.getPayoutsEnabled());
            
        } catch (Exception e) {
            log.error("Error handling account.updated", e);
        }
    }

    /**
     * Handle successful invoice payment
     */
    @Transactional
    public void handleInvoicePaymentSucceeded(Event event) {
        try {
            Invoice invoice = (Invoice) event.getDataObjectDeserializer()
                .getObject().orElseThrow(() -> new RuntimeException("Failed to deserialize invoice"));

            log.info("Invoice payment succeeded: {} for amount: {}", invoice.getId(), invoice.getAmountPaid());
            
        } catch (Exception e) {
            log.error("Error handling invoice.payment_succeeded", e);
        }
    }

    /**
     * Handle failed invoice payment
     */
    @Transactional
    public void handleInvoicePaymentFailed(Event event) {
        try {
            Invoice invoice = (Invoice) event.getDataObjectDeserializer()
                .getObject().orElseThrow(() -> new RuntimeException("Failed to deserialize invoice"));

            log.warn("Invoice payment failed: {} for amount: {}", invoice.getId(), invoice.getAmountDue());
            
        } catch (Exception e) {
            log.error("Error handling invoice.payment_failed", e);
        }
    }

    /**
     * Create a ledger entry
     */
    private void createLedgerEntry(Ledger.LedgerType type, String sourceRef, String destRef, 
                                  Long amountCents, String currency, Map<String, String> meta) {
        try {
            Ledger ledger = new Ledger();
            ledger.setId(UUID.randomUUID().toString());
            ledger.setType(type);
            ledger.setSourceRef(sourceRef);
            ledger.setDestRef(destRef);
            ledger.setAmountCents(amountCents);
            ledger.setCurrency(currency);
            ledger.setMeta(meta);
            ledger.setCreatedAt(LocalDateTime.now());
            
            ledgerRepository.save(ledger);
            log.debug("Created ledger entry: {} for amount: {}", type, amountCents);
            
        } catch (Exception e) {
            log.error("Error creating ledger entry", e);
        }
    }
}
