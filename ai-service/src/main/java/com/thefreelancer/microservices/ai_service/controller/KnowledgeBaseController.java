package com.thefreelancer.microservices.ai_service.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thefreelancer.microservices.ai_service.service.KnowledgeBaseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for managing knowledge base content
 */
@RestController
@RequestMapping("/api/ai/knowledge")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Knowledge Base Management", description = "Manage chatbot knowledge base content")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class KnowledgeBaseController {
    
    private final KnowledgeBaseService knowledgeBaseService;
    
    @Operation(
        summary = "Add knowledge entry",
        description = "Add a new entry to the knowledge base with vector embedding"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Knowledge entry added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addKnowledge(@RequestBody Map<String, Object> request) {
        try {
            // Extract and validate request data
            String title = (String) request.get("title");
            String content = (String) request.get("content");
            String contentType = (String) request.get("contentType");
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) request.get("tags");
            
            // Validate required fields
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "Title is required",
                        "timestamp", Instant.now().toString()
                    ));
            }
            
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "Content is required",
                        "timestamp", Instant.now().toString()
                    ));
            }
            
            // Set defaults
            if (contentType == null || contentType.trim().isEmpty()) {
                contentType = "guide";
            }
            
            if (tags == null) {
                tags = List.of();
            }
            
            // Validate content type
            if (!List.of("faq", "guide", "policy", "feature").contains(contentType)) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "Content type must be one of: faq, guide, policy, feature",
                        "timestamp", Instant.now().toString()
                    ));
            }
            
            log.info("Adding knowledge entry: {}", title);
            knowledgeBaseService.addKnowledgeEntry(title.trim(), content.trim(), contentType, tags);
            
            return ResponseEntity.ok(Map.of(
                "message", "Knowledge entry added successfully",
                "title", title,
                "timestamp", Instant.now().toString(),
                "status", "success"
            ));
            
        } catch (Exception e) {
            log.error("Error adding knowledge entry", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Failed to add knowledge entry: " + e.getMessage(),
                    "timestamp", Instant.now().toString(),
                    "status", "error"
                ));
        }
    }
    
    @Operation(
        summary = "Search knowledge base",
        description = "Search for similar content in the knowledge base using vector similarity or text search"
    )
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchKnowledge(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "Query parameter is required",
                        "timestamp", Instant.now().toString()
                    ));
            }
            
            // Limit the number of results
            if (limit > 20) {
                limit = 20;
            }
            
            log.info("Searching knowledge base for: {}", query);
            List<Map<String, Object>> results = knowledgeBaseService.searchSimilarContent(query.trim(), limit);
            
            return ResponseEntity.ok(Map.of(
                "results", results,
                "query", query,
                "count", results.size(),
                "timestamp", Instant.now().toString(),
                "status", "success"
            ));
            
        } catch (Exception e) {
            log.error("Error searching knowledge base", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Failed to search knowledge base: " + e.getMessage(),
                    "timestamp", Instant.now().toString(),
                    "status", "error"
                ));
        }
    }
    
    @Operation(
        summary = "Get all knowledge entries",
        description = "Retrieve all entries from the knowledge base"
    )
    @ApiResponse(responseCode = "200", description = "Knowledge entries retrieved successfully")
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllKnowledge() {
        try {
            log.info("Retrieving all knowledge entries");
            List<Map<String, Object>> entries = knowledgeBaseService.getAllKnowledgeEntries();
            
            return ResponseEntity.ok(Map.of(
                "entries", entries,
                "count", entries.size(),
                "timestamp", Instant.now().toString(),
                "status", "success"
            ));
            
        } catch (Exception e) {
            log.error("Error retrieving all knowledge entries", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Failed to retrieve knowledge entries: " + e.getMessage(),
                    "timestamp", Instant.now().toString(),
                    "status", "error"
                ));
        }
    }
    
    @Operation(
        summary = "Delete knowledge entry",
        description = "Delete a knowledge entry by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Knowledge entry deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Knowledge entry not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteKnowledge(@PathVariable String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "ID is required",
                        "timestamp", Instant.now().toString()
                    ));
            }
            
            log.info("Deleting knowledge entry with ID: {}", id);
            knowledgeBaseService.deleteKnowledgeEntry(id.trim());
            
            return ResponseEntity.ok(Map.of(
                "message", "Knowledge entry deleted successfully",
                "id", id,
                "timestamp", Instant.now().toString(),
                "status", "success"
            ));
            
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            log.error("Error deleting knowledge entry", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Failed to delete knowledge entry: " + e.getMessage(),
                    "timestamp", Instant.now().toString(),
                    "status", "error"
                ));
        } catch (Exception e) {
            log.error("Error deleting knowledge entry", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Failed to delete knowledge entry: " + e.getMessage(),
                    "timestamp", Instant.now().toString(),
                    "status", "error"
                ));
        }
    }
}