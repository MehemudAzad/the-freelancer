package com.thefreelancer.microservices.gig.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryDto {
    
    // Overall statistics
    private Long totalReviews;
    private Double averageRating;
    private Double averageQualityRating;
    private Double averageCommunicationRating;
    private Double averageTimelinessRating;
    private Double averageProfessionalismRating;
    
    // Rating distribution (1-5 stars)
    private Map<Integer, Long> ratingDistribution; // e.g., {5: 120, 4: 80, 3: 20, 2: 5, 1: 2}
    
    // Recommendation percentage
    private Double recommendationPercentage;
    
    // Recent review statistics
    private Long recentReviewsCount; // Reviews in last 30 days
    private Double recentAverageRating; // Average rating for recent reviews
    
    // Content statistics
    private Long reviewsWithComments;
    private Long reviewsWithResponse; // Reviews that have freelancer responses
    
    // Helper methods for display
    public String getFormattedAverageRating() {
        if (averageRating == null) return "0.0";
        return String.format("%.1f", averageRating);
    }
    
    public Integer getRecommendationPercentageInt() {
        if (recommendationPercentage == null) return 0;
        return recommendationPercentage.intValue();
    }
    
    public Long getRatingCount(Integer stars) {
        if (ratingDistribution == null) return 0L;
        return ratingDistribution.getOrDefault(stars, 0L);
    }
    
    public Double getRatingPercentage(Integer stars) {
        if (ratingDistribution == null || totalReviews == 0) return 0.0;
        Long count = getRatingCount(stars);
        return (count.doubleValue() / totalReviews) * 100;
    }
}
