package com.thefreelancer.microservices.gig.controller;

import com.thefreelancer.microservices.gig.dto.RecommendationRequestDto;
import com.thefreelancer.microservices.gig.dto.RecommendationResponseDto;
import com.thefreelancer.microservices.gig.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Job Recommendations", description = "AI-powered job recommendation API")
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    @GetMapping("/{userId}")
    @Operation(summary = "Get job recommendations for a user", 
               description = "Returns AI-powered job recommendations based on user's profile and tech stack")
    public ResponseEntity<RecommendationResponseDto> getRecommendations(
            @Parameter(description = "User ID to get recommendations for")
            @PathVariable Long userId,
            
            @Parameter(description = "Maximum number of recommendations to return")
            @RequestParam(defaultValue = "10") Integer limit,
            
            @Parameter(description = "Minimum match score threshold (0.0 to 1.0)")
            @RequestParam(defaultValue = "0.1") Double minMatchScore,
            
            @Parameter(description = "Comma-separated list of categories to exclude")
            @RequestParam(required = false) String excludeCategories,
            
            @Parameter(description = "Comma-separated list of preferred categories")
            @RequestParam(required = false) String preferredCategories) {
        
        log.info("Getting recommendations for user: {} with limit: {}", userId, limit);
        
        RecommendationRequestDto request = RecommendationRequestDto.builder()
            .userId(userId)
            .limit(limit)
            .minMatchScore(minMatchScore)
            .build();
        
        // TODO: Parse excludeCategories and preferredCategories if provided
        
        RecommendationResponseDto response = recommendationService.getRecommendations(userId, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{userId}")
    @Operation(summary = "Get job recommendations with detailed filters", 
               description = "Returns AI-powered job recommendations with advanced filtering options")
    public ResponseEntity<RecommendationResponseDto> getRecommendationsWithFilters(
            @Parameter(description = "User ID to get recommendations for")
            @PathVariable Long userId,
            
            @RequestBody RecommendationRequestDto request) {
        
        log.info("Getting filtered recommendations for user: {}", userId);
        
        // Set userId in request
        request.setUserId(userId);
        
        RecommendationResponseDto response = recommendationService.getRecommendations(userId, request);
        return ResponseEntity.ok(response);
    }
}