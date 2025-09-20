package com.thefreelancer.microservices.gig.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO containing matching freelancers for a job")
public class JobMatchingResponseDto {
    
    @Schema(description = "ID of the job", example = "123")
    private Long jobId;
    
    @Schema(description = "List of matching freelancers")
    private List<MatchedFreelancerDto> matchedFreelancers;
    
    @Schema(description = "Total number of matches found", example = "15")
    private Integer totalMatches;
    
    @Schema(description = "Average match score of returned results", example = "0.82")
    private Double averageMatchScore;
    
    @Schema(description = "Search query used for matching", example = "React Node.js PostgreSQL full stack developer")
    private String searchQuery;
    
    @Schema(description = "Processing time in milliseconds", example = "245")
    private Long processingTimeMs;
    
    @Schema(description = "Applied filters summary")
    private FiltersSummaryDto appliedFilters;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Details of a matched freelancer")
    public static class MatchedFreelancerDto {
        
        @Schema(description = "User ID of the freelancer", example = "456")
        private Long userId;
        
        @Schema(description = "Freelancer's headline", example = "Senior Full Stack Developer | React & Node.js Expert")
        private String headline;
        
        @Schema(description = "Freelancer's bio/summary")
        private String bio;
        
        @Schema(description = "Freelancer's skills")
        private List<String> skills;
        
        @Schema(description = "Similarity match score (0.0 to 1.0)", example = "0.87")
        private Double matchScore;
        
        @Schema(description = "Hourly rate in cents", example = "8500")
        private Long hourlyRateCents;
        
        @Schema(description = "Currency", example = "USD")
        private String currency;
        
        @Schema(description = "Location", example = "Toronto, Canada")
        private String locationText;
        
        @Schema(description = "Availability", example = "FULL_TIME")
        private String availability;
        
        @Schema(description = "Average rating", example = "4.8")
        private Double reviewAvg;
        
        @Schema(description = "Number of reviews", example = "35")
        private Integer reviewsCount;
        
        @Schema(description = "Delivery score (0-100)", example = "92.1")
        private Double deliveryScore;
        
        @Schema(description = "Profile picture URL")
        private String profilePictureUrl;
        
        @Schema(description = "Portfolio website URL")
        private String websiteUrl;
        
        @Schema(description = "LinkedIn profile URL")
        private String linkedinUrl;
        
        @Schema(description = "GitHub username")
        private String githubUsername;
        
        @Schema(description = "Matching skills breakdown")
        private List<SkillMatchDto> skillMatches;
        
        @Schema(description = "Match explanation")
        private String matchReason;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Skill match details")
    public static class SkillMatchDto {
        
        @Schema(description = "Skill name", example = "React")
        private String skill;
        
        @Schema(description = "Whether this skill was required", example = "true")
        private Boolean required;
        
        @Schema(description = "Match confidence (0.0 to 1.0)", example = "0.95")
        private Double confidence;
        
        @Schema(description = "Freelancer's proficiency level", example = "Expert")
        private String proficiencyLevel;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Summary of applied filters")
    public static class FiltersSummaryDto {
        
        @Schema(description = "Minimum similarity score used", example = "0.6")
        private Double minSimilarityScore;
        
        @Schema(description = "Minimum rating filter", example = "4.0")
        private Double minRating;
        
        @Schema(description = "Maximum hourly rate filter in cents", example = "10000")
        private Long maxHourlyRateCents;
        
        @Schema(description = "Availability filter", example = "FULL_TIME")
        private String availability;
        
        @Schema(description = "Number of freelancers filtered out", example = "25")
        private Integer filteredOutCount;
        
        @Schema(description = "Total freelancers in database", example = "150")
        private Integer totalFreelancersInDatabase;
    }
}