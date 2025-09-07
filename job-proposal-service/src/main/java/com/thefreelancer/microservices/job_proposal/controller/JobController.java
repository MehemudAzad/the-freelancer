package com.thefreelancer.microservices.job_proposal.controller;

import com.thefreelancer.microservices.job_proposal.dto.JobCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.JobResponseDto;
import com.thefreelancer.microservices.job_proposal.dto.JobUpdateDto;
import com.thefreelancer.microservices.job_proposal.service.JobService;
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
public class JobController {
    
    private final JobService jobService;
    
    @PostMapping
    public ResponseEntity<JobResponseDto> createJob(@Valid @RequestBody JobCreateDto jobCreateDto) {
        log.info("POST /api/jobs - Creating job for clientId: {}", jobCreateDto.getClientId());
        
        try {
            JobResponseDto job = jobService.createJob(jobCreateDto);
            log.info("Job successfully created with ID: {} for clientId: {}", job.getId(), job.getClientId());
            return ResponseEntity.status(HttpStatus.CREATED).body(job);
        } catch (RuntimeException e) {
            log.warn("Failed to create job for clientId {}: {}", jobCreateDto.getClientId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponseDto> getJob(@PathVariable Long jobId) {
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
    
    @PutMapping("/{jobId}")
    public ResponseEntity<JobResponseDto> updateJob(
            @PathVariable Long jobId,
            @Valid @RequestBody JobUpdateDto jobUpdateDto) {
        
        log.info("PUT /api/jobs/{} - Updating job", jobId);
        
        try {
            Optional<JobResponseDto> updatedJob = jobService.updateJob(jobId, jobUpdateDto);
            
            if (updatedJob.isPresent()) {
                log.info("Job successfully updated with ID: {}", jobId);
                return ResponseEntity.ok(updatedJob.get());
            } else {
                log.warn("Job not found with ID: {}", jobId);
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            log.warn("Failed to update job {}: {}", jobId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long jobId) {
        log.info("DELETE /api/jobs/{} - Deleting job", jobId);
        
        try {
            boolean deleted = jobService.deleteJob(jobId);
            
            if (deleted) {
                log.info("Job successfully deleted/cancelled with ID: {}", jobId);
                return ResponseEntity.noContent().build();
            } else {
                log.warn("Job not found with ID: {}", jobId);
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            log.warn("Failed to delete job {}: {}", jobId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<JobResponseDto>> getClientJobs(@PathVariable Long clientId) {
        log.info("GET /api/jobs/client/{} - Fetching jobs for client", clientId);
        
        List<JobResponseDto> jobs = jobService.getJobsByClientId(clientId);
        return ResponseEntity.ok(jobs);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<JobResponseDto>> searchJobs(
            @RequestParam(required = false) List<String> stack,
            @RequestParam(required = false) BigInteger minBudget,
            @RequestParam(required = false) BigInteger maxBudget,
            @RequestParam(required = false) String status) {
        
        log.info("GET /api/jobs/search - Searching jobs with stack: {}, minBudget: {}, maxBudget: {}, status: {}", 
                stack, minBudget, maxBudget, status);
        
        try {
            List<JobResponseDto> jobs = jobService.searchJobs(stack, minBudget, maxBudget, status);
            return ResponseEntity.ok(jobs);
        } catch (RuntimeException e) {
            log.warn("Failed to search jobs: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
