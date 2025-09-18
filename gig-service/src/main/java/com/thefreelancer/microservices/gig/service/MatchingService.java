package com.thefreelancer.microservices.gig.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for matching jobs with freelancers using AI embeddings
 * This service uses the ProfileEmbeddingService and JobEmbeddingService to find best matches
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {

    private final ProfileEmbeddingService profileEmbeddingService;
    private final JobEmbeddingService jobEmbeddingService;

    /**
     * Find the best matching freelancers for a job
     */
    public List<MatchResult> findMatchingFreelancersForJob(Long jobId, String jobDescription, 
                                                          List<String> requiredSkills, int topK) {
        try {
            // Create a query combining job description and skills
            String query = createJobQuery(jobDescription, requiredSkills);
            
            // Find similar profiles
            List<Document> matchingProfiles = profileEmbeddingService.findSimilarProfiles(query, topK, 0.7);
            
            // Convert to match results with scores
            return matchingProfiles.stream()
                .map(doc -> new MatchResult(
                    Long.parseLong(doc.getMetadata().get("userId").toString()),
                    doc.getMetadata().get("headline").toString(),
                    doc.getMetadata().get("skills").toString(),
                    calculateMatchScore(doc, requiredSkills),
                    "Job-Profile Similarity"
                ))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error finding matching freelancers for job {}: {}", jobId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Find the best matching jobs for a freelancer
     */
    public List<MatchResult> findMatchingJobsForFreelancer(Long userId, String headline, 
                                                          String bio, List<String> skills, int topK) {
        try {
            // Create a query combining freelancer's profile
            String query = createProfileQuery(headline, bio, skills);
            
            // Find similar jobs
            List<Document> matchingJobs = jobEmbeddingService.findSimilarJobs(query, topK, 0.7);
            
            // Convert to match results with scores
            return matchingJobs.stream()
                .map(doc -> new MatchResult(
                    Long.parseLong(doc.getMetadata().get("jobId").toString()),
                    doc.getMetadata().get("projectName").toString(),
                    doc.getMetadata().get("skills").toString(),
                    calculateJobMatchScore(doc, skills),
                    "Profile-Job Similarity"
                ))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error finding matching jobs for user {}: {}", userId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Find freelancers with specific skills
     */
    public List<MatchResult> findFreelancersBySkills(List<String> skills, int topK) {
        try {
            List<Document> matchingProfiles = profileEmbeddingService.findProfilesBySkills(skills, topK);
            
            return matchingProfiles.stream()
                .map(doc -> new MatchResult(
                    Long.parseLong(doc.getMetadata().get("userId").toString()),
                    doc.getMetadata().get("headline").toString(),
                    doc.getMetadata().get("skills").toString(),
                    calculateSkillMatchScore(doc.getMetadata().get("skills").toString(), skills),
                    "Skills Match"
                ))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error finding freelancers by skills {}: {}", skills, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Find jobs requiring specific skills
     */
    public List<MatchResult> findJobsBySkills(List<String> skills, int topK) {
        try {
            List<Document> matchingJobs = jobEmbeddingService.findJobsBySkills(skills, topK);
            
            return matchingJobs.stream()
                .map(doc -> new MatchResult(
                    Long.parseLong(doc.getMetadata().get("jobId").toString()),
                    doc.getMetadata().get("projectName").toString(),
                    doc.getMetadata().get("skills").toString(),
                    calculateSkillMatchScore(doc.getMetadata().get("skills").toString(), skills),
                    "Skills Match"
                ))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error finding jobs by skills {}: {}", skills, e.getMessage(), e);
            return List.of();
        }
    }

    private String createJobQuery(String description, List<String> skills) {
        StringBuilder query = new StringBuilder();
        
        if (description != null && !description.trim().isEmpty()) {
            query.append(description.trim()).append(" ");
        }
        
        if (skills != null && !skills.isEmpty()) {
            query.append("Required skills: ").append(String.join(", ", skills));
        }
        
        return query.toString();
    }

    private String createProfileQuery(String headline, String bio, List<String> skills) {
        StringBuilder query = new StringBuilder();
        
        if (headline != null && !headline.trim().isEmpty()) {
            query.append(headline.trim()).append(" ");
        }
        
        if (bio != null && !bio.trim().isEmpty()) {
            query.append(bio.trim()).append(" ");
        }
        
        if (skills != null && !skills.isEmpty()) {
            query.append("Skills: ").append(String.join(", ", skills));
        }
        
        return query.toString();
    }

    private double calculateMatchScore(Document doc, List<String> requiredSkills) {
        // This is a simple skill overlap calculation
        // In production, you might want to use more sophisticated scoring
        String profileSkills = doc.getMetadata().get("skills").toString().toLowerCase();
        
        long matchingSkills = requiredSkills.stream()
            .mapToLong(skill -> profileSkills.contains(skill.toLowerCase()) ? 1 : 0)
            .sum();
            
        return requiredSkills.isEmpty() ? 0.5 : (double) matchingSkills / requiredSkills.size();
    }

    private double calculateJobMatchScore(Document doc, List<String> freelancerSkills) {
        String jobSkills = doc.getMetadata().get("skills").toString().toLowerCase();
        
        long matchingSkills = freelancerSkills.stream()
            .mapToLong(skill -> jobSkills.contains(skill.toLowerCase()) ? 1 : 0)
            .sum();
            
        return freelancerSkills.isEmpty() ? 0.5 : (double) matchingSkills / freelancerSkills.size();
    }

    private double calculateSkillMatchScore(String documentSkills, List<String> searchSkills) {
        String docSkillsLower = documentSkills.toLowerCase();
        
        long matchingSkills = searchSkills.stream()
            .mapToLong(skill -> docSkillsLower.contains(skill.toLowerCase()) ? 1 : 0)
            .sum();
            
        return searchSkills.isEmpty() ? 0.0 : (double) matchingSkills / searchSkills.size();
    }

    /**
     * Result class for matching operations
     */
    public record MatchResult(
        Long id,
        String title,
        String skills,
        double matchScore,
        String matchType
    ) {}
}