package com.thefreelancer.microservices.job_proposal.controller;

import com.thefreelancer.microservices.job_proposal.dto.ProposalMilestoneCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.ProposalMilestoneResponseDto;
import com.thefreelancer.microservices.job_proposal.dto.ProposalMilestoneUpdateDto;
import com.thefreelancer.microservices.job_proposal.service.ProposalMilestoneService;
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
@RequestMapping("/api/proposals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Proposal Milestones", description = "Proposal milestone management operations")
public class ProposalMilestoneController {
    
    private final ProposalMilestoneService proposalMilestoneService;
    
    @Operation(summary = "Add milestone to proposal", description = "Add a milestone to a proposal (Proposal owner only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Milestone created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid milestone data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not your proposal"),
        @ApiResponse(responseCode = "404", description = "Proposal not found")
    })
    @PostMapping("/{proposalId}/milestones")
    public ResponseEntity<ProposalMilestoneResponseDto> addMilestoneToProposal(
            @Parameter(description = "ID of the proposal to add milestone to") @PathVariable Long proposalId,
            @Valid @RequestBody ProposalMilestoneCreateDto milestoneCreateDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("POST /api/proposals/{}/milestones - Adding milestone to proposal", proposalId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for adding proposal milestones");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only freelancers can add milestones to their proposals
        if (!"FREELANCER".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only freelancers can add milestones to proposals. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            
            log.info("Adding milestone to proposal: {} by user: {}", proposalId, authenticatedUserId);
            
            // TODO: Service should validate proposal ownership
            ProposalMilestoneResponseDto milestone = proposalMilestoneService.createMilestone(proposalId, milestoneCreateDto);
            log.info("Milestone created successfully with ID: {}", milestone.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(milestone);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error adding milestone to proposal {}: {}", proposalId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(summary = "Get proposal milestones", description = "Get all milestones for a proposal (Public - no authentication required)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Milestones retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Proposal not found")
    })
    @GetMapping("/{proposalId}/milestones")
    public ResponseEntity<List<ProposalMilestoneResponseDto>> getProposalMilestones(
            @Parameter(description = "ID of the proposal to get milestones for") @PathVariable Long proposalId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/proposals/{}/milestones - Getting milestones for proposal (public access)", proposalId);
        
        try {
            // Public endpoint - no authentication required for viewing milestones
            List<ProposalMilestoneResponseDto> milestones = proposalMilestoneService.getMilestonesByProposalId(proposalId);
            log.info("Found {} milestones for proposal: {}", milestones.size(), proposalId);
            
            return ResponseEntity.ok(milestones);
        } catch (Exception e) {
            log.error("Error fetching milestones for proposal {}: {}", proposalId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Update proposal milestone", description = "Update a milestone in a proposal (Proposal owner only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Milestone updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid milestone data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not your proposal"),
        @ApiResponse(responseCode = "404", description = "Milestone not found")
    })
    @PutMapping("/{proposalId}/milestones/{milestoneId}")
    public ResponseEntity<ProposalMilestoneResponseDto> updateProposalMilestone(
            @Parameter(description = "ID of the proposal") @PathVariable Long proposalId,
            @Parameter(description = "ID of the milestone to update") @PathVariable Long milestoneId,
            @Valid @RequestBody ProposalMilestoneUpdateDto milestoneUpdateDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("PUT /api/proposals/{}/milestones/{} - Updating milestone", proposalId, milestoneId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for updating proposal milestones");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only freelancers can update milestones in their proposals
        if (!"FREELANCER".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only freelancers can update proposal milestones. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            log.info("Updating milestone: {} in proposal: {} by user: {}", milestoneId, proposalId, authenticatedUserId);
            
            // TODO: Service should validate proposal and milestone ownership
            ProposalMilestoneResponseDto milestone = proposalMilestoneService.updateMilestone(milestoneId, milestoneUpdateDto);
            log.info("Milestone updated successfully: {}", milestoneId);
            
            return ResponseEntity.ok(milestone);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error updating milestone {}: {}", milestoneId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(summary = "Delete proposal milestone", description = "Delete a milestone from a proposal (Proposal owner only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Milestone deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not your proposal"),
        @ApiResponse(responseCode = "404", description = "Milestone not found")
    })
    @DeleteMapping("/{proposalId}/milestones/{milestoneId}")
    public ResponseEntity<Void> deleteProposalMilestone(
            @Parameter(description = "ID of the proposal") @PathVariable Long proposalId,
            @Parameter(description = "ID of the milestone to delete") @PathVariable Long milestoneId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("DELETE /api/proposals/{}/milestones/{} - Deleting milestone", proposalId, milestoneId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for deleting proposal milestones");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only freelancers can delete milestones from their proposals
        if (!"FREELANCER".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only freelancers can delete proposal milestones. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            log.info("Deleting milestone: {} from proposal: {} by user: {}", milestoneId, proposalId, authenticatedUserId);
            
            // TODO: Service should validate proposal and milestone ownership
            proposalMilestoneService.deleteMilestone(milestoneId);
            log.info("Milestone deleted successfully: {}", milestoneId);
            
            return ResponseEntity.noContent().build();
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error deleting milestone {}: {}", milestoneId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
