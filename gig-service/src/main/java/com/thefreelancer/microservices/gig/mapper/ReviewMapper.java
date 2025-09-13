package com.thefreelancer.microservices.gig.mapper;

import com.thefreelancer.microservices.gig.dto.ReviewCreateDto;
import com.thefreelancer.microservices.gig.dto.ReviewResponseDto;
import com.thefreelancer.microservices.gig.dto.ReviewUpdateDto;
import com.thefreelancer.microservices.gig.model.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    
    public Review toEntity(ReviewCreateDto dto, Long reviewerId) {
        if (dto == null) {
            return null;
        }
        
        return Review.builder()
            .gigId(dto.getGigId())
            .freelancerId(dto.getFreelancerId())
            .reviewerId(reviewerId)
            .jobId(dto.getJobId())
            .contractId(dto.getContractId())
            .overallRating(dto.getOverallRating())
            .qualityRating(dto.getQualityRating())
            .communicationRating(dto.getCommunicationRating())
            .timelinessRating(dto.getTimelinessRating())
            .professionalismRating(dto.getProfessionalismRating())
            .title(dto.getTitle())
            .comment(dto.getComment())
            .reviewType(Review.ReviewType.valueOf(dto.getReviewType()))
            .isAnonymous(dto.getIsAnonymous())
            .wouldRecommend(dto.getWouldRecommend())
            .status(Review.ReviewStatus.PUBLISHED)
            .build();
    }
    
    public ReviewResponseDto toResponseDto(Review review) {
        if (review == null) {
            return null;
        }
        
        return ReviewResponseDto.builder()
            .id(review.getId())
            .gigId(review.getGigId())
            .freelancerId(review.getFreelancerId())
            .reviewerId(review.getReviewerId())
            .jobId(review.getJobId())
            .contractId(review.getContractId())
            .overallRating(review.getOverallRating())
            .qualityRating(review.getQualityRating())
            .communicationRating(review.getCommunicationRating())
            .timelinessRating(review.getTimelinessRating())
            .professionalismRating(review.getProfessionalismRating())
            .averageRating(review.getAverageRating())
            .title(review.getTitle())
            .comment(review.getComment())
            .reviewType(review.getReviewType().name())
            .status(review.getStatus().name())
            .isAnonymous(review.getIsAnonymous())
            .wouldRecommend(review.getWouldRecommend())
            .freelancerResponse(review.getFreelancerResponse())
            .responseDate(review.getResponseDate())
            .helpfulCount(review.getHelpfulCount())
            .unhelpfulCount(review.getUnhelpfulCount())
            .totalHelpfulVotes(review.getTotalHelpfulVotes())
            .helpfulPercentage(review.getHelpfulPercentage())
            .isFlagged(review.getIsFlagged())
            .flagReason(review.getFlagReason())
            .createdAt(review.getCreatedAt())
            .updatedAt(review.getUpdatedAt())
            .isRecent(review.isRecent())
            .build();
    }
    
    public ReviewResponseDto toResponseDto(Review review, Long currentUserId) {
        ReviewResponseDto dto = toResponseDto(review);
        if (dto != null) {
            dto.setCanEdit(review.canBeEditedBy(currentUserId));
            dto.setCanRespond(review.canRespondBy(currentUserId));
        }
        return dto;
    }
    
    public Review updateEntity(Review existingReview, ReviewUpdateDto updateDto) {
        if (existingReview == null || updateDto == null) {
            return existingReview;
        }
        
        // Update ratings if provided
        if (updateDto.getOverallRating() != null) {
            existingReview.setOverallRating(updateDto.getOverallRating());
        }
        if (updateDto.getQualityRating() != null) {
            existingReview.setQualityRating(updateDto.getQualityRating());
        }
        if (updateDto.getCommunicationRating() != null) {
            existingReview.setCommunicationRating(updateDto.getCommunicationRating());
        }
        if (updateDto.getTimelinessRating() != null) {
            existingReview.setTimelinessRating(updateDto.getTimelinessRating());
        }
        if (updateDto.getProfessionalismRating() != null) {
            existingReview.setProfessionalismRating(updateDto.getProfessionalismRating());
        }
        
        // Update content if provided
        if (updateDto.getTitle() != null) {
            existingReview.setTitle(updateDto.getTitle());
        }
        if (updateDto.getComment() != null) {
            existingReview.setComment(updateDto.getComment());
        }
        
        // Update options if provided
        if (updateDto.getWouldRecommend() != null) {
            existingReview.setWouldRecommend(updateDto.getWouldRecommend());
        }
        
        return existingReview;
    }
    
    public Review addResponse(Review review, String response) {
        if (review == null) {
            return null;
        }
        
        review.setFreelancerResponse(response);
        review.setResponseDate(java.time.LocalDateTime.now());
        
        return review;
    }
    
    public Review flagReview(Review review, String reason, String details) {
        if (review == null) {
            return null;
        }
        
        review.setIsFlagged(true);
        review.setFlagReason(reason + (details != null ? " - " + details : ""));
        
        return review;
    }
}
