package com.thefreelancer.microservices.job_proposal.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobWithAttachmentCreateDto {
    private JobCreateDto job;
}
