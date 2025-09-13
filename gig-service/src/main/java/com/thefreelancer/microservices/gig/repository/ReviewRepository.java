package com.thefreelancer.microservices.gig.repository;

import com.thefreelancer.microservices.gig.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Find reviews by gig
    Page<Review> findByGigIdAndStatusOrderByCreatedAtDesc(Long gigId, Review.ReviewStatus status, Pageable pageable);
    
    // Find reviews by freelancer
    Page<Review> findByFreelancerIdAndStatusOrderByCreatedAtDesc(Long freelancerId, Review.ReviewStatus status, Pageable pageable);
    
    // Find reviews by reviewer
    Page<Review> findByReviewerIdAndStatusOrderByCreatedAtDesc(Long reviewerId, Review.ReviewStatus status, Pageable pageable);
    
    // Find review by specific gig and reviewer (to prevent duplicate reviews)
    Optional<Review> findByGigIdAndReviewerId(Long gigId, Long reviewerId);
    
    // Rating aggregation queries for gigs
    @Query("SELECT AVG(r.overallRating) FROM Review r WHERE r.gigId = :gigId AND r.status = 'PUBLISHED'")
    Double findAverageRatingByGigId(@Param("gigId") Long gigId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.gigId = :gigId AND r.status = 'PUBLISHED'")
    Long countReviewsByGigId(@Param("gigId") Long gigId);
    
    // Rating aggregation queries for freelancers
    @Query("SELECT AVG(r.overallRating) FROM Review r WHERE r.freelancerId = :freelancerId AND r.status = 'PUBLISHED'")
    Double findAverageRatingByFreelancerId(@Param("freelancerId") Long freelancerId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.freelancerId = :freelancerId AND r.status = 'PUBLISHED'")
    Long countReviewsByFreelancerId(@Param("freelancerId") Long freelancerId);
    
    // Detailed rating statistics for gigs
    @Query("SELECT AVG(r.qualityRating) FROM Review r WHERE r.gigId = :gigId AND r.status = 'PUBLISHED'")
    Double findAverageQualityRatingByGigId(@Param("gigId") Long gigId);
    
    @Query("SELECT AVG(r.communicationRating) FROM Review r WHERE r.gigId = :gigId AND r.status = 'PUBLISHED'")
    Double findAverageCommunicationRatingByGigId(@Param("gigId") Long gigId);
    
    @Query("SELECT AVG(r.timelinessRating) FROM Review r WHERE r.gigId = :gigId AND r.status = 'PUBLISHED'")
    Double findAverageTimelinessRatingByGigId(@Param("gigId") Long gigId);
    
    @Query("SELECT AVG(r.professionalismRating) FROM Review r WHERE r.gigId = :gigId AND r.status = 'PUBLISHED'")
    Double findAverageProfessionalismRatingByGigId(@Param("gigId") Long gigId);
    
    // Detailed rating statistics for freelancers
    @Query("SELECT AVG(r.qualityRating) FROM Review r WHERE r.freelancerId = :freelancerId AND r.status = 'PUBLISHED'")
    Double findAverageQualityRatingByFreelancerId(@Param("freelancerId") Long freelancerId);
    
    @Query("SELECT AVG(r.communicationRating) FROM Review r WHERE r.freelancerId = :freelancerId AND r.status = 'PUBLISHED'")
    Double findAverageCommunicationRatingByFreelancerId(@Param("freelancerId") Long freelancerId);
    
    @Query("SELECT AVG(r.timelinessRating) FROM Review r WHERE r.freelancerId = :freelancerId AND r.status = 'PUBLISHED'")
    Double findAverageTimelinessRatingByFreelancerId(@Param("freelancerId") Long freelancerId);
    
    @Query("SELECT AVG(r.professionalismRating) FROM Review r WHERE r.freelancerId = :freelancerId AND r.status = 'PUBLISHED'")
    Double findAverageProfessionalismRatingByFreelancerId(@Param("freelancerId") Long freelancerId);
    
    // Rating distribution queries
    @Query("SELECT r.overallRating, COUNT(r) FROM Review r WHERE r.gigId = :gigId AND r.status = 'PUBLISHED' GROUP BY r.overallRating")
    List<Object[]> findRatingDistributionByGigId(@Param("gigId") Long gigId);
    
    @Query("SELECT r.overallRating, COUNT(r) FROM Review r WHERE r.freelancerId = :freelancerId AND r.status = 'PUBLISHED' GROUP BY r.overallRating")
    List<Object[]> findRatingDistributionByFreelancerId(@Param("freelancerId") Long freelancerId);
    
    // Recommendation statistics
    @Query("SELECT COUNT(r) FROM Review r WHERE r.gigId = :gigId AND r.status = 'PUBLISHED' AND r.wouldRecommend = true")
    Long countRecommendationsByGigId(@Param("gigId") Long gigId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.freelancerId = :freelancerId AND r.status = 'PUBLISHED' AND r.wouldRecommend = true")
    Long countRecommendationsByFreelancerId(@Param("freelancerId") Long freelancerId);
    
    // Recent reviews (last 30 days)
    @Query("SELECT COUNT(r) FROM Review r WHERE r.gigId = :gigId AND r.status = 'PUBLISHED' AND r.createdAt >= :fromDate")
    Long countRecentReviewsByGigId(@Param("gigId") Long gigId, @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT AVG(r.overallRating) FROM Review r WHERE r.gigId = :gigId AND r.status = 'PUBLISHED' AND r.createdAt >= :fromDate")
    Double findRecentAverageRatingByGigId(@Param("gigId") Long gigId, @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.freelancerId = :freelancerId AND r.status = 'PUBLISHED' AND r.createdAt >= :fromDate")
    Long countRecentReviewsByFreelancerId(@Param("freelancerId") Long freelancerId, @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT AVG(r.overallRating) FROM Review r WHERE r.freelancerId = :freelancerId AND r.status = 'PUBLISHED' AND r.createdAt >= :fromDate")
    Double findRecentAverageRatingByFreelancerId(@Param("freelancerId") Long freelancerId, @Param("fromDate") LocalDateTime fromDate);
    
    // Reviews with comments and responses
    @Query("SELECT COUNT(r) FROM Review r WHERE r.gigId = :gigId AND r.status = 'PUBLISHED' AND r.comment IS NOT NULL AND r.comment != ''")
    Long countReviewsWithCommentsByGigId(@Param("gigId") Long gigId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.gigId = :gigId AND r.status = 'PUBLISHED' AND r.freelancerResponse IS NOT NULL AND r.freelancerResponse != ''")
    Long countReviewsWithResponseByGigId(@Param("gigId") Long gigId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.freelancerId = :freelancerId AND r.status = 'PUBLISHED' AND r.comment IS NOT NULL AND r.comment != ''")
    Long countReviewsWithCommentsByFreelancerId(@Param("freelancerId") Long freelancerId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.freelancerId = :freelancerId AND r.status = 'PUBLISHED' AND r.freelancerResponse IS NOT NULL AND r.freelancerResponse != ''")
    Long countReviewsWithResponseByFreelancerId(@Param("freelancerId") Long freelancerId);
    
    // Search and filtering
    @Query("SELECT r FROM Review r WHERE r.gigId = :gigId AND r.status = 'PUBLISHED' AND " +
           "(:rating IS NULL OR r.overallRating = :rating) AND " +
           "(:searchTerm IS NULL OR LOWER(r.comment) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY r.createdAt DESC")
    Page<Review> findGigReviewsWithFilters(
        @Param("gigId") Long gigId,
        @Param("rating") Integer rating,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );
    
    @Query("SELECT r FROM Review r WHERE r.freelancerId = :freelancerId AND r.status = 'PUBLISHED' AND " +
           "(:rating IS NULL OR r.overallRating = :rating) AND " +
           "(:searchTerm IS NULL OR LOWER(r.comment) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY r.createdAt DESC")
    Page<Review> findFreelancerReviewsWithFilters(
        @Param("freelancerId") Long freelancerId,
        @Param("rating") Integer rating,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );
    
    // Most helpful reviews
    @Query("SELECT r FROM Review r WHERE r.gigId = :gigId AND r.status = 'PUBLISHED' " +
           "ORDER BY (r.helpfulCount - r.unhelpfulCount) DESC, r.createdAt DESC")
    Page<Review> findMostHelpfulReviewsByGigId(@Param("gigId") Long gigId, Pageable pageable);
    
    // Flagged reviews (for moderation)
    Page<Review> findByIsFlaggedTrueOrderByCreatedAtDesc(Pageable pageable);
    
    // Reviews pending moderation
    Page<Review> findByStatusOrderByCreatedAtDesc(Review.ReviewStatus status, Pageable pageable);
    
    // Latest reviews for a freelancer
    List<Review> findTop5ByFreelancerIdAndStatusOrderByCreatedAtDesc(Long freelancerId, Review.ReviewStatus status);
    
    // Check if user has already reviewed a gig
    boolean existsByGigIdAndReviewerId(Long gigId, Long reviewerId);
    
    // Profile rating calculations
    @Query("SELECT AVG(r.overallRating) FROM Review r WHERE r.freelancerId = :freelancerId AND r.status = 'PUBLISHED'")
    Double calculateAverageRatingForFreelancer(@Param("freelancerId") Long freelancerId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.freelancerId = :freelancerId AND r.status = 'PUBLISHED'")
    Long countReviewsForFreelancer(@Param("freelancerId") Long freelancerId);
}
