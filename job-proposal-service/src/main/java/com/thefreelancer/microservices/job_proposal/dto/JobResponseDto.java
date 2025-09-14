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
    private String projectName;
    private String description;
    private String budgetType;
    private BigInteger minBudgetCents;
    private BigInteger maxBudgetCents;
    private String category;
    private List<String> skills;
    private Boolean isUrgent;
    private Boolean ndaRequired;
    private Boolean ipAssignment;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime editedAt;
}
