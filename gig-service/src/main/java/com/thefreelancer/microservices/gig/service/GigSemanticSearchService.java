package com.thefreelancer.microservices.gig.service;

import com.thefreelancer.microservices.gig.dto.GigSearchRequestDto;
import com.thefreelancer.microservices.gig.dto.GigSearchResponseDto;
import com.thefreelancer.microservices.gig.dto.GigSearchResultDto;
import com.thefreelancer.microservices.gig.model.Gig;
import com.thefreelancer.microservices.gig.repository.GigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GigSemanticSearchService {
    
    private final GigEmbeddingService gigEmbeddingService;
    private final GigRepository gigRepository;
    private final QueryEnhancementService queryEnhancementService;
    
    /**
     * Perform semantic search on gigs
     */
    public GigSearchResponseDto searchGigs(GigSearchRequestDto request) {
        log.info("Semantic search for gigs: '{}'", request.getQuery());
        
        try {
            // Enhance query for better semantic matching
            String enhancedQuery = queryEnhancementService.enhanceGigQuery(request.getQuery());
            log.info("Enhanced gig query: '{}' -> '{}'", request.getQuery(), enhancedQuery);
            
            // Perform vector search
            List<Document> documents = gigEmbeddingService.findSimilarGigs(
                enhancedQuery, 
                request.getLimit(), 
                0.7
            );
            
            // Convert documents to search results
            List<GigSearchResultDto> results = documents.stream()
                .map(this::convertDocumentToSearchResult)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            // Apply additional filters if specified
            results = applyFilters(results, request);
            
            // Calculate enhanced relevance scores
            results = enhanceRelevanceScoring(results, request.getQuery());
            
            return GigSearchResponseDto.builder()
                .query(request.getQuery())
                .enhancedQuery(enhancedQuery)
                .results(results)
                .totalResults(results.size())
                .searchType("semantic")
                .build();
            
        } catch (Exception e) {
            log.error("Error performing semantic search for gigs: {}", e.getMessage(), e);
            return GigSearchResponseDto.builder()
                .query(request.getQuery())
                .results(Collections.emptyList())
                .totalResults(0)
                .searchType("semantic")
                .error("Search temporarily unavailable")
                .build();
        }
    }
    
    /**
     * Search gigs by category with semantic enhancement
     */
    public GigSearchResponseDto searchGigsByCategory(String category, String additionalQuery, int limit) {
        log.info("Searching gigs by category: '{}' with additional query: '{}'", category, additionalQuery);
        
        try {
            List<Document> documents = gigEmbeddingService.findGigsByCategory(
                category, 
                additionalQuery, 
                limit
            );
            
            List<GigSearchResultDto> results = documents.stream()
                .map(this::convertDocumentToSearchResult)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            return GigSearchResponseDto.builder()
                .query(category + (additionalQuery != null ? " " + additionalQuery : ""))
                .results(results)
                .totalResults(results.size())
                .searchType("category_semantic")
                .build();
            
        } catch (Exception e) {
            log.error("Error searching gigs by category: {}", e.getMessage(), e);
            return GigSearchResponseDto.builder()
                .query(category)
                .results(Collections.emptyList())
                .totalResults(0)
                .searchType("category_semantic")
                .error("Search temporarily unavailable")
                .build();
        }
    }
    
    /**
     * Convert document to search result
     */
    private GigSearchResultDto convertDocumentToSearchResult(Document document) {
        try {
            Map<String, Object> metadata = document.getMetadata();
            
            Long gigId = Long.parseLong(metadata.get("gigId").toString());
            Long profileId = Long.parseLong(metadata.get("profileId").toString());
            String title = metadata.get("title").toString();
            String description = metadata.get("description").toString();
            String category = metadata.get("category").toString();
            String tagsString = metadata.get("tags").toString();
            
            // Parse tags
            List<String> tags = tagsString.isEmpty() ? 
                Collections.emptyList() : 
                Arrays.asList(tagsString.split(","));
            
            // Calculate relevance score
            double relevanceScore = calculateRelevanceScore(document);
            
            // Get additional gig details from database if needed
            Optional<Gig> gigOpt = gigRepository.findById(gigId);
            if (gigOpt.isEmpty()) {
                log.warn("Gig {} found in vector search but not in database", gigId);
                return null;
            }
            
            Gig gig = gigOpt.get();
            
            return GigSearchResultDto.builder()
                .gigId(gigId)
                .profileId(profileId)
                .title(title)
                .description(description)
                .category(category)
                .tags(tags)
                .status(gig.getStatus().toString())
                .reviewsCount(gig.getReviewsCount())
                .relevanceScore(relevanceScore)
                .matchReason(generateMatchReason(document))
                .hasPackages(false)
                .build();
            
        } catch (Exception e) {
            log.error("Error converting document to search result: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Apply additional filters to search results
     */
    private List<GigSearchResultDto> applyFilters(List<GigSearchResultDto> results, GigSearchRequestDto request) {
        return results.stream()
            .filter(result -> {
                // Category filter
                if (request.getCategory() != null && !request.getCategory().isEmpty()) {
                    if (!result.getCategory().equalsIgnoreCase(request.getCategory())) {
                        return false;
                    }
                }
                
                // Minimum rating filter
                if (request.getMinRating() != null) {
                    if (result.getReviewAvg() == null || result.getReviewAvg() < request.getMinRating()) {
                        return false;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Enhance relevance scoring based on query analysis
     */
    private List<GigSearchResultDto> enhanceRelevanceScoring(List<GigSearchResultDto> results, String originalQuery) {
        List<String> queryTerms = extractQueryTerms(originalQuery);
        
        return results.stream()
            .map(result -> {
                double enhancedScore = calculateEnhancedRelevanceScore(result, queryTerms);
                return result.toBuilder().relevanceScore(enhancedScore).build();
            })
            .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate enhanced relevance score
     */
    private double calculateEnhancedRelevanceScore(GigSearchResultDto result, List<String> queryTerms) {
        double baseScore = result.getRelevanceScore();
        
        // Title match bonus
        double titleBonus = 0.0;
        for (String term : queryTerms) {
            if (result.getTitle().toLowerCase().contains(term.toLowerCase())) {
                titleBonus += 0.1;
            }
        }
        
        // Tag match bonus
        double tagBonus = 0.0;
        for (String tag : result.getTags()) {
            for (String term : queryTerms) {
                if (tag.toLowerCase().contains(term.toLowerCase())) {
                    tagBonus += 0.05;
                }
            }
        }
        
        // Rating bonus
        double ratingBonus = 0.0;
        if (result.getReviewAvg() != null && result.getReviewsCount() > 0) {
            ratingBonus = (result.getReviewAvg() / 5.0) * 0.1;
        }
        
        return Math.min(1.0, baseScore + titleBonus + tagBonus + ratingBonus);
    }
    
    /**
     * Extract query terms for matching
     */
    private List<String> extractQueryTerms(String query) {
        return Arrays.stream(query.toLowerCase().split("\\s+"))
            .filter(term -> term.length() > 2)
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate relevance score from document
     */
    private double calculateRelevanceScore(Document document) {
        Object distanceObj = document.getMetadata().get("distance");
        if (distanceObj instanceof Number) {
            double distance = ((Number) distanceObj).doubleValue();
            return Math.max(0.0, Math.min(1.0, 1.0 - distance));
        }
        return 0.8; // Default score
    }
    
    /**
     * Generate match reason explanation
     */
    private String generateMatchReason(Document document) {
        return "Content and skill alignment with search criteria";
    }
}