package com.thefreelancer.microservices.job_proposal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProposalUpdateDto {
    
    private String coverLetter;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Proposed rate must be greater than 0")
    private BigDecimal proposedRate;
    
    @Min(value = 1, message = "Delivery days must be at least 1")
    private Integer deliveryDays;
    
    private String portfolioLinks;
    
    private String additionalNotes;
}
