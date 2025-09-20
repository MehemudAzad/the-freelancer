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
@Schema(description = "Response DTO containing comprehensive marketplace analysis")
public class MarketAnalysisResponseDto {
    
    @Schema(description = "Original search query", example = "React development")
    private String query;
    
    @Schema(description = "Enhanced query used for vector search")
    private String enhancedQuery;
    
    @Schema(description = "Demand-side market analysis")
    private DemandAnalysis demandAnalysis;
    
    @Schema(description = "Supply-side market analysis") 
    private SupplyAnalysis supplyAnalysis;
    
    @Schema(description = "Key market metrics and ratios")
    private MarketMetrics marketMetrics;
    
    @Schema(description = "Strategic recommendations")
    private Recommendations recommendations;
    
    @Schema(description = "Competitive landscape analysis")
    private CompetitiveLandscape competitiveLandscape;
    
    @Schema(description = "Analysis timestamp")
    private LocalDateTime analysisDate;
    
    @Schema(description = "Processing time in milliseconds", example = "1250")
    private Long processingTimeMs;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Market demand analysis from job postings")
    public static class DemandAnalysis {
        
        @Schema(description = "Total job postings found", example = "45")
        private Integer totalJobsFound;
        
        @Schema(description = "Average job budget in cents", example = "250000")
        private Double averageBudget;
        
        @Schema(description = "Skills demand breakdown")
        private Map<String, Long> skillDemandBreakdown;
        
        @Schema(description = "Job category distribution")
        private Map<String, Long> categoryDistribution;
        
        @Schema(description = "Job urgency level distribution")
        private Map<String, Integer> urgencyLevels;
        
        @Schema(description = "Demand trends over time")
        private Map<String, Integer> demandTrends;
        
        @Schema(description = "Most requested skills")
        private List<String> topRequiredSkills;
        
        @Schema(description = "Budget range distribution")
        private Map<String, Integer> budgetRanges;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Market supply analysis from gigs and profiles")
    public static class SupplyAnalysis {
        
        @Schema(description = "Total gigs found", example = "128")
        private Integer totalGigsFound;
        
        @Schema(description = "Total freelancers found", example = "89")
        private Integer totalFreelancersFound;
        
        @Schema(description = "Average gig price in cents", example = "180000")
        private Double averageGigPrice;
        
        @Schema(description = "Average freelancer hourly rate in cents", example = "7500")
        private Double averageFreelancerHourlyRate;
        
        @Schema(description = "Skills supply breakdown")
        private Map<String, Long> skillSupplyBreakdown;
        
        @Schema(description = "Experience level distribution")
        private Map<String, Double> experienceLevels;
        
        @Schema(description = "Freelancer availability distribution")
        private Map<String, Integer> availabilityDistribution;
        
        @Schema(description = "Most offered skills")
        private List<String> topOfferedSkills;
        
        @Schema(description = "Price range distribution")
        private Map<String, Integer> priceRanges;
        
        @Schema(description = "Quality metrics (ratings, reviews)")
        private Map<String, Object> qualityMetrics;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Key market metrics and health indicators")
    public static class MarketMetrics {
        
        @Schema(description = "Supply to demand ratio", example = "1.35")
        private Double supplyDemandRatio;
        
        @Schema(description = "Market health score (0-100)", example = "78.5")
        private Double marketHealthScore;
        
        @Schema(description = "Competition index (0-100)", example = "65.2")
        private Double competitionIndex;
        
        @Schema(description = "Opportunity score (0-100)", example = "82.1")
        private Double opportunityScore;
        
        @Schema(description = "Price gap between demand and supply in cents", example = "70000")
        private Double priceGap;
        
        @Schema(description = "Price gap as percentage", example = "38.9")
        private Double priceGapPercentage;
        
        @Schema(description = "Market maturity level", example = "Growing")
        private String marketMaturity;
        
        @Schema(description = "Growth potential assessment", example = "High")
        private String growthPotential;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Strategic recommendations for different stakeholders")
    public static class Recommendations {
        
        @Schema(description = "Recommendations for freelancers")
        private List<String> forFreelancers;
        
        @Schema(description = "Recommendations for clients")
        private List<String> forClients;
        
        @Schema(description = "Recommendations for platform")
        private List<String> forPlatform;
        
        @Schema(description = "Optimal pricing strategies")
        private Map<String, Object> optimalPricing;
        
        @Schema(description = "Market entry strategy recommendations")
        private List<String> marketEntryStrategy;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Competitive landscape and market positioning analysis")
    public static class CompetitiveLandscape {
        
        @Schema(description = "Total competitors identified", example = "67")
        private Integer totalCompetitors;
        
        @Schema(description = "Top skills among competitors")
        private Map<String, Long> topCompetitorSkills;
        
        @Schema(description = "Average competitor rating", example = "4.6")
        private Double averageCompetitorRating;
        
        @Schema(description = "Competition intensity (0-100)", example = "72.3")
        private Double competitionIntensity;
        
        @Schema(description = "Market leaders information")
        private List<Map<String, Object>> marketLeaders;
        
        @Schema(description = "Identified skill gaps in the market")
        private List<String> skillGaps;
    }
}