package com.thefreelancer.microservices.gig.service;

import com.thefreelancer.microservices.gig.model.Gig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GigEmbeddingService {
    
    private final VectorStore vectorStore;
    
    private static final String DOCUMENT_TYPE_KEY = "documentType";
    private static final String GIG_DOCUMENT_TYPE = "gig";
    private static final String GIG_ID_KEY = "gigId";
    private static final String PROFILE_ID_KEY = "profileId";
    private static final String TITLE_KEY = "title";
    private static final String DESCRIPTION_KEY = "description";
    private static final String CATEGORY_KEY = "category";
    private static final String TAGS_KEY = "tags";
    private static final String STATUS_KEY = "status";
    private static final String CONTENT_HASH_KEY = "contentHash";
    
    /**
     * Store gig embedding in vector database
     */
    public void storeGigEmbedding(Gig gig) {
        try {
            // Create comprehensive text for embedding
            String gigContent = createGigEmbeddingContent(gig);
            String contentHash = generateContentHash(gigContent);
            
            // Check if embedding already exists and is up to date
            Optional<Document> existingDoc = findExistingGigDocument(gig.getId());
            if (existingDoc.isPresent()) {
                String existingHash = existingDoc.get().getMetadata().get(CONTENT_HASH_KEY).toString();
                if (contentHash.equals(existingHash)) {
                    log.debug("Gig embedding for gig {} is already up to date", gig.getId());
                    return;
                }
                
                // Remove old embedding
                deleteGigEmbedding(gig.getId());
            }
            
            // Create document with metadata
            Document document = new Document(
                gigContent,
                Map.of(
                    DOCUMENT_TYPE_KEY, GIG_DOCUMENT_TYPE,
                    GIG_ID_KEY, gig.getId().toString(),
                    PROFILE_ID_KEY, gig.getProfileId().toString(),
                    TITLE_KEY, gig.getTitle() != null ? gig.getTitle() : "",
                    DESCRIPTION_KEY, gig.getDescription() != null ? gig.getDescription() : "",
                    CATEGORY_KEY, gig.getCategory() != null ? gig.getCategory() : "",
                    TAGS_KEY, gig.getTags() != null ? String.join(",", gig.getTags()) : "",
                    STATUS_KEY, gig.getStatus().toString(),
                    CONTENT_HASH_KEY, contentHash
                )
            );
            
            // Store in vector database
            vectorStore.add(List.of(document));
            log.info("Stored gig embedding for gig ID: {} ({})", gig.getId(), gig.getTitle());
            
        } catch (Exception e) {
            log.error("Failed to store gig embedding for gig ID {}: {}", gig.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to store gig embedding", e);
        }
    }
    
    /**
     * Find similar gigs using semantic search
     */
    public List<Document> findSimilarGigs(String query, int limit, double threshold) {
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(limit)
                .similarityThreshold(threshold)
                .filterExpression(String.format("%s == '%s' && %s == '%s'", 
                    DOCUMENT_TYPE_KEY, GIG_DOCUMENT_TYPE,
                    STATUS_KEY, Gig.Status.ACTIVE.toString()))
                .build();
            
            List<Document> results = vectorStore.similaritySearch(searchRequest);
            log.info("Found {} similar gigs for query: '{}'", results.size(), query);
            return results;
            
        } catch (Exception e) {
            log.error("Failed to find similar gigs for query '{}': {}", query, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Find gigs by category with semantic enhancement
     */
    public List<Document> findGigsByCategory(String category, String additionalQuery, int limit) {
        try {
            String enhancedQuery = category;
            if (additionalQuery != null && !additionalQuery.trim().isEmpty()) {
                enhancedQuery = category + " " + additionalQuery;
            }
            
            SearchRequest searchRequest = SearchRequest.builder()
                .query(enhancedQuery)
                .topK(limit)
                .similarityThreshold(0.6)
                .filterExpression(String.format("%s == '%s' && %s == '%s' && %s == '%s'", 
                    DOCUMENT_TYPE_KEY, GIG_DOCUMENT_TYPE,
                    STATUS_KEY, Gig.Status.ACTIVE.toString(),
                    CATEGORY_KEY, category))
                .build();
            
            List<Document> results = vectorStore.similaritySearch(searchRequest);
            log.info("Found {} gigs in category '{}' with query: '{}'", results.size(), category, enhancedQuery);
            return results;
            
        } catch (Exception e) {
            log.error("Failed to find gigs by category '{}': {}", category, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Update gig embedding
     */
    public void updateGigEmbedding(Gig gig) {
        try {
            deleteGigEmbedding(gig.getId());
            storeGigEmbedding(gig);
        } catch (Exception e) {
            log.error("Failed to update gig embedding for gig ID {}: {}", gig.getId(), e.getMessage());
        }
    }
    
    /**
     * Delete gig embedding
     */
    public void deleteGigEmbedding(Long gigId) {
        try {
            // Search for the document to delete
            SearchRequest searchRequest = SearchRequest.builder()
                .query("*")
                .topK(1)
                .filterExpression(String.format("%s == '%s' && %s == '%s'", 
                    DOCUMENT_TYPE_KEY, GIG_DOCUMENT_TYPE,
                    GIG_ID_KEY, gigId.toString()))
                .build();
            
            List<Document> documents = vectorStore.similaritySearch(searchRequest);
            if (!documents.isEmpty()) {
                // Delete using document ID
                vectorStore.delete(documents.stream()
                    .map(doc -> doc.getId())
                    .collect(Collectors.toList()));
                log.info("Deleted gig embedding for gig ID: {}", gigId);
            }
        } catch (Exception e) {
            log.error("Failed to delete gig embedding for gig ID {}: {}", gigId, e.getMessage());
        }
    }
    
    /**
     * Create comprehensive embedding content for gig
     */
    private String createGigEmbeddingContent(Gig gig) {
        StringBuilder content = new StringBuilder();
        
        // Title with emphasis
        if (gig.getTitle() != null && !gig.getTitle().trim().isEmpty()) {
            content.append("Service: ").append(gig.getTitle()).append("\n");
        }
        
        // Description
        if (gig.getDescription() != null && !gig.getDescription().trim().isEmpty()) {
            content.append("Description: ").append(gig.getDescription()).append("\n");
        }
        
        // Category
        if (gig.getCategory() != null && !gig.getCategory().trim().isEmpty()) {
            content.append("Category: ").append(gig.getCategory()).append("\n");
        }
        
        // Tags
        if (gig.getTags() != null && gig.getTags().length > 0) {
            content.append("Tags: ").append(String.join(", ", gig.getTags())).append("\n");
        }
        
        // // Package information if available
        // if (gig.getGigPackages() != null && !gig.getGigPackages().isEmpty()) {
        //     content.append("Service Packages:\n");
        //     gig.getGigPackages().forEach(pkg -> {
        //         content.append("- ").append(pkg.getTitle())
        //             .append(" (").append(pkg.getTier()).append("): ")
        //             .append(pkg.getDescription()).append("\n");
        //     });
        // }
        
        return content.toString().trim();
    }
    
    /**
     * Generate content hash for change detection
     */
    private String generateContentHash(String content) {
        return Integer.toHexString(content.hashCode());
    }
    
    /**
     * Find existing gig document
     */
    private Optional<Document> findExistingGigDocument(Long gigId) {
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                .query("*")
                .topK(1)
                .filterExpression(String.format("%s == '%s' && %s == '%s'", 
                    DOCUMENT_TYPE_KEY, GIG_DOCUMENT_TYPE,
                    GIG_ID_KEY, gigId.toString()))
                .build();
            
            List<Document> documents = vectorStore.similaritySearch(searchRequest);
            return documents.isEmpty() ? Optional.empty() : Optional.of(documents.get(0));
        } catch (Exception e) {
            log.warn("Error finding existing gig document for gig {}: {}", gigId, e.getMessage());
            return Optional.empty();
        }
    }
}