package com.thefreelancer.microservices.gig.service;

import com.thefreelancer.microservices.gig.dto.GigSearchRequestDto;
import com.thefreelancer.microservices.gig.dto.GigSearchResponseDto;
import com.thefreelancer.microservices.gig.model.Gig;
import com.thefreelancer.microservices.gig.repository.GigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GigSemanticSearchService {
    
    private final VectorStore vectorStore;
    private final QueryEnhancementService queryEnhancementService;
    private final GigRepository gigRepository;
    
    /**
     * Advanced semantic search with comprehensive filtering
     */
    public GigSearchResponseDto searchGigs(GigSearchRequestDto request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Performing semantic gig search for query: {}", request.getQuery());
            
            // Build semantic query
            String semanticQuery = buildSemanticQuery(request);
            log.debug("Enhanced semantic query: {}", semanticQuery);
            
            // Perform vector search
            List<Document> documents = performVectorSearch(semanticQuery, request);
            log.debug("Vector search returned {} documents", documents.size());
            
            // Extract gig IDs and fetch full gig data
            List<Long> gigIds = extractGigIds(documents);
            List<Gig> gigs = gigRepository.findAllById(gigIds);
            
            // Apply business filters
            List<Gig> filteredGigs = applyBusinessFilters(gigs, request);
            
            // Add similarity scores and sort
            List<GigSearchResponseDto.GigResult> results = buildResults(filteredGigs, documents, request);
            
            // Build response
            return GigSearchResponseDto.builder()
                .gigs(results)
                .totalFound(results.size())
                .query(request.getQuery())
                .enhancedQuery(semanticQuery)
                .processingTimeMs(System.currentTimeMillis() - startTime)
                .filterSummary(buildFilterSummary(gigs.size(), results.size(), request))
                .build();
                
        } catch (Exception e) {
            log.error("Error performing semantic gig search: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search gigs", e);
        }
    }
    
    /**
     * Simple semantic search with basic filtering
     */
    public GigSearchResponseDto searchGigsSimple(String query, String skills, String category, 
                                                Long minPrice, Long maxPrice, Double minRating, 
                                                Integer limit, Integer offset) {
        
        GigSearchRequestDto request = GigSearchRequestDto.builder()
            .query(query)
            .skills(skills != null ? Arrays.asList(skills.split(",")) : Collections.emptyList())
            .category(category)
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .minRating(minRating)
            .limit(limit != null ? limit : 20)
            .offset(offset != null ? offset : 0)
            .build();
            
        return searchGigs(request);
    }
    
    /**
     * Category-specific semantic search
     */
    public GigSearchResponseDto searchGigsByCategory(String category, String query, String skills,
                                                    Long minPrice, Long maxPrice, Double minRating,
                                                    Integer limit, Integer offset) {
        
        GigSearchRequestDto request = GigSearchRequestDto.builder()
            .query(query)
            .skills(skills != null ? Arrays.asList(skills.split(",")) : Collections.emptyList())
            .category(category)
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .minRating(minRating)
            .limit(limit != null ? limit : 20)
            .offset(offset != null ? offset : 0)
            .build();
            
        return searchGigs(request);
    }
    
    private String buildSemanticQuery(GigSearchRequestDto request) {
        StringBuilder queryBuilder = new StringBuilder();
        
        // Add main query
        if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
            String enhancedQuery = queryEnhancementService.enhanceGigQuery(request.getQuery());
            queryBuilder.append(enhancedQuery);
        }
        
        // Add skills
        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            String skillsText = String.join(" ", request.getSkills());
            if (queryBuilder.length() > 0) queryBuilder.append(" ");
            queryBuilder.append(skillsText);
        }
        
        // Add category context
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            if (queryBuilder.length() > 0) queryBuilder.append(" ");
            queryBuilder.append(request.getCategory());
        }
        
        return queryBuilder.toString().trim();
    }
    
    private List<Document> performVectorSearch(String query, GigSearchRequestDto request) {
        SearchRequest searchRequest = SearchRequest.builder()
            .query(query)
            .topK(Math.max(request.getLimit() * 3, 100)) // Get more for filtering
            .similarityThreshold(0.3)
            .build();
        
        return vectorStore.similaritySearch(searchRequest);
    }
    
    private List<Long> extractGigIds(List<Document> documents) {
        return documents.stream()
            .map(doc -> {
                Object gigIdObj = doc.getMetadata().get("gigId");
                if (gigIdObj instanceof Number) {
                    return ((Number) gigIdObj).longValue();
                } else if (gigIdObj instanceof String) {
                    try {
                        return Long.valueOf((String) gigIdObj);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid gigId format: {}", gigIdObj);
                        return null;
                    }
                }
                return null;
            })
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    }
    
    private List<Gig> applyBusinessFilters(List<Gig> gigs, GigSearchRequestDto request) {
        return gigs.stream()
            .filter(gig -> {
                // Filter by status (only active gigs)
                if (!Gig.Status.ACTIVE.equals(gig.getStatus())) {
                    return false;
                }
                
                // Filter by rating
                if (request.getMinRating() != null && 
                    (gig.getReviewAvg() == null || gig.getReviewAvg().doubleValue() < request.getMinRating())) {
                    return false;
                }
                if (request.getMaxRating() != null && 
                    (gig.getReviewAvg() == null || gig.getReviewAvg().doubleValue() > request.getMaxRating())) {
                    return false;
                }
                
                // Filter by review count
                if (request.getMinReviews() != null && 
                    (gig.getReviewsCount() == null || gig.getReviewsCount() < request.getMinReviews())) {
                    return false;
                }
                
                // TODO: Price filtering requires fetching gig packages
                // This would need a join query or separate service call
                
                return true;
            })
            .collect(Collectors.toList());
    }
    
    private List<GigSearchResponseDto.GigResult> buildResults(List<Gig> gigs, List<Document> documents, GigSearchRequestDto request) {
        // Create similarity score map
        Map<Long, Double> similarityScores = documents.stream()
            .collect(Collectors.toMap(
                doc -> {
                    Object gigIdObj = doc.getMetadata().get("gigId");
                    return gigIdObj instanceof Number ? ((Number) gigIdObj).longValue() : 
                           Long.valueOf(gigIdObj.toString());
                },
                doc -> (Double) doc.getMetadata().getOrDefault("distance", 0.0),
                (existing, replacement) -> existing
            ));
        
        // Convert gigs to results with similarity scores
        List<GigSearchResponseDto.GigResult> results = gigs.stream()
            .map(gig -> {
                Double similarityScore = similarityScores.getOrDefault(gig.getId(), 0.0);
                
                return GigSearchResponseDto.GigResult.builder()
                    .id(gig.getId().toString())
                    .title(gig.getTitle())
                    .description(gig.getDescription())
                    .profileId(gig.getProfileId())
                    .category(gig.getCategory())
                    .tags(gig.getTags() != null ? Arrays.asList(gig.getTags()) : Collections.emptyList())
                    .reviewAvg(gig.getReviewAvg() != null ? gig.getReviewAvg().doubleValue() : 0.0)
                    .reviewsCount(gig.getReviewsCount() != null ? gig.getReviewsCount() : 0)
                    .similarityScore(1.0 - similarityScore) // Convert distance to similarity
                    .createdAt(gig.getCreatedAt())
                    // TODO: Add packages when available
                    .packages(Collections.emptyList())
                    .build();
            })
            .collect(Collectors.toList());
        
        // Sort by relevance (similarity score)
        results.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));
        
        // Apply pagination
        int fromIndex = Math.min(request.getOffset(), results.size());
        int toIndex = Math.min(fromIndex + request.getLimit(), results.size());
        
        return results.subList(fromIndex, toIndex);
    }
    
    private Map<String, Object> buildFilterSummary(int totalBeforeFilters, int totalAfterFilters, GigSearchRequestDto request) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalGigs", totalBeforeFilters);
        summary.put("afterFilters", totalAfterFilters);
        
        List<String> appliedFilters = new ArrayList<>();
        if (request.getMinRating() != null) appliedFilters.add("minRating");
        if (request.getMaxRating() != null) appliedFilters.add("maxRating");
        if (request.getMinReviews() != null) appliedFilters.add("minReviews");
        if (request.getCategory() != null) appliedFilters.add("category");
        if (request.getMinPrice() != null) appliedFilters.add("minPrice");
        if (request.getMaxPrice() != null) appliedFilters.add("maxPrice");
        
        summary.put("appliedFilters", appliedFilters);
        return summary;
    }
    
}