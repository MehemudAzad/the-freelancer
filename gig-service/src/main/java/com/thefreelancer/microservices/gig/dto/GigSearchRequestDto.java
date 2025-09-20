package com.thefreelancer.microservices.gig.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for gig semantic search")
public class GigSearchRequestDto {
    
    @Schema(description = "Natural language search query", example = "React developer for e-commerce")
    private String query;
    
    @Schema(description = "Required technical skills", example = "[\"React\", \"Node.js\", \"JavaScript\"]")
    private List<String> skills;
    
    @Schema(description = "Gig category filter", example = "Web Development")
    private String category;
    
    @Schema(description = "Minimum package price in cents", example = "100000")
    @Min(0)
    private Long minPrice;
    
    @Schema(description = "Maximum package price in cents", example = "500000") 
    @Min(0)
    private Long maxPrice;
    
    @Schema(description = "Minimum gig rating", example = "4.0")
    @Min(0)
    @Max(5)
    private Double minRating;
    
    @Schema(description = "Maximum gig rating", example = "5.0")
    @Min(0)
    @Max(5)
    private Double maxRating;
    
    @Schema(description = "Minimum number of reviews", example = "5")
    @Min(0)
    private Integer minReviews;
    
    @Schema(description = "Sort field", example = "relevance", allowableValues = {"relevance", "price", "rating", "date"})
    @Builder.Default
    private String sortBy = "relevance";
    
    @Schema(description = "Sort order", example = "desc", allowableValues = {"asc", "desc"})
    @Builder.Default
    private String sortOrder = "desc";
    
    @Schema(description = "Number of results to return", example = "20")
    @Min(1)
    @Max(100)
    @Builder.Default
    private Integer limit = 20;
    
    @Schema(description = "Number of results to skip", example = "0")
    @Min(0)
    @Builder.Default
    private Integer offset = 0;
}