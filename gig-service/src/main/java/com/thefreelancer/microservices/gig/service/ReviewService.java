package com.thefreelancer.microservices.gig.service;

import com.thefreelancer.microservices.gig.dto.*;
import com.thefreelancer.microservices.gig.mapper.ReviewMapper;
import com.thefreelancer.microservices.gig.model.Review;
import com.thefreelancer.microservices.gig.repository.GigRepository;
import com.thefreelancer.microservices.gig.repository.ProfileRepository;
import com.thefreelancer.microservices.gig.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final GigRepository gigRepository;
    private final ProfileRepository profileRepository;
    private final ReviewMapper reviewMapper;
    private final GigService gigService; // For updating gig ratings
    private final ProfileService profileService; // For updating profile ratings
    
    @Transactional
    public ReviewResponseDto createReview(ReviewCreateDto createDto, Long reviewerId) {
        log.info("Creating review for gig: {} by user: {}", createDto.getGigId(), reviewerId);
        
        // Validate that the gig exists
        if (!gigRepository.existsById(createDto.getGigId())) {
            throw new IllegalArgumentException("Gig not found with ID: " + createDto.getGigId());
        }
        
        // Check if the user has already reviewed this gig
        if (reviewRepository.existsByGigIdAndReviewerId(createDto.getGigId(), reviewerId)) {
            throw new IllegalArgumentException("You have already reviewed this gig");
        }
        
        // Validate that the reviewer is not the freelancer
        if (createDto.getFreelancerId().equals(reviewerId)) {
            throw new IllegalArgumentException("You cannot review your own gig");
        }
        
        // Create and save the review
        Review review = reviewMapper.toEntity(createDto, reviewerId);
        Review savedReview = reviewRepository.save(review);
        
        // Update gig ratings asynchronously
        updateGigRatings(createDto.getGigId());
        updateFreelancerRatings(createDto.getFreelancerId());
        
        log.info("Review created successfully with ID: {}", savedReview.getId());
        return reviewMapper.toResponseDto(savedReview, reviewerId);
    }
    
    @Transactional(readOnly = true)
    public ReviewPageResponseDto getGigReviews(Long gigId, int page, int size, String sortBy, Integer rating, String search) {
        log.info("Getting reviews for gig: {}, page: {}, size: {}", gigId, page, size);
        
        if (!gigRepository.existsById(gigId)) {
            throw new IllegalArgumentException("Gig not found with ID: " + gigId);
        }
        
        // Create pageable with sorting
        Sort sort = createSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get reviews with filters
        Page<Review> reviewsPage;
        if (rating != null || search != null) {
            reviewsPage = reviewRepository.findGigReviewsWithFilters(gigId, rating, search, pageable);
        } else {
            reviewsPage = reviewRepository.findByGigIdAndStatusOrderByCreatedAtDesc(gigId, Review.ReviewStatus.PUBLISHED, pageable);
        }
        
        List<ReviewResponseDto> reviewDtos = reviewsPage.getContent().stream()
            .map(reviewMapper::toResponseDto)
            .collect(Collectors.toList());
        
        // TODO: Populate user names from auth service
        
        // Get review summary
        ReviewSummaryDto summary = getGigReviewSummary(gigId);
        
        return ReviewPageResponseDto.builder()
            .reviews(reviewDtos)
            .currentPage(page)
            .pageSize(size)
            .totalElements(reviewsPage.getTotalElements())
            .totalPages(reviewsPage.getTotalPages())
            .hasNext(reviewsPage.hasNext())
            .hasPrevious(reviewsPage.hasPrevious())
            .summary(summary)
            .build();
    }
    
    @Transactional(readOnly = true)
    public ReviewPageResponseDto getFreelancerReviews(Long freelancerId, int page, int size, String sortBy, Integer rating, String search) {
        log.info("Getting reviews for freelancer: {}, page: {}, size: {}", freelancerId, page, size);
        
        if (!profileRepository.existsById(freelancerId)) {
            throw new IllegalArgumentException("Freelancer profile not found with ID: " + freelancerId);
        }
        
        Sort sort = createSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Review> reviewsPage;
        if (rating != null || search != null) {
            reviewsPage = reviewRepository.findFreelancerReviewsWithFilters(freelancerId, rating, search, pageable);
        } else {
            reviewsPage = reviewRepository.findByFreelancerIdAndStatusOrderByCreatedAtDesc(freelancerId, Review.ReviewStatus.PUBLISHED, pageable);
        }
        
        List<ReviewResponseDto> reviewDtos = reviewsPage.getContent().stream()
            .map(reviewMapper::toResponseDto)
            .collect(Collectors.toList());
        
        ReviewSummaryDto summary = getFreelancerReviewSummary(freelancerId);
        
        return ReviewPageResponseDto.builder()
            .reviews(reviewDtos)
            .currentPage(page)
            .pageSize(size)
            .totalElements(reviewsPage.getTotalElements())
            .totalPages(reviewsPage.getTotalPages())
            .hasNext(reviewsPage.hasNext())
            .hasPrevious(reviewsPage.hasPrevious())
            .summary(summary)
            .build();
    }
    
    @Transactional(readOnly = true)
    public ReviewResponseDto getReviewById(Long reviewId, Long currentUserId) {
        log.info("Getting review by ID: {}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));
        
        return reviewMapper.toResponseDto(review, currentUserId);
    }
    
    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, ReviewUpdateDto updateDto, Long userId) {
        log.info("Updating review: {} by user: {}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));
        
        // Check permissions
        if (!review.canBeEditedBy(userId)) {
            throw new IllegalArgumentException("You cannot edit this review");
        }
        
        // Update the review
        Review updatedReview = reviewMapper.updateEntity(review, updateDto);
        Review savedReview = reviewRepository.save(updatedReview);
        
        // Update ratings if rating changed
        updateGigRatings(savedReview.getGigId());
        updateFreelancerRatings(savedReview.getFreelancerId());
        
        log.info("Review updated successfully: {}", reviewId);
        return reviewMapper.toResponseDto(savedReview, userId);
    }
    
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        log.info("Deleting review: {} by user: {}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));
        
        // Check permissions (only reviewer or admin can delete)
        if (!review.getReviewerId().equals(userId)) {
            throw new IllegalArgumentException("You cannot delete this review");
        }
        
        Long gigId = review.getGigId();
        Long freelancerId = review.getFreelancerId();
        
        // Soft delete by changing status
        review.setStatus(Review.ReviewStatus.DELETED);
        reviewRepository.save(review);
        
        // Update ratings
        updateGigRatings(gigId);
        updateFreelancerRatings(freelancerId);
        
        log.info("Review deleted successfully: {}", reviewId);
    }
    
    @Transactional
    public ReviewResponseDto addFreelancerResponse(Long reviewId, ReviewResponseCreateDto responseDto, Long freelancerId) {
        log.info("Adding freelancer response to review: {} by freelancer: {}", reviewId, freelancerId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));
        
        // Check permissions
        if (!review.canRespondBy(freelancerId)) {
            throw new IllegalArgumentException("You cannot respond to this review");
        }
        
        Review updatedReview = reviewMapper.addResponse(review, responseDto.getResponse());
        Review savedReview = reviewRepository.save(updatedReview);
        
        log.info("Freelancer response added successfully to review: {}", reviewId);
        return reviewMapper.toResponseDto(savedReview, freelancerId);
    }
    
    @Transactional
    public void markReviewHelpful(Long reviewId, Long userId, boolean helpful) {
        log.info("Marking review: {} as {} by user: {}", reviewId, helpful ? "helpful" : "unhelpful", userId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));
        
        // Update helpful counts
        if (helpful) {
            review.setHelpfulCount(review.getHelpfulCount() + 1);
        } else {
            review.setUnhelpfulCount(review.getUnhelpfulCount() + 1);
        }
        
        reviewRepository.save(review);
        log.info("Review helpfulness updated: {}", reviewId);
    }
    
    @Transactional
    public void flagReview(Long reviewId, ReviewFlagDto flagDto, Long userId) {
        log.info("Flagging review: {} by user: {}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));
        
        Review flaggedReview = reviewMapper.flagReview(review, flagDto.getReason(), flagDto.getDetails());
        reviewRepository.save(flaggedReview);
        
        log.info("Review flagged successfully: {}", reviewId);
    }
    
    @Transactional(readOnly = true)
    public ReviewSummaryDto getGigReviewSummary(Long gigId) {
        log.debug("Getting review summary for gig: {}", gigId);
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        return ReviewSummaryDto.builder()
            .totalReviews(reviewRepository.countReviewsByGigId(gigId))
            .averageRating(reviewRepository.findAverageRatingByGigId(gigId))
            .averageQualityRating(reviewRepository.findAverageQualityRatingByGigId(gigId))
            .averageCommunicationRating(reviewRepository.findAverageCommunicationRatingByGigId(gigId))
            .averageTimelinessRating(reviewRepository.findAverageTimelinessRatingByGigId(gigId))
            .averageProfessionalismRating(reviewRepository.findAverageProfessionalismRatingByGigId(gigId))
            .ratingDistribution(getRatingDistribution(reviewRepository.findRatingDistributionByGigId(gigId)))
            .recommendationPercentage(calculateRecommendationPercentage(
                reviewRepository.countRecommendationsByGigId(gigId),
                reviewRepository.countReviewsByGigId(gigId)))
            .recentReviewsCount(reviewRepository.countRecentReviewsByGigId(gigId, thirtyDaysAgo))
            .recentAverageRating(reviewRepository.findRecentAverageRatingByGigId(gigId, thirtyDaysAgo))
            .reviewsWithComments(reviewRepository.countReviewsWithCommentsByGigId(gigId))
            .reviewsWithResponse(reviewRepository.countReviewsWithResponseByGigId(gigId))
            .build();
    }
    
    @Transactional(readOnly = true)
    public ReviewSummaryDto getFreelancerReviewSummary(Long freelancerId) {
        log.debug("Getting review summary for freelancer: {}", freelancerId);
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        return ReviewSummaryDto.builder()
            .totalReviews(reviewRepository.countReviewsByFreelancerId(freelancerId))
            .averageRating(reviewRepository.findAverageRatingByFreelancerId(freelancerId))
            .averageQualityRating(reviewRepository.findAverageQualityRatingByFreelancerId(freelancerId))
            .averageCommunicationRating(reviewRepository.findAverageCommunicationRatingByFreelancerId(freelancerId))
            .averageTimelinessRating(reviewRepository.findAverageTimelinessRatingByFreelancerId(freelancerId))
            .averageProfessionalismRating(reviewRepository.findAverageProfessionalismRatingByFreelancerId(freelancerId))
            .ratingDistribution(getRatingDistribution(reviewRepository.findRatingDistributionByFreelancerId(freelancerId)))
            .recommendationPercentage(calculateRecommendationPercentage(
                reviewRepository.countRecommendationsByFreelancerId(freelancerId),
                reviewRepository.countReviewsByFreelancerId(freelancerId)))
            .recentReviewsCount(reviewRepository.countRecentReviewsByFreelancerId(freelancerId, thirtyDaysAgo))
            .recentAverageRating(reviewRepository.findRecentAverageRatingByFreelancerId(freelancerId, thirtyDaysAgo))
            .reviewsWithComments(reviewRepository.countReviewsWithCommentsByFreelancerId(freelancerId))
            .reviewsWithResponse(reviewRepository.countReviewsWithResponseByFreelancerId(freelancerId))
            .build();
    }
    
    @Transactional
    public void updateGigRatings(Long gigId) {
        log.debug("Updating ratings for gig: {}", gigId);
        
        Double averageRating = reviewRepository.findAverageRatingByGigId(gigId);
        Long reviewCount = reviewRepository.countReviewsByGigId(gigId);
        
        // Update via GigService
        gigService.updateGigRatings(gigId, averageRating, reviewCount);
    }
    
    @Transactional
    public void updateFreelancerRatings(Long freelancerId) {
        log.debug("Updating ratings for freelancer: {}", freelancerId);
        
        // Update the freelancer's profile with latest rating data
        profileService.updateProfileRatings(freelancerId);
        
        log.debug("Successfully updated freelancer ratings for: {}", freelancerId);
    }
    
    private Sort createSort(String sortBy) {
        return switch (sortBy != null ? sortBy : "newest") {
            case "oldest" -> Sort.by("createdAt").ascending();
            case "rating_high" -> Sort.by("overallRating").descending().and(Sort.by("createdAt").descending());
            case "rating_low" -> Sort.by("overallRating").ascending().and(Sort.by("createdAt").descending());
            case "helpful" -> Sort.by("helpfulCount").descending().and(Sort.by("createdAt").descending());
            default -> Sort.by("createdAt").descending();
        };
    }
    
    private Map<Integer, Long> getRatingDistribution(List<Object[]> results) {
        Map<Integer, Long> distribution = new HashMap<>();
        
        // Initialize with zeros
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        
        // Fill with actual data
        for (Object[] result : results) {
            Integer rating = (Integer) result[0];
            Long count = (Long) result[1];
            distribution.put(rating, count);
        }
        
        return distribution;
    }
    
    private Double calculateRecommendationPercentage(Long recommendations, Long totalReviews) {
        if (totalReviews == null || totalReviews == 0) {
            return 0.0;
        }
        return (recommendations != null ? recommendations.doubleValue() : 0.0) / totalReviews * 100;
    }
}
