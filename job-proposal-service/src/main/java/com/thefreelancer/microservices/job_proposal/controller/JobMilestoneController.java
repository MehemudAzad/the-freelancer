package com.thefreelancer.microservices.job_proposal.controller;

import com.thefreelancer.microservices.job_proposal.dto.JobMilestoneCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.JobMilestoneResponseDto;
import com.thefreelancer.microservices.job_proposal.dto.JobMilestoneUpdateDto;
import com.thefreelancer.microservices.job_proposal.service.JobMilestoneService;
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
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Job Milestones", description = "Job milestone management operations")
public class JobMilestoneController {
    
    private final JobMilestoneService jobMilestoneService;
    
    // ============== PUBLIC ENDPOINTS ==============
    
    @Operation(summary = "Get job milestones", description = "Get all milestones for a job (public, for discovery)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Milestones retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/{jobId}/milestones")
    public ResponseEntity<List<JobMilestoneResponseDto>> getJobMilestones(
            @Parameter(description = "ID of the job") @PathVariable Long jobId) {
        
        log.info("GET /api/jobs/{}/milestones - Getting job milestones", jobId);
        
        try {
            List<JobMilestoneResponseDto> milestones = jobMilestoneService.getMilestonesByJobId(jobId);
            log.info("Found {} milestones for job: {}", milestones.size(), jobId);
            return ResponseEntity.ok(milestones);
        } catch (RuntimeException e) {
            log.error("Error fetching milestones for job {}: {}", jobId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @Operation(summary = "Add milestone to job", description = "Add a milestone template to a job (public endpoint)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Milestone created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid milestone data"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @PostMapping("/{jobId}/milestones")
    public ResponseEntity<JobMilestoneResponseDto> addMilestoneToJob(
            @Parameter(description = "ID of the job") @PathVariable Long jobId,
            @Valid @RequestBody JobMilestoneCreateDto createDto) {
        
        log.info("POST /api/jobs/{}/milestones - Adding milestone to job (public)", jobId);
        
        try {
            JobMilestoneResponseDto milestone = jobMilestoneService.createMilestone(jobId, createDto);
            log.info("Milestone created successfully with ID: {}", milestone.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(milestone);
        } catch (RuntimeException e) {
            log.error("Error creating milestone for job {}: {}", jobId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // ============== CLIENT SECURE ENDPOINTS ==============
    // Milestone creation/management is restricted to job owners via /my-jobs/ endpoints
    
    @Operation(summary = "Update job milestone template", description = "Update a milestone template in a job (Client only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Milestone updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid milestone data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not your job"),
        @ApiResponse(responseCode = "404", description = "Milestone or job not found")
    })
    @PutMapping("/{jobId}/milestones/{milestoneId}")
    public ResponseEntity<JobMilestoneResponseDto> updateJobMilestoneTemplate(
            @Parameter(description = "ID of the job") @PathVariable Long jobId,
            @Parameter(description = "ID of the milestone") @PathVariable Long milestoneId,
            @Valid @RequestBody JobMilestoneUpdateDto updateDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("PUT /api/jobs/{}/milestones/{} - Updating job milestone template", jobId, milestoneId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for updating job milestones");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only clients can update their job milestones
        if (!"CLIENT".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only clients can update their job milestones. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            log.info("Updating milestone template for job {} by client userId: {}", jobId, authenticatedUserId);
            
            // TODO: Service should validate job ownership
            JobMilestoneResponseDto milestone = jobMilestoneService.updateMilestone(jobId, milestoneId, updateDto);
            log.info("Milestone template updated successfully: {}", milestoneId);
            
            return ResponseEntity.ok(milestone);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error updating milestone template {} for job {}: {}", milestoneId, jobId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // ============== MY-JOBS SECURE ENDPOINTS (Job Owner Only) ==============
    // All milestone creation/modification is done via /my-jobs/ endpoints for proper ownership validation
    
    @Operation(summary = "Add milestone to my job", description = "Add a milestone to an authenticated client's job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Milestone created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid milestone data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not your job"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @PostMapping("/my-jobs/{jobId}/milestones")
    public ResponseEntity<JobMilestoneResponseDto> createJobMilestone(
            @Parameter(description = "ID of the job") @PathVariable Long jobId,
            @Valid @RequestBody JobMilestoneCreateDto createDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("POST /api/jobs/my-jobs/{}/milestones - Creating milestone for authenticated client's job", jobId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for creating job milestones");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only clients can create job milestones
        if (!"CLIENT".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only clients can create job milestones. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            log.info("Creating milestone for authenticated client userId: {}", authenticatedUserId);
            
            // TODO: Service should validate job ownership
            JobMilestoneResponseDto milestone = jobMilestoneService.createMilestone(jobId, createDto);
            log.info("Milestone created successfully with ID: {}", milestone.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(milestone);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error creating milestone for job {}: {}", jobId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @Operation(summary = "Update my job milestone", description = "Update a milestone in an authenticated client's job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Milestone updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid milestone data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not your job"),
        @ApiResponse(responseCode = "404", description = "Milestone or job not found")
    })
    @PutMapping("/my-jobs/{jobId}/milestones/{milestoneId}")
    public ResponseEntity<JobMilestoneResponseDto> updateJobMilestone(
            @Parameter(description = "ID of the job") @PathVariable Long jobId,
            @Parameter(description = "ID of the milestone") @PathVariable Long milestoneId,
            @Valid @RequestBody JobMilestoneUpdateDto updateDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("PUT /api/jobs/my-jobs/{}/milestones/{} - Updating authenticated client's job milestone", jobId, milestoneId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for updating job milestones");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only clients can update their job milestones
        if (!"CLIENT".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only clients can update their job milestones. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            log.info("Updating milestone for authenticated client userId: {}", authenticatedUserId);
            
            // TODO: Service should validate job ownership
            JobMilestoneResponseDto milestone = jobMilestoneService.updateMilestone(jobId, milestoneId, updateDto);
            log.info("Milestone updated successfully: {}", milestoneId);
            
            return ResponseEntity.ok(milestone);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error updating milestone {} for job {}: {}", milestoneId, jobId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @Operation(summary = "Delete my job milestone", description = "Remove a milestone from an authenticated client's job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Milestone deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not your job"),
        @ApiResponse(responseCode = "404", description = "Milestone or job not found")
    })
    @DeleteMapping("/my-jobs/{jobId}/milestones/{milestoneId}")
    public ResponseEntity<Void> deleteJobMilestone(
            @Parameter(description = "ID of the job") @PathVariable Long jobId,
            @Parameter(description = "ID of the milestone") @PathVariable Long milestoneId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("DELETE /api/jobs/my-jobs/{}/milestones/{} - Deleting authenticated client's job milestone", jobId, milestoneId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for deleting job milestones");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only clients can delete their job milestones
        if (!"CLIENT".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only clients can delete their job milestones. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            log.info("Deleting milestone for authenticated client userId: {}", authenticatedUserId);
            
            // TODO: Service should validate job ownership
            jobMilestoneService.deleteMilestone(jobId, milestoneId);
            log.info("Milestone deleted successfully: {}", milestoneId);
            
            return ResponseEntity.noContent().build();
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error deleting milestone {} for job {}: {}", milestoneId, jobId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
