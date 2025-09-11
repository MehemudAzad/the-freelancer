package com.thefreelancer.microservices.job_proposal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Update job milestone request")
public class JobMilestoneUpdateDto {
    
    @Schema(description = "Milestone title", example = "UI/UX Design & Wireframes")
    private String title;

    @Schema(description = "Detailed description", example = "Create user interface mockups and wireframes for all main pages")
    private String description;

    @Positive(message = "Suggested amount must be positive")
    @Schema(description = "Suggested amount in cents", example = "80000")
    private Long suggestedAmountCents;

    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Positive(message = "Estimated days must be positive")
    @Schema(description = "Estimated days to complete", example = "7")
    private Integer estimatedDays;

    @Schema(description = "Order index for sorting", example = "1")
    private Integer orderIndex;
}
