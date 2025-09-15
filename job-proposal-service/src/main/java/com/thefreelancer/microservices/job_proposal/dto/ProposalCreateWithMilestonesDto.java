package com.thefreelancer.microservices.job_proposal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalCreateWithMilestonesDto {
    private Long jobId;
    private Long freelancerId;
    private String cover;
    private BigInteger totalCents;
    private Integer deliveryDays;
    private List<ProposalMilestoneDto> milestones;
}
