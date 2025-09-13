package com.thefreelancer.microservices.job_proposal.dto.workspace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a room in workspace service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomCreateDto {
    
    private Long contractId;
    private String jobTitle;
    private String clientId;
    private String freelancerId;
    
    @Builder.Default
    private String status = "ACTIVE";
}
