package com.thefreelancer.microservices.job_proposal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProposalCreateDto {
    
    @NotNull(message = "Job ID is required")
    private Long jobId;
    
    private Long freelancerId; // Will be set from authenticated user
    
    @NotBlank(message = "Cover letter is required")
    private String coverLetter;
    
    @NotNull(message = "Proposed rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Proposed rate must be greater than 0")
    private BigDecimal proposedRate;
    
    @NotNull(message = "Delivery days is required")
    @Min(value = 1, message = "Delivery days must be at least 1")
    private Integer deliveryDays;
    
    private String portfolioLinks;
    
    private String additionalNotes;
}
