package com.thefreelancer.microservices.gig.controller;

import com.thefreelancer.microservices.gig.dto.MarketAnalysisRequestDto;
import com.thefreelancer.microservices.gig.dto.MarketAnalysisResponseDto;
import com.thefreelancer.microservices.gig.service.MarketplaceAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Marketplace Intelligence APIs
 * Provides comprehensive market analysis using vector embeddings
 */
@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Marketplace Analysis", description = "Market intelligence and analysis APIs using vector embeddings")
public class MarketplaceAnalysisController {
    
    private final MarketplaceAnalysisService marketplaceAnalysisService;
    
    @PostMapping("/analyze")
    @Operation(
        summary = "Comprehensive marketplace analysis",
        description = "Analyze supply/demand patterns, competition, and opportunities for a skill or category using vector embeddings. " +
                     "Provides detailed insights including market metrics, recommendations, and competitive landscape."
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Market analysis completed successfully"
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request parameters"
    )
    public ResponseEntity<MarketAnalysisResponseDto> analyzeMarket(
            @Valid @RequestBody MarketAnalysisRequestDto request) {
        
        log.info("Received comprehensive market analysis request for: {}", request.getSkillOrCategory());
        
        try {
            MarketAnalysisResponseDto response = marketplaceAnalysisService.analyzeMarket(request);
            
            log.info("Market analysis completed for '{}' - Found {} jobs, {} gigs, {} freelancers", 
                    request.getSkillOrCategory(),
                    response.getDemandAnalysis().getTotalJobsFound(),
                    response.getSupplyAnalysis().getTotalGigsFound(),
                    response.getSupplyAnalysis().getTotalFreelancersFound());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in comprehensive market analysis for {}: {}", request.getSkillOrCategory(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/analyze")
    @Operation(
        summary = "Quick marketplace analysis",
        description = "Simple market analysis using query parameters. Provides essential market insights with minimal configuration."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Quick market analysis completed successfully"
    )
    public ResponseEntity<MarketAnalysisResponseDto> analyzeMarketSimple(
            @Parameter(description = "Skill or category to analyze", example = "React development", required = true)
            @RequestParam String skill,
            
            @Parameter(description = "Sample size for analysis (10-500)", example = "100")
            @RequestParam(defaultValue = "100") Integer sampleSize,
            
            @Parameter(description = "Analysis depth level", example = "standard")
            @RequestParam(defaultValue = "standard") String depth,
            
            @Parameter(description = "Geographic filter", example = "Remote")
            @RequestParam(required = false) String location,
            
            @Parameter(description = "Minimum similarity threshold (0.0-1.0)", example = "0.3")
            @RequestParam(defaultValue = "0.3") Double minSimilarity) {
        
        log.info("Received quick market analysis request for: {}", skill);
        
        try {
            // Validate sample size
            if (sampleSize < 10 || sampleSize > 500) {
                log.warn("Invalid sample size {}, using default of 100", sampleSize);
                sampleSize = 100;
            }
            
            // Validate similarity threshold
            if (minSimilarity < 0.0 || minSimilarity > 1.0) {
                log.warn("Invalid similarity threshold {}, using default of 0.3", minSimilarity);
                minSimilarity = 0.3;
            }
            
            MarketAnalysisRequestDto request = MarketAnalysisRequestDto.builder()
                .skillOrCategory(skill)
                .sampleSize(sampleSize)
                .analysisDepth(depth)
                .location(location)
                .minSimilarityThreshold(minSimilarity)
                .build();
                
            MarketAnalysisResponseDto response = marketplaceAnalysisService.analyzeMarket(request);
            
            log.info("Quick market analysis completed for '{}' in {}ms", 
                    skill, response.getProcessingTimeMs());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in quick market analysis for {}: {}", skill, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/trends/{category}")
    @Operation(
        summary = "Category trend analysis",
        description = "Get detailed market trends and insights for a specific category. " +
                     "Analyzes historical patterns and provides growth projections."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Category trends analysis completed successfully"
    )
    public ResponseEntity<MarketAnalysisResponseDto> getCategoryTrends(
            @Parameter(description = "Category to analyze trends for", example = "Web Development", required = true)
            @PathVariable String category,
            
            @Parameter(description = "Analysis timeframe", example = "30d")
            @RequestParam(defaultValue = "30d") String timeframe,
            
            @Parameter(description = "Sample size for analysis", example = "150")
            @RequestParam(defaultValue = "150") Integer sampleSize,
            
            @Parameter(description = "Geographic filter", example = "Remote")
            @RequestParam(required = false) String location) {
        
        log.info("Received category trends analysis request for: {} (timeframe: {})", category, timeframe);
        
        try {
            MarketAnalysisRequestDto request = MarketAnalysisRequestDto.builder()
                .skillOrCategory(category)
                .timeframe(timeframe)
                .sampleSize(Math.min(Math.max(sampleSize, 10), 500)) // Clamp between 10-500
                .analysisDepth("standard")
                .location(location)
                .build();
                
            MarketAnalysisResponseDto response = marketplaceAnalysisService.analyzeMarket(request);
            
            log.info("Category trends analysis completed for '{}' - Market health: {:.1f}%", 
                    category, response.getMarketMetrics().getMarketHealthScore());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in category trends analysis for {}: {}", category, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/health/{skill}")
    @Operation(
        summary = "Market health check",
        description = "Quick market health assessment for a specific skill or technology. " +
                     "Returns key health indicators and opportunity metrics."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Market health check completed successfully"
    )
    public ResponseEntity<MarketAnalysisResponseDto.MarketMetrics> getMarketHealth(
            @Parameter(description = "Skill or technology to check", example = "Python", required = true)
            @PathVariable String skill,
            
            @Parameter(description = "Sample size for health check", example = "50")
            @RequestParam(defaultValue = "50") Integer sampleSize) {
        
        log.info("Received market health check request for: {}", skill);
        
        try {
            MarketAnalysisRequestDto request = MarketAnalysisRequestDto.builder()
                .skillOrCategory(skill)
                .sampleSize(Math.min(Math.max(sampleSize, 10), 200)) // Smaller sample for health check
                .analysisDepth("basic")
                .build();
                
            MarketAnalysisResponseDto response = marketplaceAnalysisService.analyzeMarket(request);
            
            log.info("Market health check completed for '{}' - Health score: {:.1f}%", 
                    skill, response.getMarketMetrics().getMarketHealthScore());
            
            return ResponseEntity.ok(response.getMarketMetrics());
            
        } catch (Exception e) {
            log.error("Error in market health check for {}: {}", skill, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}