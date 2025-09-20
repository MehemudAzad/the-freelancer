package com.thefreelancer.microservices.gig.service;

import com.thefreelancer.microservices.gig.model.Gig;
import com.thefreelancer.microservices.gig.model.Profile;
import com.thefreelancer.microservices.gig.repository.GigRepository;
import com.thefreelancer.microservices.gig.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing profile embeddings using Spring AI VectorStore
 * Uses PGVector as the underlying vector database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileEmbeddingService {

    private final VectorStore vectorStore;
    private final ProfileRepository profileRepository;
    private final GigRepository gigRepository;
    
    private static final String PROFILE_DOCUMENT_TYPE = "profile";
    private static final String USER_ID_KEY = "userId";
    private static final String CONTENT_HASH_KEY = "contentHash";
    private static final String SKILLS_KEY = "skills";
    private static final String HEADLINE_KEY = "headline";
    private static final String BIO_KEY = "bio";
    private static final String DOCUMENT_TYPE_KEY = "documentType";
    
    /**
     * Store or update profile embedding (enhanced with gig data)
     */
    public void storeProfileEmbedding(Long userId, String headline, String bio, List<String> skills) {
        try {
            // Create enhanced profile summary including gig data
            String profileSummary = createEnhancedProfileSummary(userId, headline, bio, skills);
            String contentHash = generateContentHash(profileSummary);
            
            // Check if embedding already exists and is up to date
            Optional<Document> existingDoc = findExistingProfileDocument(userId);
            if (existingDoc.isPresent()) {
                String existingHash = existingDoc.get().getMetadata().get(CONTENT_HASH_KEY).toString();
                if (contentHash.equals(existingHash)) {
                    log.debug("Profile embedding for user {} is already up to date", userId);
                    return;
                }
                // Remove old embedding by finding documents to delete
                deleteProfileEmbedding(userId);
            }
            
            // Create document with metadata
            Document document = new Document(
                profileSummary,
                Map.of(
                    USER_ID_KEY, userId.toString(),
                    CONTENT_HASH_KEY, contentHash,
                    SKILLS_KEY, String.join(", ", skills),
                    HEADLINE_KEY, headline != null ? headline : "",
                    BIO_KEY, bio != null ? bio : "",
                    DOCUMENT_TYPE_KEY, PROFILE_DOCUMENT_TYPE
                )
            );
            
            // Store in vector database
            vectorStore.add(List.of(document));
            log.info("Stored enhanced profile embedding for user {} with gig data", userId);
            
        } catch (Exception e) {
            log.error("Error storing profile embedding for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to store profile embedding", e);
        }
    }
    
    /**
     * Find similar profiles based on skills and description
     */
    public List<Document> findSimilarProfiles(String query, int topK, double similarityThreshold) {
        
        // Validate query is not empty
        if (query == null || query.trim().isEmpty()) {
            log.warn("Empty query provided to findSimilarProfiles, using fallback");
            query = "software developer programmer";
        }
        
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        Filter.Expression filter = builder.eq(DOCUMENT_TYPE_KEY, PROFILE_DOCUMENT_TYPE).build();
        
        SearchRequest searchRequest = SearchRequest.builder()
            .query(query.trim())
            .topK(topK)
            .similarityThreshold(similarityThreshold)
            .filterExpression(filter)
            .build();
            
        return vectorStore.similaritySearch(searchRequest);
    }
    
    /**
     * Find profiles by specific skills
     */
    public List<Document> findProfilesBySkills(List<String> skills, int topK) {
        String skillsQuery = String.join(" ", skills);
        return findSimilarProfiles(skillsQuery, topK, 0.7);
    }
    
    /**
     * Update profile embedding when gig data changes
     */
    public void updateProfileEmbeddingOnGigChange(Long userId) {
        try {
            log.info("Updating profile embedding for user {} due to gig changes", userId);
            
            // Get current profile data
            Optional<Profile> profileOpt = profileRepository.findByUserId(userId);
            if (profileOpt.isEmpty()) {
                log.warn("Profile not found for user {} when updating embedding on gig change", userId);
                return;
            }
            
            Profile profile = profileOpt.get();
            List<String> skills = profile.getSkills() != null ? Arrays.asList(profile.getSkills()) : List.of();
            
            // Update the profile embedding with current profile + gig data
            storeProfileEmbedding(userId, profile.getHeadline(), profile.getBio(), skills);
            
        } catch (Exception e) {
            log.error("Error updating profile embedding on gig change for user {}: {}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * Delete profile embedding
     */
    public void deleteProfileEmbedding(Long userId) {
        try {
            // Find existing documents for this user
            List<Document> existingDocs = findAllProfileDocuments(userId);
            
            if (!existingDocs.isEmpty()) {
                // Extract document IDs for deletion
                List<String> documentIds = existingDocs.stream()
                    .map(Document::getId)
                    .toList();
                
                vectorStore.delete(documentIds);
                log.info("Deleted {} profile embedding documents for user {}", documentIds.size(), userId);
            } else {
                log.debug("No profile embeddings found for user {} to delete", userId);
            }
            
        } catch (Exception e) {
            log.error("Error deleting profile embedding for user {}: {}", userId, e.getMessage(), e);
        }
    }
    
    private Optional<Document> findExistingProfileDocument(Long userId) {
        List<Document> docs = findAllProfileDocuments(userId);
        return docs.isEmpty() ? Optional.empty() : Optional.of(docs.get(0));
    }
    
    private List<Document> findAllProfileDocuments(Long userId) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        Filter.Expression filter = builder.and(
            builder.eq(DOCUMENT_TYPE_KEY, PROFILE_DOCUMENT_TYPE),
            builder.eq(USER_ID_KEY, userId.toString())
        ).build();
        
        SearchRequest searchRequest = SearchRequest.builder()
            .query("*") // Match any content
            .topK(10) // Get all documents for this user
            .filterExpression(filter)
            .build();
            
        return vectorStore.similaritySearch(searchRequest);
    }
    
    /**
     * Create enhanced profile summary including comprehensive gig portfolio for semantic matching
     * Features: categorized organization, aggregated skills, performance indicators, concise descriptions
     */
    private String createEnhancedProfileSummary(Long userId, String headline, String bio, List<String> skills) {
        StringBuilder summary = new StringBuilder();
        
        // Add basic profile information
        if (headline != null && !headline.trim().isEmpty()) {
            summary.append("Headline: ").append(headline.trim()).append(". ");
        }
        
        if (bio != null && !bio.trim().isEmpty()) {
            summary.append("Bio: ").append(bio.trim()).append(". ");
        }
        
        if (skills != null && !skills.isEmpty()) {
            summary.append("Skills: ").append(String.join(", ", skills)).append(". ");
        }
        
        // Add comprehensive gig portfolio for enhanced matching
        try {
            List<Gig> userGigs = gigRepository.findByProfileIdAndStatus(userId, Gig.Status.ACTIVE);
            if (!userGigs.isEmpty()) {
                summary.append("\nService Portfolio:\n");
                
                // Group gigs by category for better organization
                Map<String, List<Gig>> gigsByCategory = userGigs.stream()
                    .filter(gig -> gig.getCategory() != null && !gig.getCategory().trim().isEmpty())
                    .collect(Collectors.groupingBy(Gig::getCategory));
                
                // Add ungrouped gigs
                List<Gig> uncategorizedGigs = userGigs.stream()
                    .filter(gig -> gig.getCategory() == null || gig.getCategory().trim().isEmpty())
                    .collect(Collectors.toList());
                
                // Process categorized gigs
                for (Map.Entry<String, List<Gig>> entry : gigsByCategory.entrySet()) {
                    summary.append(entry.getKey()).append(" Services: ");
                    
                    for (int i = 0; i < entry.getValue().size(); i++) {
                        Gig gig = entry.getValue().get(i);
                        appendGigDetails(summary, gig);
                        
                        if (i < entry.getValue().size() - 1) {
                            summary.append("; ");
                        }
                    }
                    summary.append(". ");
                }
                
                // Process uncategorized gigs
                if (!uncategorizedGigs.isEmpty()) {
                    summary.append("Additional Services: ");
                    for (int i = 0; i < uncategorizedGigs.size(); i++) {
                        appendGigDetails(summary, uncategorizedGigs.get(i));
                        if (i < uncategorizedGigs.size() - 1) {
                            summary.append("; ");
                        }
                    }
                    summary.append(". ");
                }
                
                // Add aggregated skill summary from all gigs
                Set<String> allGigSkills = userGigs.stream()
                    .filter(gig -> gig.getTags() != null)
                    .flatMap(gig -> Arrays.stream(gig.getTags()))
                    .filter(tag -> tag != null && !tag.trim().isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toSet());
                
                if (!allGigSkills.isEmpty()) {
                    summary.append("Demonstrated Technologies: ")
                           .append(String.join(", ", allGigSkills))
                           .append(". ");
                }
                
                // Add portfolio statistics
                summary.append("Total Active Services: ").append(userGigs.size());
                
                // Add overall performance indicators if available
                double avgRating = userGigs.stream()
                    .filter(gig -> gig.getReviewAvg() != null)
                    .mapToDouble(gig -> gig.getReviewAvg().doubleValue())
                    .average()
                    .orElse(0.0);
                
                if (avgRating > 0) {
                    summary.append(". Portfolio Average Rating: ").append(String.format("%.1f", avgRating)).append("/5.0");
                }
                
                summary.append(".");
                
            } else {
                summary.append("No active service offerings currently available. Profile represents capabilities and availability for new projects.");
            }
            
        } catch (Exception e) {
            log.warn("Error fetching gigs for profile embedding of user {}: {}", userId, e.getMessage());
            summary.append("Profile represents core capabilities for freelance services.");
        }
        
        return summary.toString();
    }
    
    /**
     * Helper method to append gig details consistently with performance indicators
     * Uses concise descriptions (200 chars max) to avoid embedding bloat
     */
    private void appendGigDetails(StringBuilder summary, Gig gig) {
        if (gig.getTitle() != null && !gig.getTitle().trim().isEmpty()) {
            summary.append(gig.getTitle().trim());
        }
        
        // Add a concise version of the description (first 200 chars for semantic value)
        if (gig.getDescription() != null && !gig.getDescription().trim().isEmpty()) {
            String description = gig.getDescription().trim();
            String conciseDesc = description.length() > 200 ? 
                description.substring(0, 200) + "..." : description;
            summary.append(" - ").append(conciseDesc);
        }
        
        // Add technical tags for better skill matching
        if (gig.getTags() != null && gig.getTags().length > 0) {
            List<String> cleanTags = Arrays.stream(gig.getTags())
                .filter(tag -> tag != null && !tag.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());
            
            if (!cleanTags.isEmpty()) {
                summary.append(" [Technologies: ").append(String.join(", ", cleanTags)).append("]");
            }
        }
        
        // Add performance indicators if available
        if (gig.getReviewAvg() != null && gig.getReviewAvg().doubleValue() > 0) {
            summary.append(" (Rating: ").append(gig.getReviewAvg()).append("/5.0)");
        }
        
        if (gig.getReviewsCount() != null && gig.getReviewsCount() > 0) {
            summary.append(" (").append(gig.getReviewsCount()).append(" reviews)");
        }
    }

    private String generateContentHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}