package com.thefreelancer.microservices.job_proposal.controller;

import com.thefreelancer.microservices.job_proposal.dto.ProposalCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.ProposalResponseDto;
import com.thefreelancer.microservices.job_proposal.dto.ProposalUpdateDto;
import com.thefreelancer.microservices.job_proposal.service.ProposalService;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/proposals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Proposals", description = "Proposal management operations")
public class ProposalController {
    
    private final ProposalService proposalService;
    
    // ============== FREELANCER SECURE ENDPOINTS ==============
    
    @Operation(summary = "Get my submitted proposals", description = "Get all proposals submitted by the authenticated freelancer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Proposals retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - FREELANCER role required")
    })
    @GetMapping("/my-proposals")
    public ResponseEntity<List<ProposalResponseDto>> getMyProposals(
            @RequestParam(required = false) String status,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/proposals/my-proposals - Getting authenticated freelancer's proposals with status: {}", status);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for my-proposals endpoint");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only freelancers can view their proposals
        if (!"FREELANCER".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only freelancers can view their proposals. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            List<ProposalResponseDto> myProposals = proposalService.getMyProposals(authenticatedUserId, status);
            log.info("Found {} proposals for authenticated freelancer: {}", myProposals.size(), authenticatedUserId);
            return ResponseEntity.ok(myProposals);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error fetching my proposals: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(summary = "Submit new proposal", description = "Submit a new proposal for a job (FREELANCER only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Proposal submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid proposal data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - FREELANCER role required")
    })
    @PostMapping("/my-proposals")
    public ResponseEntity<ProposalResponseDto> submitProposal(
            @Valid @RequestBody ProposalCreateDto proposalCreateDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("POST /api/proposals/my-proposals - Submitting new proposal for job: {}", proposalCreateDto.getJobId());
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for submitting proposals");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only freelancers can submit proposals
        if (!"FREELANCER".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only freelancers can submit proposals. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            
            // Set the freelancer ID from the authenticated user
            proposalCreateDto.setFreelancerId(authenticatedUserId);
            
            log.info("Submitting proposal for authenticated freelancer userId: {}", authenticatedUserId);
            
            ProposalResponseDto proposal = proposalService.createProposal(proposalCreateDto);
            log.info("Proposal submitted successfully with ID: {}", proposal.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(proposal);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error submitting proposal: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(summary = "Update my proposal", description = "Update a proposal submitted by the authenticated freelancer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Proposal updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid proposal data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not your proposal"),
        @ApiResponse(responseCode = "404", description = "Proposal not found")
    })
    @PutMapping("/my-proposals/{proposalId}")
    public ResponseEntity<ProposalResponseDto> updateMyProposal(
            @Parameter(description = "ID of the proposal to update") @PathVariable Long proposalId,
            @Valid @RequestBody ProposalUpdateDto proposalUpdateDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("PUT /api/proposals/my-proposals/{} - Updating authenticated freelancer's proposal", proposalId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for updating proposals");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only freelancers can update their proposals
        if (!"FREELANCER".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only freelancers can update proposals. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            log.info("Updating proposal for authenticated freelancer userId: {}", authenticatedUserId);
            
            // TODO: Service should validate proposal ownership
            ProposalResponseDto proposal = proposalService.updateProposal(proposalId, proposalUpdateDto);
            log.info("Proposal updated successfully: {}", proposalId);
            
            return ResponseEntity.ok(proposal);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error updating proposal {}: {}", proposalId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(summary = "Withdraw my proposal", description = "Withdraw (delete) a proposal submitted by the authenticated freelancer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Proposal withdrawn successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not your proposal"),
        @ApiResponse(responseCode = "404", description = "Proposal not found")
    })
    @DeleteMapping("/my-proposals/{proposalId}")
    public ResponseEntity<Void> withdrawMyProposal(
            @Parameter(description = "ID of the proposal to withdraw") @PathVariable Long proposalId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("DELETE /api/proposals/my-proposals/{} - Withdrawing authenticated freelancer's proposal", proposalId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for withdrawing proposals");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only freelancers can withdraw their proposals
        if (!"FREELANCER".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only freelancers can withdraw proposals. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            log.info("Withdrawing proposal for authenticated freelancer userId: {}", authenticatedUserId);
            
            // TODO: Service should validate proposal ownership
            proposalService.deleteProposal(proposalId);
            log.info("Proposal withdrawn successfully: {}", proposalId);
            
            return ResponseEntity.noContent().build();
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error withdrawing proposal {}: {}", proposalId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ============== CROSS-USER VIEW (Read-only with auth) ==============
    
    @Operation(summary = "View proposal details", description = "View proposal details (accessible by job owner and proposal owner)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Proposal found"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not authorized to view this proposal"),
        @ApiResponse(responseCode = "404", description = "Proposal not found")
    })
    @GetMapping("/{proposalId}")
    public ResponseEntity<ProposalResponseDto> getProposal(
            @Parameter(description = "ID of the proposal to retrieve") @PathVariable Long proposalId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/proposals/{} - Fetching proposal details", proposalId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for viewing proposal details");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            
            Optional<ProposalResponseDto> proposal = proposalService.getProposalById(proposalId);
            
            if (proposal.isPresent()) {
                // TODO: Service should validate that user is either:
                // 1. The freelancer who submitted the proposal, OR
                // 2. The client who owns the job the proposal was submitted for
                log.info("Proposal found with ID: {}", proposalId);
                return ResponseEntity.ok(proposal.get());
            } else {
                log.warn("Proposal not found with ID: {}", proposalId);
                return ResponseEntity.notFound().build();
            }
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error fetching proposal {}: {}", proposalId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @Operation(summary = "Get all proposals for a job", description = "Get all proposals submitted for a specific job (CLIENT access)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Proposals retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - CLIENT role required"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ProposalResponseDto>> getProposalsForJob(
            @Parameter(description = "ID of the job to get proposals for") @PathVariable Long jobId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/proposals/job/{} - Fetching all proposals for job", jobId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for viewing job proposals");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only clients can view proposals for their jobs
        if (!"CLIENT".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only clients can view proposals for jobs. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            
            // Get proposals for the job (service will validate job ownership)
            List<ProposalResponseDto> proposals = proposalService.getProposalsForJobByClient(jobId, authenticatedUserId);
            
            log.info("Found {} proposals for job: {} for client: {}", proposals.size(), jobId, authenticatedUserId);
            return ResponseEntity.ok(proposals);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error fetching proposals for job {}: {}", jobId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
