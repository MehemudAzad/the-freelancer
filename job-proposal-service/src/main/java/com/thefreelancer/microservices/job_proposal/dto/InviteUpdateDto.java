package com.thefreelancer.microservices.job_proposal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteUpdateDto {
    
    @NotNull(message = "Status is required")
    private String status; // ACCEPTED, DECLINED
}