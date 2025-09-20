package com.thefreelancer.microservices.job_proposal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Job milestone response DTO")
public class JobMilestoneResponseDto {
    
    @Schema(description = "Milestone ID", example = "1")
    private Long id;

    @Schema(description = "Job ID", example = "1")
    private Long jobId;

    @Schema(description = "Milestone title", example = "UI/UX Design & Wireframes")
    private String title;

    @Schema(description = "Detailed description", example = "Create user interface mockups and wireframes for all main pages")
    private String description;

    @Schema(description = "Suggested amount in cents", example = "80000")
    private Long suggestedAmountCents;

    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Schema(description = "Estimated days to complete", example = "7")
    private Integer estimatedDays;

    @Schema(description = "Order index for sorting", example = "1")
    private Integer orderIndex;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
