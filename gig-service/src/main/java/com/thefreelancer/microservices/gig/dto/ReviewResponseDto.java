package com.thefreelancer.microservices.gig.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
    
    private Long id;
    private Long gigId;
    private String gigTitle; // Populated from Gig service
    private Long freelancerId;
    private String freelancerName; // Populated from User service
    private String freelancerHandle; // Populated from User service
    private Long reviewerId;
    private String reviewerName; // Populated from User service (or Anonymous)
    private String reviewerHandle; // Populated from User service (or Anonymous)
    private String jobId;
    private String contractId;
    
    // Ratings
    private Integer overallRating;
    private Integer qualityRating;
    private Integer communicationRating;
    private Integer timelinessRating;
    private Integer professionalismRating;
    private Double averageRating; // Calculated average
    
    // Review content
    private String title;
    private String comment;
    private String reviewType;
    private String status;
    private Boolean isAnonymous;
    private Boolean wouldRecommend;
    
    // Freelancer response
    private String freelancerResponse;
    private LocalDateTime responseDate;
    
    // Helpful votes
    private Integer helpfulCount;
    private Integer unhelpfulCount;
    private Integer totalHelpfulVotes;
    private Double helpfulPercentage;
    
    // Metadata
    private Boolean isFlagged;
    private String flagReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Helper flags for UI
    private Boolean canEdit; // Can current user edit this review
    private Boolean canRespond; // Can current user respond to this review
    private Boolean isRecent; // Is this review recent (within 30 days)
}
