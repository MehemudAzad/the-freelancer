package com.thefreelancer.microservices.job_proposal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for nested freelancer information in proposal responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FreelancerInfoDto {
    private Long id;
    private String name;
    private String email;
    private String profilePictureUrl;
    private String joinedAt; // ISO date string
}