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
 * Service for managing job embeddings using Spring AI VectorStore
 * Uses PGVector as the underlying vector database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobEmbeddingService {

    private final VectorStore vectorStore;
    
    private static final String JOB_DOCUMENT_TYPE = "job";
    private static final String JOB_ID_KEY = "jobId";
    private static final String CONTENT_HASH_KEY = "contentHash";
    private static final String SKILLS_KEY = "skills";
    private static final String PROJECT_NAME_KEY = "projectName";
    private static final String DESCRIPTION_KEY = "description";
    private static final String DOCUMENT_TYPE_KEY = "documentType";
    private static final String BUDGET_MIN_KEY = "budgetMin";
    private static final String BUDGET_MAX_KEY = "budgetMax";
    private static final String BUDGET_TYPE_KEY = "budgetType";
    
    /**
     * Store or update job embedding
     */
    public void storeJobEmbedding(Long jobId, String projectName, String description, 
                                 List<String> skills, String budgetType, 
                                 Long budgetMin, Long budgetMax) {
        try {
            // Create job summary for embedding
            String jobSummary = createJobSummary(projectName, description, skills);
            String contentHash = generateContentHash(jobSummary);
            
            // Check if embedding already exists and is up to date
            Optional<Document> existingDoc = findExistingJobDocument(jobId);
            if (existingDoc.isPresent()) {
                String existingHash = existingDoc.get().getMetadata().get(CONTENT_HASH_KEY).toString();
                if (contentHash.equals(existingHash)) {
                    log.debug("Job embedding for job {} is already up to date", jobId);
                    return;
                }
                // Remove old embedding
                deleteJobEmbedding(jobId);
            }
            
            // Create document with metadata
            Document document = new Document(
                jobSummary,
                Map.of(
                    JOB_ID_KEY, jobId.toString(),
                    CONTENT_HASH_KEY, contentHash,
                    SKILLS_KEY, String.join(", ", skills),
                    PROJECT_NAME_KEY, projectName != null ? projectName : "",
                    DESCRIPTION_KEY, description != null ? description : "",
                    DOCUMENT_TYPE_KEY, JOB_DOCUMENT_TYPE,
                    BUDGET_TYPE_KEY, budgetType != null ? budgetType : "",
                    BUDGET_MIN_KEY, budgetMin != null ? budgetMin.toString() : "0",
                    BUDGET_MAX_KEY, budgetMax != null ? budgetMax.toString() : "0"
                )
            );
            
            // Store in vector database
            vectorStore.add(List.of(document));
            log.info("Stored job embedding for job {}", jobId);
            
        } catch (Exception e) {
            log.error("Error storing job embedding for job {}: {}", jobId, e.getMessage(), e);
            throw new RuntimeException("Failed to store job embedding", e);
        }
    }
    
    /**
     * Find similar jobs based on description and skills
     */
    public List<Document> findSimilarJobs(String query, int topK, double similarityThreshold) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        Filter.Expression filter = builder.eq(DOCUMENT_TYPE_KEY, JOB_DOCUMENT_TYPE).build();
        
        SearchRequest searchRequest = SearchRequest.builder()
            .query(query)
            .topK(topK)
            .similarityThreshold(similarityThreshold)
            .filterExpression(filter)
            .build();
            
        return vectorStore.similaritySearch(searchRequest);
    }
    
    /**
     * Find jobs by specific skills
     */
    public List<Document> findJobsBySkills(List<String> skills, int topK) {
        String skillsQuery = String.join(" ", skills);
        return findSimilarJobs(skillsQuery, topK, 0.7);
    }
    
    /**
     * Find jobs within budget range
     */
    public List<Document> findJobsInBudgetRange(String query, String budgetType, 
                                               Long minBudget, Long maxBudget, int topK) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        Filter.Expression filter = builder.and(
            builder.eq(DOCUMENT_TYPE_KEY, JOB_DOCUMENT_TYPE),
            builder.eq(BUDGET_TYPE_KEY, budgetType)
        ).build();
        
        SearchRequest searchRequest = SearchRequest.builder()
            .query(query)
            .topK(topK)
            .filterExpression(filter)
            .build();
            
        return vectorStore.similaritySearch(searchRequest);
    }
    
    /**
     * Delete job embedding
     */
    public void deleteJobEmbedding(Long jobId) {
        try {
            // Find existing documents for this job
            List<Document> existingDocs = findAllJobDocuments(jobId);
            
            if (!existingDocs.isEmpty()) {
                // Extract document IDs for deletion
                List<String> documentIds = existingDocs.stream()
                    .map(Document::getId)
                    .toList();
                
                vectorStore.delete(documentIds);
                log.info("Deleted {} job embedding documents for job {}", documentIds.size(), jobId);
            } else {
                log.debug("No job embeddings found for job {} to delete", jobId);
            }
            
        } catch (Exception e) {
            log.error("Error deleting job embedding for job {}: {}", jobId, e.getMessage(), e);
        }
    }
    
    private Optional<Document> findExistingJobDocument(Long jobId) {
        List<Document> docs = findAllJobDocuments(jobId);
        return docs.isEmpty() ? Optional.empty() : Optional.of(docs.get(0));
    }
    
    public List<Document> findAllJobDocuments(Long jobId) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        Filter.Expression filter = builder.and(
            builder.eq(DOCUMENT_TYPE_KEY, JOB_DOCUMENT_TYPE),
            builder.eq(JOB_ID_KEY, jobId.toString())
        ).build();
        
        SearchRequest searchRequest = SearchRequest.builder()
            .query("*") // Match any content
            .topK(10) // Get all documents for this job
            .filterExpression(filter)
            .build();
            
        return vectorStore.similaritySearch(searchRequest);
    }
    
    private String createJobSummary(String projectName, String description, List<String> skills) {
        StringBuilder summary = new StringBuilder();
        
        if (projectName != null && !projectName.trim().isEmpty()) {
            summary.append("Project: ").append(projectName.trim()).append(". ");
        }
        
        if (description != null && !description.trim().isEmpty()) {
            summary.append("Description: ").append(description.trim()).append(". ");
        }
        
        if (skills != null && !skills.isEmpty()) {
            summary.append("Required Skills: ").append(String.join(", ", skills)).append(".");
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