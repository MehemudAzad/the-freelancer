package com.thefreelancer.microservices.job_proposal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalMilestoneDto {
	private String title;
	private String description;
	private BigInteger amountCents;
	private LocalDate dueDate;
	private Integer orderIndex;
}
