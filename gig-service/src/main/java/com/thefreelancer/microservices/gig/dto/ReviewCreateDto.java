package com.thefreelancer.microservices.gig.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateDto {
    
    // Remove gigId requirement - reviews are now user-centric
    private Long gigId; // Optional - if review is related to a specific gig
    
    @NotNull(message = "Freelancer ID is required")
    private Long freelancerId;
    
    // Optional - if this review is related to a specific job/contract
    private String jobId;
    private String contractId;
    
    // Ratings (1-5 scale)
    @NotNull(message = "Overall rating is required")
    @Min(value = 1, message = "Overall rating must be between 1 and 5")
    @Max(value = 5, message = "Overall rating must be between 1 and 5")
    private Integer overallRating;
    
    @NotNull(message = "Quality rating is required")
    @Min(value = 1, message = "Quality rating must be between 1 and 5")
    @Max(value = 5, message = "Quality rating must be between 1 and 5")
    private Integer qualityRating;
    
    @NotNull(message = "Communication rating is required")
    @Min(value = 1, message = "Communication rating must be between 1 and 5")
    @Max(value = 5, message = "Communication rating must be between 1 and 5")
    private Integer communicationRating;
    
    @NotNull(message = "Timeliness rating is required")
    @Min(value = 1, message = "Timeliness rating must be between 1 and 5")
    @Max(value = 5, message = "Timeliness rating must be between 1 and 5")
    private Integer timelinessRating;
    
    @NotNull(message = "Professionalism rating is required")
    @Min(value = 1, message = "Professionalism rating must be between 1 and 5")
    @Max(value = 5, message = "Professionalism rating must be between 1 and 5")
    private Integer professionalismRating;
    
    // Review content
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;
    
    @NotBlank(message = "Comment is required")
    @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
    private String comment;
    
    // Review type and options
    @Builder.Default
    private String reviewType = "GIG_REVIEW";
    
    @Builder.Default
    private Boolean isAnonymous = false;
    
    @Builder.Default
    private Boolean wouldRecommend = true;
}
