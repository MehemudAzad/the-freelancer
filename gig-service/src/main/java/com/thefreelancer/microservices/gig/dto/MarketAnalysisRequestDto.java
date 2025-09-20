package com.thefreelancer.microservices.gig.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for marketplace analysis")
public class MarketAnalysisRequestDto {
    
    @NotBlank(message = "Skill or category is required")
    @Schema(description = "Skill or category to analyze", example = "React development", required = true)
    private String skillOrCategory;
    
    @Min(value = 10, message = "Sample size must be at least 10")
    @Max(value = 500, message = "Sample size must not exceed 500")
    @Builder.Default
    @Schema(description = "Number of documents to analyze", example = "100", defaultValue = "100")
    private Integer sampleSize = 100;
    
    @Builder.Default
    @Schema(description = "Analysis timeframe", example = "30d", defaultValue = "30d", 
            allowableValues = {"7d", "30d", "90d", "180d", "365d"})
    private String timeframe = "30d";
    
    @Builder.Default
    @Schema(description = "Analysis depth level", example = "standard", defaultValue = "standard",
            allowableValues = {"basic", "standard", "deep"})
    private String analysisDepth = "standard";
    
    @Schema(description = "Geographic filter for analysis", example = "Remote")
    private String location;
    
    @Min(value = 0, message = "Similarity threshold must be between 0 and 1")
    @Max(value = 1, message = "Similarity threshold must be between 0 and 1")
    @Builder.Default
    @Schema(description = "Minimum similarity threshold for matching", example = "0.3", defaultValue = "0.3")
    private Double minSimilarityThreshold = 0.3;
}