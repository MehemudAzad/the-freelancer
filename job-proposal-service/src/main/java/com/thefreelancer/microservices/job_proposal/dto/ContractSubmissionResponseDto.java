package com.thefreelancer.microservices.job_proposal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractSubmissionResponseDto {
    private Long contractId;
    private String jobTitle;
    private Long clientId;
    private Long freelancerId;
    
    // Submission Details
    private String submissionDescription;
    private String submissionNotes;
    private List<String> deliverableUrls;
    private LocalDateTime submittedAt;
    
    // Acceptance/Rejection Details
    private LocalDateTime acceptedAt;
    private LocalDateTime rejectedAt;
    private String rejectionReason;
    private String rejectionFeedback;
    
    // Status Information
    private String contractStatus;
    private Boolean isSubmitted;
    private Boolean isAccepted;
    private Boolean isRejected;
}