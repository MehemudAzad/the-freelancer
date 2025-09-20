package com.thefreelancer.microservices.job_proposal.controller;

import com.thefreelancer.microservices.job_proposal.dto.JobCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.JobResponseDto;
import com.thefreelancer.microservices.job_proposal.dto.JobUpdateDto;
import com.thefreelancer.microservices.job_proposal.service.JobService;
import com.thefreelancer.microservices.job_proposal.model.Job;
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

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Jobs", description = "Job posting and management operations")
public class JobController {
    
    private final JobService jobService;
    
    // ====================
    // PUBLIC DISCOVERY APIs (No authentication required)
    // ====================
    
    @Operation(summary = "Get job by ID", description = "Fetch a specific job by its ID (public endpoint)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job found"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponseDto> getJob(
            @Parameter(description = "ID of the job to retrieve") @PathVariable Long jobId) {
        log.info("GET /api/jobs/{} - Fetching job", jobId);
        
        Optional<JobResponseDto> job = jobService.getJobById(jobId);
        
        if (job.isPresent()) {
            log.info("Job found with ID: {}", jobId);
            return ResponseEntity.ok(job.get());
        } else {
            log.warn("Job not found with ID: {}", jobId);
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Search jobs", description = "Search and filter jobs by various criteria (public endpoint)")
    @GetMapping("/search")
    public ResponseEntity<List<JobResponseDto>> searchJobs(
            @Parameter(description = "Category filter") @RequestParam(required = false) String category,
            @Parameter(description = "Minimum budget filter") @RequestParam(required = false) BigInteger minBudget,
            @Parameter(description = "Maximum budget filter") @RequestParam(required = false) BigInteger maxBudget,
            @Parameter(description = "Is urgent filter") @RequestParam(required = false) Boolean isUrgent,
            @Parameter(description = "Budget type filter (FIXED|HOURLY)") @RequestParam(required = false) String budgetType) {

        log.info("GET /api/jobs/search - Searching jobs with category: {}, minBudget: {}, maxBudget: {}, isUrgent: {}, budgetType: {}", 
                category, minBudget, maxBudget, isUrgent, budgetType);

        try {
            Job.BudgetType bt = null;
            if (budgetType != null) {
                bt = Job.BudgetType.valueOf(budgetType.toUpperCase());
            }

            List<JobResponseDto> jobs = jobService.searchJobs(category, minBudget, maxBudget, isUrgent, bt);
            return ResponseEntity.ok(jobs);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid budget type provided: {}", budgetType);
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.warn("Failed to search jobs: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "Get jobs by user", description = "Get all public jobs posted by a specific user (for portfolio view)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<JobResponseDto>> getUserJobs(
            @Parameter(description = "ID of the user whose jobs to retrieve") @PathVariable Long userId) {
        log.info("GET /api/jobs/user/{} - Fetching jobs for user", userId);
        
        List<JobResponseDto> jobs = jobService.getJobsByClientId(userId);
        return ResponseEntity.ok(jobs);
    }
    
    // ====================
    // CLIENT ACTION APIs (Secure - CLIENT role required)
    // ====================
    
    @Operation(summary = "Create new job", description = "Post a new job (CLIENT role required)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Job created successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Only CLIENTs can create jobs"),
        @ApiResponse(responseCode = "400", description = "Invalid job data")
    })
    @PostMapping
    public ResponseEntity<JobResponseDto> createJob(
            @Valid @RequestBody JobCreateDto jobCreateDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("POST /api/jobs - Creating job");
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for creating jobs");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only clients can create jobs
        if (!"CLIENT".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only clients can create jobs. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            log.info("Creating job for authenticated client userId: {}", authenticatedUserId);
            
            // Pass the clientId from authentication to the service
            JobResponseDto job = jobService.createJob(jobCreateDto, authenticatedUserId);
            log.info("Job successfully created with ID: {} for clientId: {}", job.getId(), job.getClientId());
            return ResponseEntity.status(HttpStatus.CREATED).body(job);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.warn("Failed to create job: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "Get my posted jobs", description = "Get all jobs posted by the authenticated client")
    @GetMapping("/my-jobs")
    public ResponseEntity<List<JobResponseDto>> getMyJobs(
            @Parameter(description = "Optional status filter") @RequestParam(required = false) String status,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/jobs/my-jobs - Fetching authenticated client's jobs with status: {}", status);
        
        if (userIdHeader == null) {
            log.warn("Authentication required for my-jobs endpoint");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            List<JobResponseDto> myJobs = jobService.getJobsByClientId(authenticatedUserId);
            log.info("Found {} jobs for authenticated client: {}", myJobs.size(), authenticatedUserId);
            return ResponseEntity.ok(myJobs);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error fetching my jobs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @Operation(summary = "Update my job", description = "Update a job owned by the authenticated client")
    @PutMapping("/my-jobs/{jobId}")
    public ResponseEntity<JobResponseDto> updateMyJob(
            @Parameter(description = "ID of the job to update") @PathVariable Long jobId,
            @Valid @RequestBody JobUpdateDto jobUpdateDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("PUT /api/jobs/my-jobs/{} - Updating authenticated client's job", jobId);
        
        if (userIdHeader == null) {
            log.warn("Authentication required for updating jobs");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            
            // TODO: Add ownership validation in service layer
            // For now, using existing updateJob method with TODO comment
            Optional<JobResponseDto> updatedJob = jobService.updateJob(jobId, jobUpdateDto);
            
            if (updatedJob.isPresent()) {
                log.info("Job successfully updated with ID: {} by user: {}", jobId, authenticatedUserId);
                return ResponseEntity.ok(updatedJob.get());
            } else {
                log.warn("Job not found with ID: {} for user: {}", jobId, authenticatedUserId);
                return ResponseEntity.notFound().build();
            }
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.warn("Failed to update job {}: {}", jobId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "Delete my job", description = "Delete/cancel a job owned by the authenticated client")
    @DeleteMapping("/my-jobs/{jobId}")
    public ResponseEntity<Void> deleteMyJob(
            @Parameter(description = "ID of the job to delete") @PathVariable Long jobId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("DELETE /api/jobs/my-jobs/{} - Deleting authenticated client's job", jobId);
        
        if (userIdHeader == null) {
            log.warn("Authentication required for deleting jobs");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            
            // TODO: Add ownership validation in service layer
            // For now, using existing deleteJob method with TODO comment
            boolean deleted = jobService.deleteJob(jobId);
            
            if (deleted) {
                log.info("Job successfully deleted/cancelled with ID: {} by user: {}", jobId, authenticatedUserId);
                return ResponseEntity.noContent().build();
            } else {
                log.warn("Job not found with ID: {} for user: {}", jobId, authenticatedUserId);
                return ResponseEntity.notFound().build();
            }
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.warn("Failed to delete job {}: {}", jobId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
