package com.thefreelancer.microservices.job_proposal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalResponseDto {
    
    private Long id;
    private Long jobId;
    private String jobTitle; // Included for convenience
    private Long freelancerId;
    private String freelancerName; // Included for convenience
    private String coverLetter;
    private BigDecimal proposedRate;
    private Integer deliveryDays;
    private String portfolioLinks;
    private String additionalNotes;
    private String status; // SUBMITTED, ACCEPTED, REJECTED, WITHDRAWN
    private Long contractId; // ID of the contract if one exists
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
    
    // Enhanced freelancer information from auth-service
    private FreelancerInfo freelancerInfo;
    private List<ProposalMilestoneDto> milestones;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FreelancerInfo {
        private Long id;
        private String name;
        private String email;
        private String handle;
        private String profilePicture;
        private String bio;
        private String skills;
        private Double hourlyRate;
        private String location;
        private LocalDateTime joinedAt;
        private Integer completedProjects;
        private Double rating;
        private String portfolioUrl;
    }
}
