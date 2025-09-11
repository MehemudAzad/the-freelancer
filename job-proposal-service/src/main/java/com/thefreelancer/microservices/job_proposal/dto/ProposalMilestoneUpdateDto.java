package com.thefreelancer.microservices.job_proposal.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProposalMilestoneUpdateDto {
    
    private String title;
    private String description;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amountCents;
    
    private String currency;
    private LocalDate dueDate;
    private Integer orderIndex;
}
