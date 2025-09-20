package com.thefreelancer.microservices.job_proposal.dto;

import com.thefreelancer.microservices.job_proposal.model.ContractMilestone;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractMilestoneUpdateDto {
    
    private String title;
    private String description;
    private ContractMilestone.MilestoneStatus status;
    private String rejectionReason; // Used when rejecting milestone
}
