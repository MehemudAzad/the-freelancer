package com.thefreelancer.microservices.job_proposal.dto;

import com.thefreelancer.microservices.job_proposal.model.Job;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobCreateDto {
    
    @NotNull(message = "Client ID is required")
    private Long clientId;
    
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;
    
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;
    
    private List<String> stack;
    
    @NotNull(message = "Budget type is required")
    private Job.BudgetType budgetType;
    
    @Positive(message = "Minimum budget must be positive")
    private BigInteger minBudgetCents;
    
    @Positive(message = "Maximum budget must be positive")
    private BigInteger maxBudgetCents;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;
    
    private Boolean ndaRequired = false;
    
    private Boolean ipAssignment = true;
    
    private String repoLink;
}
