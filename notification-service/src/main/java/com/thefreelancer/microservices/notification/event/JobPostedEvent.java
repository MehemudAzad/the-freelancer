package com.thefreelancer.microservices.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostedEvent {
    private Long jobId;
    private Long clientId;
    private String clientName;
    private String clientHandle;
    private String jobTitle;
    private String jobDescription;
    private String[] requiredSkills;
    private Long minBudget;
    private Long maxBudget;
    private String currency;
    private String budgetType; // FIXED, HOURLY
    private Integer estimatedDuration;
    private LocalDateTime postedAt;
    private String jobCategory;
    private String experienceLevel; // ENTRY, INTERMEDIATE, EXPERT
    private boolean isRemote;
    private String location;
}
