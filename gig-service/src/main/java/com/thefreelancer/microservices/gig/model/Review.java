package com.thefreelancer.microservices.gig.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_gig_reviews", columnList = "gig_id, created_at"),
    @Index(name = "idx_reviewer_reviews", columnList = "reviewer_id, created_at"),
    @Index(name = "idx_freelancer_reviews", columnList = "freelancer_id, created_at"),
    @Index(name = "idx_gig_rating", columnList = "gig_id, overall_rating"),
    @Index(name = "idx_freelancer_rating", columnList = "freelancer_id, overall_rating")
})
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "gig_id", nullable = false)
    private Long gigId; // References Gig.id
    
    @Column(name = "freelancer_id", nullable = false)
    private Long freelancerId; // References User.id from auth-service (gig owner)
    
    @Column(name = "reviewer_id", nullable = false)  
    private Long reviewerId; // References User.id from auth-service (client who hired)
    
    @Column(name = "job_id")
    private String jobId; // References job from job-proposal-service (if applicable)
    
    @Column(name = "contract_id")
    private String contractId; // References contract from job-proposal-service (if applicable)
    
    // Rating categories (1-5 scale)
    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating; // 1-5 stars
    
    @Column(name = "quality_rating", nullable = false)
    private Integer qualityRating; // Quality of work
    
    @Column(name = "communication_rating", nullable = false)
    private Integer communicationRating; // Communication skills
    
    @Column(name = "timeliness_rating", nullable = false)
    private Integer timelinessRating; // Meeting deadlines
    
    @Column(name = "professionalism_rating", nullable = false)
    private Integer professionalismRating; // Professional behavior
    
    // Review content
    @Column(name = "title")
    private String title; // Short title for the review
    
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment; // Detailed review text
    
    // Review metadata
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReviewType reviewType = ReviewType.GIG_REVIEW;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PUBLISHED;
    
    @Column(name = "is_anonymous")
    @Builder.Default
    private Boolean isAnonymous = false;
    
    @Column(name = "would_recommend")
    @Builder.Default
    private Boolean wouldRecommend = true;
    
    // Response from freelancer
    @Column(name = "freelancer_response", columnDefinition = "TEXT")
    private String freelancerResponse;
    
    @Column(name = "response_date")
    private LocalDateTime responseDate;
    
    // Helpful votes (like/dislike system)
    @Column(name = "helpful_count")
    @Builder.Default
    private Integer helpfulCount = 0;
    
    @Column(name = "unhelpful_count")  
    @Builder.Default
    private Integer unhelpfulCount = 0;
    
    // Moderation
    @Column(name = "is_flagged")
    @Builder.Default
    private Boolean isFlagged = false;
    
    @Column(name = "flag_reason")
    private String flagReason;
    
    @Column(name = "moderated_by")
    private Long moderatedBy; // Admin user id
    
    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Calculated fields
    public Double getAverageRating() {
        return (qualityRating + communicationRating + timelinessRating + professionalismRating) / 4.0;
    }
    
    public Integer getTotalHelpfulVotes() {
        return helpfulCount + unhelpfulCount;
    }
    
    public Double getHelpfulPercentage() {
        int total = getTotalHelpfulVotes();
        if (total == 0) return 0.0;
        return (helpfulCount.doubleValue() / total) * 100;
    }
    
    // Helper methods
    public boolean canBeEditedBy(Long userId) {
        return reviewerId.equals(userId) && 
               status == ReviewStatus.PUBLISHED &&
               createdAt.isAfter(LocalDateTime.now().minusHours(24)); // 24h edit window
    }
    
    public boolean canRespondBy(Long userId) {
        return freelancerId.equals(userId) && 
               status == ReviewStatus.PUBLISHED &&
               freelancerResponse == null;
    }
    
    public boolean isRecent() {
        return createdAt.isAfter(LocalDateTime.now().minusDays(30));
    }
    
    public enum ReviewType {
        GIG_REVIEW,     // Review for a specific gig
        PROFILE_REVIEW, // General profile review
        JOB_REVIEW      // Review after completing a job
    }
    
    public enum ReviewStatus {
        DRAFT,          // Review being written
        PUBLISHED,      // Published and visible
        HIDDEN,         // Hidden by moderator
        DELETED,        // Soft deleted
        PENDING_REVIEW  // Awaiting moderation
    }
}
