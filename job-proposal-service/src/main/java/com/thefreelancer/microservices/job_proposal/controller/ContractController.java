package com.thefreelancer.microservices.job_proposal.controller;

import com.thefreelancer.microservices.job_proposal.dto.*;
import com.thefreelancer.microservices.job_proposal.service.ContractService;
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
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contract Management", description = "APIs for managing contracts between clients and freelancers")
@SecurityRequirement(name = "Bearer Authentication")
public class ContractController {

    private final ContractService contractService;

    @Operation(summary = "Create contract from accepted proposal")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Contract created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or proposal not accepted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only job owner can create contract"),
        @ApiResponse(responseCode = "404", description = "Job or proposal not found")
    })
    @PostMapping
    public ResponseEntity<ContractResponseDto> createContract(
            @Valid @RequestBody ContractCreateDto createDto) {
        
        // log.info("Creating contract for user: {} with request: {}", userId, createDto);
        
        try {
            ContractResponseDto response = contractService.createContract(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Contract creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating contract", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get contract details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contract found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Access denied"),
        @ApiResponse(responseCode = "404", description = "Contract not found")
    })
    @GetMapping("/{contractId}")
    public ResponseEntity<ContractResponseDto> getContract(
            @Parameter(description = "Contract ID") @PathVariable Long contractId,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") String userId) {
        
        log.info("Getting contract: {} for user: {}", contractId, userId);
        
        try {
            ContractResponseDto response = contractService.getContract(contractId, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Contract access failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error getting contract", e);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get user's contracts", description = "Get all contracts where user is either client or freelancer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contracts retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-contracts")
    public ResponseEntity<List<ContractResponseDto>> getUserContracts(
            @Parameter(hidden = true) @RequestHeader("X-User-ID") String userId) {
        
        log.info("Getting contracts for user: {}", userId);
        
        try {
            Long userIdLong = Long.parseLong(userId);
            List<ContractResponseDto> response = contractService.getUserContracts(userIdLong);
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            log.warn("Invalid user ID format: {}", userId);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting user contracts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Update contract status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contract status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Access denied"),
        @ApiResponse(responseCode = "404", description = "Contract not found")
    })
    @PutMapping("/{contractId}/status")
    public ResponseEntity<ContractResponseDto> updateContractStatus(
            @Parameter(description = "Contract ID") @PathVariable Long contractId,
            @Valid @RequestBody ContractStatusUpdateDto updateDto,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") String userId) {
        
        log.info("Updating contract status: {} to {} for user: {}", contractId, updateDto.getStatus(), userId);
        
        try {
            ContractResponseDto response = contractService.updateContractStatus(contractId, updateDto, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Contract status update failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating contract status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
