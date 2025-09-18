package com.thefreelancer.microservices.gig.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRecommendationDto {
    
    private Long jobId;
    private String projectName;
    private String description;
    private String category;
    private List<String> skills;
    private String budgetType;
    private BigInteger minBudgetCents;
    private BigInteger maxBudgetCents;
    private Boolean isUrgent;
    private Double matchScore; // Similarity score from 0.0 to 1.0
    private String matchReason; // Brief explanation of why it's recommended
}