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
    
    // clientId should come from authentication headers, not from request body
    
    @NotBlank(message = "Project name is required")
    @Size(min = 5, max = 200, message = "Project name must be between 5 and 200 characters")
    private String projectName;
    
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;
    
    private String category;
    
    private List<String> skills;
    
    private Boolean isUrgent = false;
    
    @NotNull(message = "Budget type is required")
    private Job.BudgetType budgetType;
    
    @Positive(message = "Minimum budget must be positive")
    private BigInteger minBudgetCents;
    
    @Positive(message = "Maximum budget must be positive")
    private BigInteger maxBudgetCents;
    
    private Boolean ndaRequired = false;
    
    private Boolean ipAssignment = true;
}
