package com.thefreelancer.microservices.job_proposal.controller;

import com.thefreelancer.microservices.job_proposal.dto.InviteCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.InviteResponseDto;
import com.thefreelancer.microservices.job_proposal.dto.InviteUpdateDto;
import com.thefreelancer.microservices.job_proposal.service.InviteService;
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
@RequestMapping("/api/invites")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invites", description = "Job invitation management operations")
public class InviteController {
    
    private final InviteService inviteService;
    
    // ============== CLIENT SECURE ENDPOINTS ==============
    
    @Operation(summary = "Create a job invitation", description = "Client invites a freelancer to apply for a job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Invitation created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - CLIENT role required"),
        @ApiResponse(responseCode = "404", description = "Job or freelancer not found"),
        @ApiResponse(responseCode = "409", description = "Invitation already exists")
    })
    @PostMapping
    public ResponseEntity<InviteResponseDto> createInvite(
            @Valid @RequestBody InviteCreateDto createDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("POST /api/invites - Creating invitation for job {} to freelancer {}", 
                createDto.getJobId(), createDto.getFreelancerId());
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Missing authentication headers");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only clients can create invites
        if (!"CLIENT".equals(userRole)) {
            log.warn("Access denied. User role: {} is not CLIENT", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Set client ID from authenticated user
        createDto.setClientId(Long.valueOf(userIdHeader));
        
        try {
            InviteResponseDto createdInvite = inviteService.createInvite(createDto);
            log.info("Successfully created invitation with id: {}", createdInvite.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInvite);
        } catch (IllegalArgumentException e) {
            log.warn("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "Get invitations sent by client", description = "Get all invitations sent by the authenticated client")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invitations retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - CLIENT role required")
    })
    @GetMapping("/my-sent")
    public ResponseEntity<List<InviteResponseDto>> getMySentInvites(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/invites/my-sent - Getting invitations sent by client");
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Missing authentication headers");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only clients can view their sent invites
        if (!"CLIENT".equals(userRole)) {
            log.warn("Access denied. User role: {} is not CLIENT", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<InviteResponseDto> invites = inviteService.getInvitesByClient(Long.valueOf(userIdHeader));
        log.info("Retrieved {} invitations for client {}", invites.size(), userIdHeader);
        return ResponseEntity.ok(invites);
    }
    
    // ============== FREELANCER SECURE ENDPOINTS ==============
    
    @Operation(summary = "Get invitations received by freelancer", description = "Get all invitations received by the authenticated freelancer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invitations retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - FREELANCER role required")
    })
    @GetMapping("/my-received")
    public ResponseEntity<List<InviteResponseDto>> getMyReceivedInvites(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/invites/my-received - Getting invitations received by freelancer");
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Missing authentication headers");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only freelancers can view their received invites
        if (!"FREELANCER".equals(userRole)) {
            log.warn("Access denied. User role: {} is not FREELANCER", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<InviteResponseDto> invites = inviteService.getInvitesForFreelancer(Long.valueOf(userIdHeader));
        log.info("Retrieved {} invitations for freelancer {}", invites.size(), userIdHeader);
        return ResponseEntity.ok(invites);
    }
    
    @Operation(summary = "Update invitation status", description = "Freelancer accepts or declines an invitation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invitation status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status or status transition"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - FREELANCER role required or not your invitation"),
        @ApiResponse(responseCode = "404", description = "Invitation not found")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<InviteResponseDto> updateInviteStatus(
            @PathVariable Long id,
            @Valid @RequestBody InviteUpdateDto updateDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("PUT /api/invites/{}/status - Updating invitation status to: {}", id, updateDto.getStatus());
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Missing authentication headers");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only freelancers can update invite status
        if (!"FREELANCER".equals(userRole)) {
            log.warn("Access denied. User role: {} is not FREELANCER", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            // First check if the invite belongs to this freelancer
            InviteResponseDto existingInvite = inviteService.getInviteById(id);
            if (!existingInvite.getFreelancerId().equals(Long.valueOf(userIdHeader))) {
                log.warn("Access denied. Invitation {} does not belong to freelancer {}", id, userIdHeader);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            InviteResponseDto updatedInvite = inviteService.updateInviteStatus(id, updateDto);
            log.info("Successfully updated invitation {} status to: {}", id, updateDto.getStatus());
            return ResponseEntity.ok(updatedInvite);
        } catch (IllegalArgumentException e) {
            log.warn("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    // ============== SHARED ENDPOINTS ==============
    
    @Operation(summary = "Get invitation by ID", description = "Get detailed information about a specific invitation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invitation retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not your invitation"),
        @ApiResponse(responseCode = "404", description = "Invitation not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<InviteResponseDto> getInviteById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/invites/{} - Getting invitation by ID", id);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Missing authentication headers");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            InviteResponseDto invite = inviteService.getInviteById(id);
            
            // Check if user has access to this invite
            Long userId = Long.valueOf(userIdHeader);
            boolean hasAccess = false;
            
            if ("CLIENT".equals(userRole) && invite.getClientId().equals(userId)) {
                hasAccess = true;
            } else if ("FREELANCER".equals(userRole) && invite.getFreelancerId().equals(userId)) {
                hasAccess = true;
            }
            
            if (!hasAccess) {
                log.warn("Access denied. User {} does not have access to invitation {}", userIdHeader, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            log.info("Successfully retrieved invitation {}", id);
            return ResponseEntity.ok(invite);
        } catch (Exception e) {
            log.warn("Error retrieving invitation {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Get invitations for a job", description = "Get all invitations for a specific job (job owner only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invitations retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not job owner"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<InviteResponseDto>> getInvitesByJob(
            @PathVariable Long jobId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/invites/job/{} - Getting invitations for job", jobId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Missing authentication headers");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Only clients can view invitations for jobs (typically the job owner)
        if (!"CLIENT".equals(userRole)) {
            log.warn("Access denied. User role: {} is not CLIENT", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<InviteResponseDto> invites = inviteService.getInvitesByJob(jobId);
        log.info("Retrieved {} invitations for job {}", invites.size(), jobId);
        return ResponseEntity.ok(invites);
    }
    
    // ============== DELETE ENDPOINT ==============
    
    @Operation(summary = "Delete an invitation", description = "Client can delete a sent invitation that hasn't been responded to")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Invitation deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete invitation - already responded to"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not your invitation"),
        @ApiResponse(responseCode = "404", description = "Invitation not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvite(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("DELETE /api/invites/{} - Deleting invitation", id);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Missing authentication headers");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Only clients can delete invitations they sent
        if (!"CLIENT".equals(userRole)) {
            log.warn("Access denied. User role: {} is not CLIENT", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            // First check if the invite belongs to this client
            InviteResponseDto existingInvite = inviteService.getInviteById(id);
            if (!existingInvite.getClientId().equals(Long.valueOf(userIdHeader))) {
                log.warn("Access denied. Invitation {} was not sent by client {}", id, userIdHeader);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            inviteService.deleteInvite(id);
            log.info("Successfully deleted invitation {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.warn("Error deleting invitation {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}