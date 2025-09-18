package com.thefreelancer.microservices.gig.service;

import com.thefreelancer.microservices.gig.model.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {
    
    private final ProfileEmbeddingService profileEmbeddingService;
    private final JobEmbeddingService jobEmbeddingService;
    
    public void generateAndStoreProfileEmbedding(Profile profile) {
        try {
            List<String> skills = profile.getSkills() != null ? List.of(profile.getSkills()) : List.of();
            profileEmbeddingService.storeProfileEmbedding(
                profile.getUserId(),
                profile.getHeadline(),
                profile.getBio(),
                skills
            );
            log.info("Successfully stored embedding for profile: {}", profile.getUserId());
        } catch (Exception e) {
            log.error("Failed to generate embedding for profile: {}", profile.getUserId(), e);
            throw new RuntimeException("Failed to generate profile embedding", e);
        }
    }
    
    public void generateJobEmbedding(Long jobId, String projectName, String description, List<String> skills) {
        try {
            jobEmbeddingService.storeJobEmbedding(
                jobId,
                projectName,
                description,
                skills,
                "fixed", // default budget type
                null, // budget min
                null  // budget max
            );
            log.info("Successfully stored embedding for job: {}", jobId);
        } catch (Exception e) {
            log.error("Failed to generate embedding for job: {}", jobId, e);
            throw new RuntimeException("Failed to generate job embedding", e);
        }
    }
    
    public List<Document> findSimilarProfiles(String query, int limit) {
        try {
            return profileEmbeddingService.findSimilarProfiles(query, limit, 0.7);
        } catch (Exception e) {
            log.error("Failed to search for similar profiles", e);
            throw new RuntimeException("Failed to search similar profiles", e);
        }
    }
    
    public List<Document> findSimilarJobs(String query, int limit) {
        try {
            return jobEmbeddingService.findSimilarJobs(query, limit, 0.7);
        } catch (Exception e) {
            log.error("Failed to search for similar jobs", e);
            throw new RuntimeException("Failed to search similar jobs", e);
        }
    }

    public void deleteJobEmbedding(Long jobId) {
        try {
            jobEmbeddingService.deleteJobEmbedding(jobId);
            log.info("Successfully deleted embedding for job: {}", jobId);
        } catch (Exception e) {
            log.error("Failed to delete embedding for job: {}", jobId, e);
            throw new RuntimeException("Failed to delete job embedding", e);
        }
    }
}