package com.thefreelancer.microservices.gig.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequestDto {
    
    private Long userId;
    private Integer limit; // Number of recommendations to return (default 10)
    private Double minMatchScore; // Minimum similarity score threshold (default 0.1)
    private List<String> excludeCategories; // Categories to exclude from recommendations
    private List<String> preferredCategories; // Preferred job categories
}