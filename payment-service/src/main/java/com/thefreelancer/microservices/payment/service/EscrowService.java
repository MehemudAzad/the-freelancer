package com.thefreelancer.microservices.payment.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Transfer;
import com.thefreelancer.microservices.payment.dto.EscrowCreateDto;
import com.thefreelancer.microservices.payment.dto.EscrowResponseDto;
import com.thefreelancer.microservices.payment.dto.RefundCreateDto;
import com.thefreelancer.microservices.payment.model.Escrow;
import com.thefreelancer.microservices.payment.model.Ledger;
import com.thefreelancer.microservices.payment.model.Payout;
import com.thefreelancer.microservices.payment.model.Refund;
import com.thefreelancer.microservices.payment.repository.EscrowRepository;
import com.thefreelancer.microservices.payment.repository.LedgerRepository;
import com.thefreelancer.microservices.payment.repository.PayoutRepository;
import com.thefreelancer.microservices.payment.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EscrowService {
    
    private final EscrowRepository escrowRepository;
    private final PayoutRepository payoutRepository;
    private final RefundRepository refundRepository;
    private final LedgerRepository ledgerRepository;
    private final StripeService stripeService;
    
    @Transactional
    public EscrowResponseDto createEscrow(EscrowCreateDto createDto) {
        log.info("Creating escrow for milestone: {}", createDto.getMilestoneId());
        
        // Check if escrow already exists for this milestone
        if (escrowRepository.existsByMilestoneId(createDto.getMilestoneId())) {
            throw new IllegalArgumentException("Escrow already exists for milestone: " + createDto.getMilestoneId());
        }
        
        try {
            // Create Payment Intent with Stripe
            PaymentIntent paymentIntent = stripeService.createEscrowPaymentIntent(
                createDto.getMilestoneId(),
                createDto.getAmountCents(),
                createDto.getCurrency(),
                createDto.getPaymentMethodId()
            );
            
            // Create escrow record
            Escrow escrow = Escrow.builder()
                .id(UUID.randomUUID().toString())
                .milestoneId(createDto.getMilestoneId())
                .paymentIntentId(paymentIntent.getId())
                .amountCents(createDto.getAmountCents())
                .currency(createDto.getCurrency())
                .status(Escrow.EscrowStatus.HELD)
                .build();
            
            Escrow savedEscrow = escrowRepository.save(escrow);
            
            // Record in ledger
            recordLedgerEntry(
                Ledger.LedgerType.CHARGE,
                null,
                paymentIntent.getId(),
                createDto.getAmountCents(),
                createDto.getCurrency(),
                "Escrow created for milestone: " + createDto.getMilestoneId()
            );
            
            log.info("Escrow created successfully: {}", savedEscrow.getId());
            
            return EscrowResponseDto.builder()
                .id(savedEscrow.getId())
                .milestoneId(savedEscrow.getMilestoneId())
                .paymentIntentId(savedEscrow.getPaymentIntentId())
                .amountCents(savedEscrow.getAmountCents())
                .currency(savedEscrow.getCurrency())
                .status(savedEscrow.getStatus().name())
                .createdAt(savedEscrow.getCreatedAt())
                .updatedAt(savedEscrow.getUpdatedAt())
                .clientSecret(paymentIntent.getClientSecret())
                .build();
                
        } catch (StripeException e) {
            log.error("Stripe error creating escrow: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create escrow: " + e.getMessage());
        }
    }
    
    @Transactional
    public void releaseEscrow(Long milestoneId, String destinationAccountId) {
        log.info("Releasing escrow for milestone: {} to account: {}", milestoneId, destinationAccountId);
        
        Escrow escrow = escrowRepository.findByMilestoneId(milestoneId)
            .orElseThrow(() -> new IllegalArgumentException("Escrow not found for milestone: " + milestoneId));
        
        if (escrow.getStatus() != Escrow.EscrowStatus.HELD) {
            throw new IllegalArgumentException("Escrow is not in HELD status: " + escrow.getStatus());
        }
        
        try {
            // Capture the payment intent first (if not already captured)
            stripeService.capturePaymentIntent(escrow.getPaymentIntentId());
            
            // Transfer to freelancer
            Transfer transfer = stripeService.releaseEscrowToFreelancer(
                destinationAccountId,
                escrow.getAmountCents(),
                escrow.getCurrency(),
                milestoneId
            );
            
            // Update escrow status
            escrow.setStatus(Escrow.EscrowStatus.RELEASED);
            escrowRepository.save(escrow);
            
            // Create payout record
            Long platformFee = stripeService.calculatePlatformFee(escrow.getAmountCents());
            Payout payout = Payout.builder()
                .id(UUID.randomUUID().toString())
                .milestoneId(milestoneId)
                .transferId(transfer.getId())
                .destinationAccountId(destinationAccountId)
                .amountCents(escrow.getAmountCents() - platformFee)
                .feeCents(platformFee)
                .currency(escrow.getCurrency())
                .status(Payout.PayoutStatus.INITIATED)
                .build();
            
            payoutRepository.save(payout);
            
            // Record in ledger
            recordLedgerEntry(
                Ledger.LedgerType.TRANSFER,
                escrow.getPaymentIntentId(),
                transfer.getId(),
                escrow.getAmountCents() - platformFee,
                escrow.getCurrency(),
                "Escrow released for milestone: " + milestoneId
            );
            
            // Record platform fee
            recordLedgerEntry(
                Ledger.LedgerType.FEE,
                escrow.getPaymentIntentId(),
                "platform",
                platformFee,
                escrow.getCurrency(),
                "Platform fee for milestone: " + milestoneId
            );
            
            log.info("Escrow released successfully for milestone: {}", milestoneId);
            
        } catch (StripeException e) {
            log.error("Stripe error releasing escrow: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to release escrow: " + e.getMessage());
        }
    }
    
    @Transactional
    public void refundEscrow(RefundCreateDto refundDto) {
        log.info("Refunding escrow: {}", refundDto.getEscrowId());
        
        Escrow escrow = escrowRepository.findById(refundDto.getEscrowId())
            .orElseThrow(() -> new IllegalArgumentException("Escrow not found: " + refundDto.getEscrowId()));
        
        if (escrow.getStatus() != Escrow.EscrowStatus.HELD) {
            throw new IllegalArgumentException("Cannot refund escrow that is not HELD: " + escrow.getStatus());
        }
        
        try {
            // Create refund with Stripe
            com.stripe.model.Refund stripeRefund = stripeService.createRefund(
                escrow.getPaymentIntentId(),
                refundDto.getAmountCents(),
                refundDto.getReason()
            );
            
            // Update escrow status
            escrow.setStatus(Escrow.EscrowStatus.REFUNDED);
            escrowRepository.save(escrow);
            
            // Create refund record
            Refund refund = Refund.builder()
                .id(UUID.randomUUID().toString())
                .escrow(escrow)
                .refundId(stripeRefund.getId())
                .amountCents(refundDto.getAmountCents())
                .currency(escrow.getCurrency())
                .reason(refundDto.getReason())
                .status(Refund.RefundStatus.INITIATED)
                .build();
            
            refundRepository.save(refund);
            
            // Record in ledger
            recordLedgerEntry(
                Ledger.LedgerType.REFUND,
                escrow.getPaymentIntentId(),
                stripeRefund.getId(),
                refundDto.getAmountCents(),
                escrow.getCurrency(),
                "Refund: " + (refundDto.getReason() != null ? refundDto.getReason() : "No reason provided")
            );
            
            log.info("Escrow refunded successfully: {}", refundDto.getEscrowId());
            
        } catch (StripeException e) {
            log.error("Stripe error refunding escrow: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to refund escrow: " + e.getMessage());
        }
    }
    
    public Optional<EscrowResponseDto> getEscrowByMilestone(Long milestoneId) {
        return escrowRepository.findByMilestoneId(milestoneId)
            .map(this::convertToDto);
    }
    
    public List<EscrowResponseDto> getEscrowsByStatus(Escrow.EscrowStatus status) {
        return escrowRepository.findByStatus(status)
            .stream()
            .map(this::convertToDto)
            .toList();
    }
    
    private EscrowResponseDto convertToDto(Escrow escrow) {
        return EscrowResponseDto.builder()
            .id(escrow.getId())
            .milestoneId(escrow.getMilestoneId())
            .paymentIntentId(escrow.getPaymentIntentId())
            .amountCents(escrow.getAmountCents())
            .currency(escrow.getCurrency())
            .status(escrow.getStatus().name())
            .createdAt(escrow.getCreatedAt())
            .updatedAt(escrow.getUpdatedAt())
            .build();
    }
    
    private void recordLedgerEntry(Ledger.LedgerType type, String sourceRef, String destRef, 
                                 Long amountCents, String currency, String description) {
        Ledger ledgerEntry = Ledger.builder()
            .id(UUID.randomUUID().toString())
            .type(type)
            .sourceRef(sourceRef)
            .destRef(destRef)
            .amountCents(amountCents)
            .currency(currency)
            .meta(java.util.Map.of("description", description))
            .build();
        
        ledgerRepository.save(ledgerEntry);
    }
}
