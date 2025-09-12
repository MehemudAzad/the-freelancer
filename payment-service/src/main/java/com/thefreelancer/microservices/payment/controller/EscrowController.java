package com.thefreelancer.microservices.payment.controller;

import com.thefreelancer.microservices.payment.dto.EscrowCreateDto;
import com.thefreelancer.microservices.payment.dto.EscrowResponseDto;
import com.thefreelancer.microservices.payment.dto.RefundCreateDto;
import com.thefreelancer.microservices.payment.model.Escrow;
import com.thefreelancer.microservices.payment.service.EscrowService;
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

import java.util.List;

@RestController
@RequestMapping("/api/payments/escrow")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Escrow Management", description = "APIs for managing escrow funds, releases, and refunds")
public class EscrowController {
    
    private final EscrowService escrowService;
    
    /**
     * Create escrow (fund milestone)
     */
    @Operation(
        summary = "Create escrow for milestone funding",
        description = "Creates an escrow account and charges the client's payment method to hold funds for a milestone"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Escrow created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/fund")
    public ResponseEntity<EscrowResponseDto> createEscrow(@Valid @RequestBody EscrowCreateDto createDto) {
        log.info("Creating escrow for milestone: {}", createDto.getMilestoneId());
        
        try {
            EscrowResponseDto response = escrowService.createEscrow(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid escrow creation request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating escrow", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Release escrow to freelancer (when milestone is accepted)
     */
    @Operation(
        summary = "Release escrow funds",
        description = "Releases held escrow funds to the freelancer's connected Stripe account when milestone is accepted"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Escrow released successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or escrow not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{escrowId}/release")
    public ResponseEntity<Void> releaseEscrow(
            @Parameter(description = "Escrow ID to release") @PathVariable String escrowId,
            @Parameter(description = "Stripe connected account ID of the freelancer") @RequestParam String destinationAccountId) {
        log.info("Releasing escrow: {} to account: {}", escrowId, destinationAccountId);
        
        try {
            // Find escrow by ID to get milestone ID
            EscrowResponseDto escrow = escrowService.getEscrowByMilestone(Long.parseLong(escrowId))
                .orElseThrow(() -> new IllegalArgumentException("Escrow not found"));
            
            escrowService.releaseEscrow(escrow.getMilestoneId(), destinationAccountId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid escrow release request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error releasing escrow", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Release escrow by milestone ID
     */
    @PostMapping("/milestone/{milestoneId}/release")
    public ResponseEntity<Void> releaseEscrowByMilestone(
            @PathVariable Long milestoneId,
            @RequestParam String destinationAccountId) {
        log.info("Releasing escrow for milestone: {} to account: {}", milestoneId, destinationAccountId);
        
        try {
            escrowService.releaseEscrow(milestoneId, destinationAccountId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid escrow release request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error releasing escrow", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Refund escrow
     */
    @PostMapping("/refund")
    public ResponseEntity<Void> refundEscrow(@Valid @RequestBody RefundCreateDto refundDto) {
        log.info("Refunding escrow: {}", refundDto.getEscrowId());
        
        try {
            escrowService.refundEscrow(refundDto);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid refund request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error refunding escrow", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get escrow by milestone ID
     */
    @GetMapping("/milestone/{milestoneId}")
    public ResponseEntity<EscrowResponseDto> getEscrowByMilestone(@PathVariable Long milestoneId) {
        log.info("Getting escrow for milestone: {}", milestoneId);
        
        return escrowService.getEscrowByMilestone(milestoneId)
            .map(escrow -> ResponseEntity.ok(escrow))
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get escrows by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EscrowResponseDto>> getEscrowsByStatus(@PathVariable String status) {
        log.info("Getting escrows with status: {}", status);
        
        try {
            Escrow.EscrowStatus escrowStatus = Escrow.EscrowStatus.valueOf(status.toUpperCase());
            List<EscrowResponseDto> escrows = escrowService.getEscrowsByStatus(escrowStatus);
            return ResponseEntity.ok(escrows);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status: {}", status);
            return ResponseEntity.badRequest().build();
        }
    }
}
