package com.thefreelancer.microservices.gig.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for gig semantic search")
public class GigSearchResponseDto {
    
    @Schema(description = "Found gigs matching the search criteria")
    private List<GigResult> gigs;
    
    @Schema(description = "Total number of gigs found", example = "45")
    private Integer totalFound;
    
    @Schema(description = "Original search query", example = "React developer")
    private String query;
    
    @Schema(description = "AI-enhanced search query", example = "react reactjs next.js jsx frontend web development")
    private String enhancedQuery;
    
    @Schema(description = "Search processing time in milliseconds", example = "180")
    private Long processingTimeMs;
    
    @Schema(description = "Filter application summary")
    private Map<String, Object> filterSummary;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Individual gig search result")
    public static class GigResult {
        
        @Schema(description = "Gig ID", example = "123")
        private String id;
        
        @Schema(description = "Gig title", example = "React E-commerce Development")
        private String title;
        
        @Schema(description = "Gig description", example = "I will build modern e-commerce platforms...")
        private String description;
        
        @Schema(description = "Freelancer profile ID", example = "456")
        private Long profileId;
        
        @Schema(description = "Gig category", example = "Web Development")
        private String category;
        
        @Schema(description = "Gig tags/skills", example = "[\"React\", \"Node.js\", \"E-commerce\"]")
        private List<String> tags;
        
        @Schema(description = "Average rating", example = "4.8")
        private Double reviewAvg;
        
        @Schema(description = "Number of reviews", example = "23")
        private Integer reviewsCount;
        
        @Schema(description = "AI similarity score (0-1)", example = "0.89")
        private Double similarityScore;
        
        @Schema(description = "Gig creation date")
        private LocalDateTime createdAt;
        
        @Schema(description = "Gig packages (pricing tiers)")
        private List<PackageInfo> packages;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Gig package information")
    public static class PackageInfo {
        
        @Schema(description = "Package ID", example = "pkg1")
        private String id;
        
        @Schema(description = "Package tier", example = "BASIC", allowableValues = {"BASIC", "STANDARD", "PREMIUM"})
        private String tier;
        
        @Schema(description = "Package title", example = "Basic E-commerce Site")
        private String title;
        
        @Schema(description = "Package price in cents", example = "150000")
        private Long price;
        
        @Schema(description = "Currency code", example = "USD")
        private String currency;
        
        @Schema(description = "Delivery time in days", example = "14")
        private Integer deliveryDays;
        
        @Schema(description = "Number of revisions included", example = "2")
        private Integer revisions;
    }
}