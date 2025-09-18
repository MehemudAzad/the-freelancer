package com.thefreelancer.microservices.gig.service;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing profile embeddings using Spring AI VectorStore
 * Uses PGVector as the underlying vector database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileEmbeddingService {

    private final VectorStore vectorStore;
    
    private static final String PROFILE_DOCUMENT_TYPE = "profile";
    private static final String USER_ID_KEY = "userId";
    private static final String CONTENT_HASH_KEY = "contentHash";
    private static final String SKILLS_KEY = "skills";
    private static final String HEADLINE_KEY = "headline";
    private static final String BIO_KEY = "bio";
    private static final String DOCUMENT_TYPE_KEY = "documentType";
    
    /**
     * Store or update profile embedding
     */
    public void storeProfileEmbedding(Long userId, String headline, String bio, List<String> skills) {
        try {
            // Create profile summary for embedding
            String profileSummary = createProfileSummary(headline, bio, skills);
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
            log.info("Stored profile embedding for user {}", userId);
            
        } catch (Exception e) {
            log.error("Error storing profile embedding for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to store profile embedding", e);
        }
    }
    
    /**
     * Find similar profiles based on skills and description
     */
    public List<Document> findSimilarProfiles(String query, int topK, double similarityThreshold) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        Filter.Expression filter = builder.eq(DOCUMENT_TYPE_KEY, PROFILE_DOCUMENT_TYPE).build();
        
        SearchRequest searchRequest = SearchRequest.builder()
            .query(query)
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
    
    private String createProfileSummary(String headline, String bio, List<String> skills) {
        StringBuilder summary = new StringBuilder();
        
        if (headline != null && !headline.trim().isEmpty()) {
            summary.append("Headline: ").append(headline.trim()).append(". ");
        }
        
        if (bio != null && !bio.trim().isEmpty()) {
            summary.append("Bio: ").append(bio.trim()).append(". ");
        }
        
        if (skills != null && !skills.isEmpty()) {
            summary.append("Skills: ").append(String.join(", ", skills)).append(".");
        }
        
        return summary.toString();
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