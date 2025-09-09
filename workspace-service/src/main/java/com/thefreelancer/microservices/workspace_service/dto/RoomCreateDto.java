package com.thefreelancer.microservices.workspace_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomCreateDto {
    
    @NotNull(message = "Contract ID is required")
    private Long contractId;
    
    @NotBlank(message = "Job title is required")
    private String jobTitle;
    
    @NotBlank(message = "Client ID is required")
    private String clientId;
    
    @NotBlank(message = "Freelancer ID is required")
    private String freelancerId;
}
