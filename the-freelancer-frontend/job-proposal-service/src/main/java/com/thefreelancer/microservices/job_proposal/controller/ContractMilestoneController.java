package com.thefreelancer.microservices.job_proposal.controller;

import com.thefreelancer.microservices.job_proposal.dto.*;
import com.thefreelancer.microservices.job_proposal.service.ContractMilestoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contract Milestone Management", description = "APIs for managing contract milestones")
@SecurityRequirement(name = "Bearer Authentication")
public class ContractMilestoneController {

    private final ContractMilestoneService contractMilestoneService;

    // DEPRECATED: Milestones are now automatically created from proposal milestones when contract is created
    // This endpoint is no longer needed as of the new contract creation flow
    /*
    @Operation(summary = "Add milestone to contract")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Milestone added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only contract client can add milestones"),
        @ApiResponse(responseCode = "404", description = "Contract not found")
    })
    @PostMapping("/contracts/{contractId}/milestones")
    public ResponseEntity<ContractMilestoneResponseDto> addMilestone(
            @Parameter(description = "Contract ID") @PathVariable Long contractId,
            @Valid @RequestBody ContractMilestoneCreateDto createDto,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") String userId) {
        
        log.info("Adding milestone to contract: {} for user: {}", contractId, userId);
        
        try {
            ContractMilestoneResponseDto response = contractMilestoneService.addMilestone(contractId, createDto, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Milestone creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error adding milestone", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    */

    @Operation(summary = "Get contract milestones")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Milestones retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Access denied"),
        @ApiResponse(responseCode = "404", description = "Contract not found")
    })
    @GetMapping("/contracts/{contractId}/milestones")
    public ResponseEntity<List<ContractMilestoneResponseDto>> getContractMilestones(
            @Parameter(description = "Contract ID") @PathVariable Long contractId,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") String userId) {
        
        log.info("Getting milestones for contract: {} for user: {}", contractId, userId);
        
        try {
            List<ContractMilestoneResponseDto> response = contractMilestoneService.getContractMilestones(contractId, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Milestone access failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error getting milestones", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Submit milestone deliverable", description = "Freelancer submits work for milestone review")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Milestone submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid milestone state for submission"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only assigned freelancer can submit"),
        @ApiResponse(responseCode = "404", description = "Milestone not found")
    })
    @PutMapping("/milestones/{milestoneId}/submit")
    public ResponseEntity<ContractMilestoneResponseDto> submitMilestone(
            @Parameter(description = "Milestone ID") @PathVariable Long milestoneId,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") String userId) {
        
        log.info("Submitting milestone: {} for user: {}", milestoneId, userId);
        
        try {
            ContractMilestoneResponseDto response = contractMilestoneService.submitMilestone(milestoneId, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Milestone submission failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error submitting milestone", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Accept milestone", description = "Client accepts submitted milestone and releases payment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Milestone accepted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid milestone state for acceptance"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only contract client can accept"),
        @ApiResponse(responseCode = "404", description = "Milestone not found")
    })
    @PutMapping("/milestones/{milestoneId}/accept")
    public ResponseEntity<ContractMilestoneResponseDto> acceptMilestone(
            @Parameter(description = "Milestone ID") @PathVariable Long milestoneId,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") String userId) {
        
        log.info("Accepting milestone: {} for user: {}", milestoneId, userId);
        
        try {
            ContractMilestoneResponseDto response = contractMilestoneService.acceptMilestone(milestoneId, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Milestone acceptance failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error accepting milestone", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Reject milestone", description = "Client rejects submitted milestone with feedback")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Milestone rejected successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid milestone state for rejection"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only contract client can reject"),
        @ApiResponse(responseCode = "404", description = "Milestone not found")
    })
    @PutMapping("/milestones/{milestoneId}/reject")
    public ResponseEntity<ContractMilestoneResponseDto> rejectMilestone(
            @Parameter(description = "Milestone ID") @PathVariable Long milestoneId,
            @Valid @RequestBody ContractMilestoneUpdateDto updateDto,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") String userId) {
        
        log.info("Rejecting milestone: {} for user: {}", milestoneId, userId);
        
        try {
            ContractMilestoneResponseDto response = contractMilestoneService.rejectMilestone(milestoneId, updateDto, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Milestone rejection failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error rejecting milestone", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Update milestone status", description = "Generic milestone status update")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Milestone status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Access denied"),
        @ApiResponse(responseCode = "404", description = "Milestone not found")
    })
    @PutMapping("/milestones/{milestoneId}/status")
    public ResponseEntity<ContractMilestoneResponseDto> updateMilestoneStatus(
            @Parameter(description = "Milestone ID") @PathVariable Long milestoneId,
            @Valid @RequestBody ContractMilestoneUpdateDto updateDto,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") String userId) {
        
        log.info("Updating milestone status: {} to {} for user: {}", milestoneId, updateDto.getStatus(), userId);
        
        try {
            ContractMilestoneResponseDto response = contractMilestoneService.updateMilestoneStatus(milestoneId, updateDto, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Milestone status update failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating milestone status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
