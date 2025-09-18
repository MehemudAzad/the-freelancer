package com.thefreelancer.microservices.gig.service;

import com.thefreelancer.microservices.gig.dto.JobRecommendationDto;
import com.thefreelancer.microservices.gig.dto.RecommendationRequestDto;
import com.thefreelancer.microservices.gig.dto.RecommendationResponseDto;
import com.thefreelancer.microservices.gig.model.Profile;
import com.thefreelancer.microservices.gig.repository.ProfileRepository;
import com.thefreelancer.microservices.gig.client.JobProposalServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {
    
    private final ProfileRepository profileRepository;
    private final JobProposalServiceClient jobProposalServiceClient;
    private final ProfileEmbeddingService profileEmbeddingService;
    private final JobEmbeddingService jobEmbeddingService;
    
    public RecommendationResponseDto getRecommendations(Long userId, RecommendationRequestDto request) {
        log.info("Getting recommendations for user: {}", userId);
        
        // Get user profile
        Profile profile = profileRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
        
        // Check if profile has skills
        if (profile.getSkills() == null || profile.getSkills().length == 0) {
            return RecommendationResponseDto.builder()
                .userId(userId)
                .recommendations(Collections.emptyList())
                .totalRecommendations(0)
                .averageMatchScore(0.0)
                .message("Please update your skills in your profile to get personalized recommendations")
                .build();
        }
        
        // Ensure profile embedding exists
        ensureProfileEmbeddingExists(profile);
        
        // Create search query from profile
        String searchQuery = createSearchQueryFromProfile(profile);
        
        // Find similar jobs using Spring AI vector search
        List<Document> similarJobs = jobEmbeddingService.findSimilarJobs(searchQuery, request.getLimit(), 0.7);
        
        if (similarJobs.isEmpty()) {
            return RecommendationResponseDto.builder()
                .userId(userId)
                .recommendations(Collections.emptyList())
                .totalRecommendations(0)
                .averageMatchScore(0.0)
                .message("No jobs available for recommendations at the moment")
                .build();
        }
        
        // Convert documents to recommendations
        List<JobRecommendationDto> recommendations = similarJobs.stream()
            .map(this::convertDocumentToRecommendation)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        // Calculate average match score
        double averageScore = recommendations.stream()
            .mapToDouble(JobRecommendationDto::getMatchScore)
            .average()
            .orElse(0.0);
        
        return RecommendationResponseDto.builder()
            .userId(userId)
            .recommendations(recommendations)
            .totalRecommendations(recommendations.size())
            .averageMatchScore(averageScore)
            .message("Recommendations based on your skills and profile")
            .build();
    }
    
    private void ensureProfileEmbeddingExists(Profile profile) {
        // Spring AI will create embedding if it doesn't exist
        try {
            List<String> skills = profile.getSkills() != null ? List.of(profile.getSkills()) : List.of();
            profileEmbeddingService.storeProfileEmbedding(
                profile.getUserId(),
                profile.getHeadline(),
                profile.getBio(),
                skills
            );
        } catch (Exception e) {
            log.warn("Failed to ensure profile embedding exists for user {}: {}", profile.getUserId(), e.getMessage());
        }
    }
    
    private String createSearchQueryFromProfile(Profile profile) {
        StringBuilder query = new StringBuilder();
        
        if (profile.getHeadline() != null) {
            query.append(profile.getHeadline()).append(" ");
        }
        
        if (profile.getSkills() != null && profile.getSkills().length > 0) {
            query.append("Skills: ").append(String.join(", ", profile.getSkills()));
        }
        
        return query.toString().trim();
    }
    
    private JobRecommendationDto convertDocumentToRecommendation(Document document) {
        try {
            Map<String, Object> metadata = document.getMetadata();
            
            if (!"job".equals(metadata.get("documentType"))) {
                return null; // Skip non-job documents
            }
            
            Long jobId = Long.parseLong(metadata.get("jobId").toString());
            String projectName = (String) metadata.get("projectName");
            String description = (String) metadata.get("description");
            String skills = (String) metadata.get("skills");
            
            // Calculate match score based on similarity (Spring AI provides this)
            double matchScore = document.getMetadata().containsKey("distance") 
                ? 1.0 - (Double) document.getMetadata().get("distance") 
                : 0.8; // Default score
            
            return JobRecommendationDto.builder()
                .jobId(jobId)
                .projectName(projectName)
                .description(description)
                .skills(skills != null ? Arrays.asList(skills.split(",")) : Arrays.asList())
                .matchScore(matchScore)
                .matchReason("Skills alignment and profile compatibility")
                .build();
        } catch (Exception e) {
            log.error("Error converting document to recommendation", e);
            return null;
        }
    }
}