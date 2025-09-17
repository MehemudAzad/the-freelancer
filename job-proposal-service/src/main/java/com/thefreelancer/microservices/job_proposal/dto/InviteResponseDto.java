package com.thefreelancer.microservices.job_proposal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteResponseDto {
    
    private Long id;
    private Long jobId;
    private String jobTitle; // Included for convenience
    private Long clientId;
    private String clientName; // Included for convenience
    private Long freelancerId;
    private String freelancerName; // Included for convenience
    private String status; // SENT, ACCEPTED, DECLINED, EXPIRED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}