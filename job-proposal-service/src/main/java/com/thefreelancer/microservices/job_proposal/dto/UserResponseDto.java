package com.thefreelancer.microservices.job_proposal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for User information received from Auth Service
 * This is a simplified version for inter-service communication
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private String name;
    private String handle;
    private String role; // Simplified as String instead of enum
    private String country;
    private String timezone;
    private Boolean isActive;
    private String kycStatus; // Simplified as String instead of enum
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
