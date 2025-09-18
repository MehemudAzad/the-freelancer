package com.thefreelancer.microservices.gig.controller;

import com.thefreelancer.microservices.gig.dto.JobDataForEmbeddingDto;
import com.thefreelancer.microservices.gig.service.EmbeddingService;
import com.thefreelancer.microservices.gig.service.ProfileEmbeddingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/internal/embeddings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Internal Embedding Generation", description = "Internal APIs for triggering embedding generation")
public class EmbeddingController {

    private final EmbeddingService embeddingService;
    private final ProfileEmbeddingService profileEmbeddingService;

    @PostMapping("/jobs")
    @Operation(summary = "Generate or update a job embedding", description = "Receives job data and triggers the generation of its vector embedding.")
    public ResponseEntity<Void> generateJobEmbedding(@RequestBody JobDataForEmbeddingDto jobData) {
        log.info("Received request to generate embedding for job ID: {}", jobData.getJobId());
        try {
            List<String> skillsList = jobData.getSkills() != null ? 
                Arrays.asList(jobData.getSkills()) : List.of();
            
            embeddingService.generateJobEmbedding(
                    jobData.getJobId(),
                    jobData.getProjectName(),
                    jobData.getDescription(),
                    skillsList
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to generate embedding for job ID: {}", jobData.getJobId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/profiles/{userId}")
    @Operation(summary = "Generate or update a profile embedding", description = "Triggers the generation of vector embedding for a user profile.")
    public ResponseEntity<Void> generateProfileEmbedding(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @RequestParam(required = false) String headline,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) List<String> skills) {
        log.info("Received request to generate embedding for profile ID: {}", userId);
        try {
            profileEmbeddingService.storeProfileEmbedding(
                    userId,
                    headline,
                    bio,
                    skills != null ? skills : List.of()
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to generate embedding for profile ID: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/jobs/{jobId}")
    @Operation(summary = "Delete job embedding", description = "Removes the vector embedding for a job.")
    public ResponseEntity<Void> deleteJobEmbedding(@Parameter(description = "Job ID") @PathVariable Long jobId) {
        log.info("Received request to delete embedding for job ID: {}", jobId);
        try {
            embeddingService.deleteJobEmbedding(jobId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete embedding for job ID: {}", jobId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/profiles/{userId}")
    @Operation(summary = "Delete profile embedding", description = "Removes the vector embedding for a user profile.")
    public ResponseEntity<Void> deleteProfileEmbedding(@Parameter(description = "User ID") @PathVariable Long userId) {
        log.info("Received request to delete embedding for profile ID: {}", userId);
        try {
            profileEmbeddingService.deleteProfileEmbedding(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete embedding for profile ID: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
