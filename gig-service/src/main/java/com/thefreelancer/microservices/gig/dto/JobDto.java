package com.thefreelancer.microservices.gig.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Job data from job-proposal-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDto {
    private Long id;
    private String title;
    private String description;
    private List<String> skills;
    private String category;
    private Long minBudgetCents;
    private Long maxBudgetCents;
    private String currency;
    private String status;
}