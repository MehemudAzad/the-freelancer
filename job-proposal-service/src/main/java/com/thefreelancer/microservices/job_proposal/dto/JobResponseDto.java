package com.thefreelancer.microservices.job_proposal.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobResponseDto {
    private Long id;
    private Long clientId;
    private String title;
    private String description;
    private List<String> stack;
    private String budgetType;
    private BigInteger minBudgetCents;
    private BigInteger maxBudgetCents;
    private String currency;
    private Boolean ndaRequired;
    private Boolean ipAssignment;
    private String repoLink;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
