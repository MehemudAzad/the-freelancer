package com.thefreelancer.microservices.gig.integration;

import com.thefreelancer.microservices.gig.dto.ReviewCreateDto;
import com.thefreelancer.microservices.gig.dto.ReviewResponseDto;
import com.thefreelancer.microservices.gig.model.Gig;
import com.thefreelancer.microservices.gig.model.Profile;
import com.thefreelancer.microservices.gig.model.Review;
import com.thefreelancer.microservices.gig.repository.GigRepository;
import com.thefreelancer.microservices.gig.repository.ProfileRepository;
import com.thefreelancer.microservices.gig.repository.ReviewRepository;
import com.thefreelancer.microservices.gig.service.ReviewService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReviewIntegrationTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private GigRepository gigRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private Validator validator;

    private Gig testGig;
    private Profile testProfile;

    @BeforeEach
    void setUp() {
        // Create test profile (freelancer)
        testProfile = Profile.builder()
                .userId(100L)
                .headline("Test Freelancer")
                .bio("Test bio")
                .currency("USD")
                .build();
        testProfile = profileRepository.save(testProfile);

        // Create test gig
        testGig = Gig.builder()
                .title("Test Gig")
                .description("Test description")
                .profileId(testProfile.getUserId())
                .category("PROGRAMMING")
                .status(Gig.Status.ACTIVE)
                .build();
        testGig = gigRepository.save(testGig);
    }

    @Test
    void testCreateReview_Success() {
        // Given
        ReviewCreateDto createDto = ReviewCreateDto.builder()
                .gigId(testGig.getId())
                .freelancerId(testProfile.getUserId())
                .jobId("job-1")
                .overallRating(4)
                .qualityRating(5)
                .communicationRating(4)
                .timelinessRating(4)
                .professionalismRating(5)
                .wouldRecommend(true)
                .comment("Excellent work! Very professional and delivered on time.")
                .build();

        // When
        ReviewResponseDto result = reviewService.createReview(createDto, 200L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGigId()).isEqualTo(testGig.getId());
        assertThat(result.getFreelancerId()).isEqualTo(testProfile.getUserId());
        assertThat(result.getOverallRating()).isEqualTo(4.0); // Set explicitly
        assertThat(result.getWouldRecommend()).isTrue();
        assertThat(result.getComment()).isEqualTo("Excellent work! Very professional and delivered on time.");
        assertThat(result.getStatus()).isEqualTo(Review.ReviewStatus.PUBLISHED);

        // Verify review was saved
        assertThat(reviewRepository.count()).isEqualTo(1);
        
        // Verify gig rating was updated
        Gig updatedGig = gigRepository.findById(testGig.getId()).orElseThrow();
        assertThat(updatedGig.getReviewAvg()).isEqualByComparingTo(BigDecimal.valueOf(4.0));
        assertThat(updatedGig.getReviewsCount()).isEqualTo(1);

        // Verify profile rating was updated
        Profile updatedProfile = profileRepository.findById(testProfile.getUserId()).orElseThrow();
        assertThat(updatedProfile.getReviewAvg()).isEqualByComparingTo(BigDecimal.valueOf(4.0));
        assertThat(updatedProfile.getReviewsCount()).isEqualTo(1);
    }

    @Test
    void testCreateReview_ValidationErrors() {
        // Given - Invalid review (missing required fields and invalid ratings)
        ReviewCreateDto createDto = ReviewCreateDto.builder()
                .gigId(null) // Missing required field
                .freelancerId(testProfile.getUserId())
                .qualityRating(6) // Invalid (max 5)
                .communicationRating(0) // Invalid (min 1)
                .build();

        // When
        Set<ConstraintViolation<ReviewCreateDto>> violations = validator.validate(createDto);

        // Then
        assertThat(violations).hasSizeGreaterThan(0);
        boolean hasNullViolation = violations.stream()
                .anyMatch(v -> v.getMessage().contains("must not be null"));
        boolean hasRangeViolation = violations.stream()
                .anyMatch(v -> v.getMessage().contains("must be between 1 and 5"));
        
        assertThat(hasNullViolation).isTrue();
        assertThat(hasRangeViolation).isTrue();
    }

    @Test
    void testMultipleReviews_RatingCalculation() {
        // Given - Create multiple reviews with different ratings
        createTestReview(5, 5, 5, 5, 5, true, 201L); // Overall: 5.0
        createTestReview(4, 4, 4, 4, 4, true, 202L); // Overall: 4.0  
        createTestReview(3, 3, 3, 3, 3, false, 203L); // Overall: 3.0

        // When - Check gig rating summary
        var summary = reviewService.getGigReviewSummary(testGig.getId());

        // Then
        assertThat(summary.getTotalReviews()).isEqualTo(3);
        assertThat(summary.getAverageRating()).isEqualByComparingTo(4.0); // (5+4+3)/3
        assertThat(summary.getRecommendationPercentage()).isEqualByComparingTo(66.67); // 2 out of 3
        assertThat(summary.getAverageQualityRating()).isEqualByComparingTo(4.0);
        assertThat(summary.getAverageCommunicationRating()).isEqualByComparingTo(4.0);
    }

    private void createTestReview(int overall, int quality, int communication, int timeliness, 
                                  int professionalism, boolean recommend, Long reviewerId) {
        ReviewCreateDto createDto = ReviewCreateDto.builder()
                .gigId(testGig.getId())
                .freelancerId(testProfile.getUserId())
                .jobId("job-" + reviewerId)
                .overallRating(overall)
                .qualityRating(quality)
                .communicationRating(communication)
                .timelinessRating(timeliness)
                .professionalismRating(professionalism)
                .wouldRecommend(recommend)
                .comment("Test review comment")
                .build();
        
        reviewService.createReview(createDto, reviewerId);
    }
}
