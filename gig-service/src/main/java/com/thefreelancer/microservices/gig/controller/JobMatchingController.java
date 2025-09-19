package com.thefreelancer.microservices.gig.controller;

import com.thefreelancer.microservices.gig.dto.JobMatchingRequestDto;
import com.thefreelancer.microservices.gig.dto.JobMatchingResponseDto;
import com.thefreelancer.microservices.gig.service.JobToFreelancerMatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Job Matching", description = "APIs for finding suitable freelancers for jobs")
public class JobMatchingController {
    
    private final JobToFreelancerMatchingService jobToFreelancerMatchingService;
    
    @PostMapping("/match-freelancers")
    @Operation(
        summary = "Find matching freelancers for a job",
        description = "Uses semantic search and skill matching to find freelancers that best match the job requirements"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully found matching freelancers",
        content = @Content(schema = @Schema(implementation = JobMatchingResponseDto.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request parameters"
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error during matching process"
    )
    public ResponseEntity<JobMatchingResponseDto> findMatchingFreelancers(
            @Valid @RequestBody JobMatchingRequestDto request) {
        
        log.info("Received job matching request for jobId: {} with {} required skills", 
                request.getJobId(), 
                request.getRequiredSkills() != null ? request.getRequiredSkills().size() : 0);
        
        try {
            JobMatchingResponseDto response = jobToFreelancerMatchingService.findMatchingFreelancers(request);
            
            log.info("Successfully found {} matching freelancers for job: {}", 
                    response.getTotalMatches(), request.getJobId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error finding matching freelancers for job {}: {}", request.getJobId(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{jobId}/match-freelancers")
    @Operation(
        summary = "Find matching freelancers using GET with query parameters",
        description = "Alternative endpoint using query parameters for simpler job matching requests"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Successfully found matching freelancers"
    )
    public ResponseEntity<JobMatchingResponseDto> findMatchingFreelancersSimple(
            @Parameter(description = "Job ID to find freelancers for")
            @PathVariable String jobId,
            
            @Parameter(description = "Job title")
            @RequestParam(required = false) String title,
            
            @Parameter(description = "Job description")
            @RequestParam(required = false) String description,
            
            @Parameter(description = "Required skills (comma-separated)")
            @RequestParam(required = false) String requiredSkills,
            
            @Parameter(description = "Job category")
            @RequestParam(required = false) String category,
            
            @Parameter(description = "Maximum number of results", example = "20")
            @RequestParam(defaultValue = "20") Integer limit,
            
            @Parameter(description = "Minimum similarity score", example = "0.3")
            @RequestParam(defaultValue = "0.3") Double minSimilarityScore,
            
            @Parameter(description = "Minimum rating", example = "4.0")
            @RequestParam(required = false) Double minRating,
            
            @Parameter(description = "Maximum hourly rate in cents")
            @RequestParam(required = false) Long maxHourlyRateCents,
            
            @Parameter(description = "Required availability", example = "FULL_TIME")
            @RequestParam(required = false) String availability) {
        
        log.info("Received simple job matching request for jobId: {}", jobId);
        
        try {
            // Build request DTO from query parameters
            JobMatchingRequestDto request = JobMatchingRequestDto.builder()
                .jobId(Long.parseLong(jobId))
                .title(title)
                .description(description)
                .requiredSkills(requiredSkills != null ? List.of(requiredSkills.split(",")) : null)
                .category(category)
                .limit(limit)
                .minSimilarityScore(minSimilarityScore)
                .minRating(minRating)
                .maxHourlyRateCents(maxHourlyRateCents)
                .availability(availability)
                .build();
            
            JobMatchingResponseDto response = jobToFreelancerMatchingService.findMatchingFreelancers(request);
            
            log.info("Successfully found {} matching freelancers for job: {}", 
                    response.getTotalMatches(), jobId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error finding matching freelancers for job {}: {}", jobId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/{jobId}/bulk-match")
    @Operation(
        summary = "Find matching freelancers for multiple job variations",
        description = "Allows testing different matching criteria for the same job"
    )
    public ResponseEntity<List<JobMatchingResponseDto>> bulkMatchFreelancers(
            @PathVariable String jobId,
            @Valid @RequestBody List<JobMatchingRequestDto> requests) {
        
        log.info("Received bulk matching request for jobId: {} with {} variations", 
                jobId, requests.size());
        
        try {
            List<JobMatchingResponseDto> responses = requests.stream()
                .map(request -> {
                    // Ensure jobId is set
                    request.setJobId(Long.parseLong(jobId));
                    return jobToFreelancerMatchingService.findMatchingFreelancers(request);
                })
                .toList();
            
            log.info("Successfully processed {} matching requests for job: {}", responses.size(), jobId);
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            log.error("Error in bulk matching for job {}: {}", jobId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}