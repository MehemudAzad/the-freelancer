package com.thefreelancer.microservices.job_proposal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Create job milestone request")
public class JobMilestoneCreateDto {
    
    @NotBlank(message = "Title is required")
    @Schema(description = "Milestone title", example = "UI/UX Design & Wireframes", required = true)
    private String title;

    @Schema(description = "Detailed description with acceptance criteria", 
            example = "Create user interface mockups and wireframes for all main pages including:\n• High-fidelity mockups for all pages\n• Interactive prototype\n• Design system documentation")
    private String description;

    @Positive(message = "Suggested amount must be positive")
    @Schema(description = "Suggested amount in cents", example = "80000")
    private Long suggestedAmountCents;

    @Schema(description = "Currency code", example = "USD")
    private String currency = "USD";

    @Positive(message = "Estimated days must be positive")
    @Schema(description = "Estimated days to complete", example = "7")
    private Integer estimatedDays;

    @NotNull(message = "Order index is required")
    @Schema(description = "Order index for sorting", example = "1", required = true)
    private Integer orderIndex;
}
