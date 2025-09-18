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
public class RecommendationResponseDto {
    
    private Long userId;
    private List<JobRecommendationDto> recommendations;
    private Integer totalRecommendations;
    private Double averageMatchScore;
    private String message; // Any relevant message about the recommendations
}