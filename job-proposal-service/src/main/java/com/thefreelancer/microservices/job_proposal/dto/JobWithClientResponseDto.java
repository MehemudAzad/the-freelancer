package com.thefreelancer.microservices.job_proposal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Enhanced Job Response DTO that includes client information
 * This is returned when we need to display jobs with client details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobWithClientResponseDto {
    // Job information
    private Long id;
    private Long clientId;
    private String projectName;
    private String description;
    private String budgetType; // FIXED, HOURLY
    private BigInteger minBudgetCents;
    private BigInteger maxBudgetCents;
    private String category;
    private List<String> skills;
    private Boolean isUrgent;
    private Boolean ndaRequired;
    private Boolean ipAssignment;
    private String status; // DRAFT, OPEN, etc.
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime editedAt;
    
    // Client information (from Auth Service)
    private ClientInfo client;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientInfo {
        private Long id;
        private String name;
        private String email;
        private String handle;
        private String country;
        private String timezone;
        private Boolean isActive;
    }
}
