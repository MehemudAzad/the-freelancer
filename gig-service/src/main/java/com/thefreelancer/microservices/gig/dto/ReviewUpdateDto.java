package com.thefreelancer.microservices.gig.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewUpdateDto {
    
    // Updated ratings (optional - only include if changed)
    @Min(value = 1, message = "Overall rating must be between 1 and 5")
    @Max(value = 5, message = "Overall rating must be between 1 and 5")
    private Integer overallRating;
    
    @Min(value = 1, message = "Quality rating must be between 1 and 5")
    @Max(value = 5, message = "Quality rating must be between 1 and 5")
    private Integer qualityRating;
    
    @Min(value = 1, message = "Communication rating must be between 1 and 5")
    @Max(value = 5, message = "Communication rating must be between 1 and 5")
    private Integer communicationRating;
    
    @Min(value = 1, message = "Timeliness rating must be between 1 and 5")
    @Max(value = 5, message = "Timeliness rating must be between 1 and 5")
    private Integer timelinessRating;
    
    @Min(value = 1, message = "Professionalism rating must be between 1 and 5")
    @Max(value = 5, message = "Professionalism rating must be between 1 and 5")
    private Integer professionalismRating;
    
    // Updated content
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;
    
    @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
    private String comment;
    
    // Updated options
    private Boolean wouldRecommend;
}
