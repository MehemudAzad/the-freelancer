package com.thefreelancer.microservices.gig.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Request DTO for finding matching freelancers for a job")
public class JobMatchingRequestDto {
    
    @NotNull(message = "Job ID is required")
    @Schema(description = "ID of the job", example = "123")
    private Long jobId;
    
    @NotBlank(message = "Job title is required")
    @Size(min = 5, max = 200, message = "Job title must be between 5 and 200 characters")
    @Schema(description = "Title of the job", example = "Full Stack Developer for E-commerce Platform")
    private String title;
    
    @NotBlank(message = "Job description is required")
    @Size(min = 50, message = "Job description must be at least 50 characters")
    @Schema(description = "Detailed description of the job requirements", 
            example = "We need an experienced full-stack developer to build a modern e-commerce platform using React, Node.js, and PostgreSQL. Must have experience with payment integrations and cloud deployment.")
    private String description;
    
    @Schema(description = "Required skills for the job", 
            example = "[\"React\", \"Node.js\", \"PostgreSQL\", \"JavaScript\", \"API Development\"]")
    private List<String> requiredSkills;
    
    @Schema(description = "Preferred skills (nice to have)", 
            example = "[\"AWS\", \"Docker\", \"TypeScript\", \"Redis\"]")
    private List<String> preferredSkills;
    
    @Min(value = 1, message = "Budget must be positive")
    @Schema(description = "Budget for the job in cents", example = "500000")
    private Long budgetCents;
    
    @Schema(description = "Currency code", example = "USD")
    private String currency;
    
    @Schema(description = "Job category", example = "Web Development")
    private String category;
    
    @Schema(description = "Project duration in days", example = "30")
    private Integer durationDays;
    
    @Min(value = 1, message = "Limit must be at least 1")
    @Schema(description = "Maximum number of results to return", example = "20")
    @Builder.Default
    private Integer limit = 20;
    
    @Schema(description = "Minimum similarity threshold (0.0 to 1.0)", example = "0.6")
    @Builder.Default
    private Double minSimilarityScore = 0.6;
    
    @Schema(description = "Minimum freelancer rating", example = "4.0")
    private Double minRating;
    
    @Schema(description = "Maximum hourly rate in cents", example = "10000")
    private Long maxHourlyRateCents;
    
    @Schema(description = "Required availability", example = "FULL_TIME", allowableValues = {"FULL_TIME", "PART_TIME", "OCCASIONAL"})
    private String availability;
    
    @Schema(description = "Preferred locations/countries", example = "[\"United States\", \"Canada\", \"Remote\"]")
    private List<String> preferredLocations;
    
    @Schema(description = "Whether to include only verified freelancers", example = "false")
    @Builder.Default
    private Boolean verifiedOnly = false;
    
    @Schema(description = "Minimum delivery score (0-100)", example = "85.0")
    private Double minDeliveryScore;
}