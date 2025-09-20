package com.thefreelancer.microservices.job_proposal.dto.workspace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO from workspace service room creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponseDto {
    
    private Long id;
    private Long contractId;
    private String jobTitle;
    private String clientId;
    private String freelancerId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
