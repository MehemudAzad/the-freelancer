package com.thefreelancer.microservices.ai_service.service;

import java.util.List;
import java.util.Map;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing knowledge base entries and performing vector similarity search
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseService {
    
    @Autowired(required = false)
    private EmbeddingModel embeddingModel;
    
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * Add a new knowledge entry to the database with vector embedding
     */
    public void addKnowledgeEntry(String title, String content, String contentType, List<String> tags) {
        try {
            if (embeddingModel != null) {
                // Generate embedding for the content
                var embedding = embeddingModel.embed(title + " " + content);
                
                // Convert embedding to PostgreSQL vector format
                StringBuilder vectorBuilder = new StringBuilder("[");
                for (int i = 0; i < embedding.length; i++) {
                    if (i > 0) vectorBuilder.append(",");
                    vectorBuilder.append(embedding[i]);
                }
                vectorBuilder.append("]");
                String vectorStr = vectorBuilder.toString();
                
                // Insert into knowledge base with embedding
                jdbcTemplate.update(
                    "INSERT INTO knowledge_base (title, content, content_type, tags, embedding) VALUES (?, ?, ?, ?, ?::vector)",
                    title, content, contentType, tags.toArray(new String[0]), vectorStr
                );
            } else {
                // Insert without embedding if embedding model is not available
                jdbcTemplate.update(
                    "INSERT INTO knowledge_base (title, content, content_type, tags) VALUES (?, ?, ?, ?)",
                    title, content, contentType, tags.toArray(new String[0])
                );
            }
            
            log.info("Added knowledge entry: {}", title);
        } catch (Exception e) {
            log.error("Failed to add knowledge entry: {}", title, e);
            throw new RuntimeException("Failed to add knowledge entry", e);
        }
    }
    
    /**
     * Search for similar content using vector similarity or text search as fallback
     */
    public List<Map<String, Object>> searchSimilarContent(String query, int limit) {
        try {
            if (embeddingModel != null) {
                return performVectorSearch(query, limit);
            } else {
                return performTextSearch(query, limit);
            }
        } catch (Exception e) {
            log.error("Failed to search similar content for query: {}", query, e);
            // Fallback to text search if vector search fails
            return performTextSearch(query, limit);
        }
    }
    
    /**
     * Perform vector similarity search
     */
    private List<Map<String, Object>> performVectorSearch(String query, int limit) {
        try {
            // Generate embedding for the query
            var queryEmbedding = embeddingModel.embed(query);
            StringBuilder vectorBuilder = new StringBuilder("[");
            for (int i = 0; i < queryEmbedding.length; i++) {
                if (i > 0) vectorBuilder.append(",");
                vectorBuilder.append(queryEmbedding[i]);
            }
            vectorBuilder.append("]");
            String vectorStr = vectorBuilder.toString();
            
            // Search for similar content using cosine similarity
            return jdbcTemplate.queryForList(
                """
                SELECT title, content, content_type, tags, 
                       (1 - (embedding <=> ?::vector)) as similarity_score
                FROM knowledge_base 
                WHERE embedding IS NOT NULL 
                  AND (1 - (embedding <=> ?::vector)) > 0.3
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """,
                vectorStr, vectorStr, vectorStr, limit
            );
        } catch (Exception e) {
            log.warn("Vector search failed, falling back to text search: {}", e.getMessage());
            return performTextSearch(query, limit);
        }
    }
    
    /**
     * Perform text-based search as fallback
     */
    private List<Map<String, Object>> performTextSearch(String query, int limit) {
        try {
            return jdbcTemplate.queryForList(
                """
                SELECT title, content, content_type, tags, 
                       ts_rank(to_tsvector('english', title || ' ' || content), plainto_tsquery('english', ?)) as similarity_score
                FROM knowledge_base 
                WHERE to_tsvector('english', title || ' ' || content) @@ plainto_tsquery('english', ?)
                ORDER BY similarity_score DESC
                LIMIT ?
                """,
                query, query, limit
            );
        } catch (Exception e) {
            log.error("Text search also failed: {}", e.getMessage());
            // Return basic keyword matching as last resort
            return jdbcTemplate.queryForList(
                """
                SELECT title, content, content_type, tags, 1.0 as similarity_score
                FROM knowledge_base 
                WHERE LOWER(title) LIKE LOWER(?) OR LOWER(content) LIKE LOWER(?)
                ORDER BY CASE 
                    WHEN LOWER(title) LIKE LOWER(?) THEN 1
                    ELSE 2
                END
                LIMIT ?
                """,
                "%" + query + "%", "%" + query + "%", "%" + query + "%", limit
            );
        }
    }
    
    /**
     * Get all knowledge entries
     */
    public List<Map<String, Object>> getAllKnowledgeEntries() {
        try {
            return jdbcTemplate.queryForList(
                "SELECT id, title, content, content_type, tags, created_at, updated_at FROM knowledge_base ORDER BY created_at DESC"
            );
        } catch (Exception e) {
            log.error("Failed to get all knowledge entries", e);
            throw new RuntimeException("Failed to get knowledge entries", e);
        }
    }
    
    /**
     * Delete a knowledge entry by ID
     */
    public void deleteKnowledgeEntry(String id) {
        try {
            int rowsAffected = jdbcTemplate.update("DELETE FROM knowledge_base WHERE id = ?", id);
            if (rowsAffected > 0) {
                log.info("Deleted knowledge entry with ID: {}", id);
            } else {
                log.warn("No knowledge entry found with ID: {}", id);
                throw new RuntimeException("Knowledge entry not found");
            }
        } catch (Exception e) {
            log.error("Failed to delete knowledge entry with ID: {}", id, e);
            throw new RuntimeException("Failed to delete knowledge entry", e);
        }
    }
}