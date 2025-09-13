package com.thefreelancer.microservices.job_proposal.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobAttachmentResponseDto {
    private Long id;
    private Long jobId;
    private String kind;
    private String url;
    private String filename;
    private BigInteger bytes;
    private String contentType;
    private String cloudinaryPublicId;
    private String cloudinaryResourceType;
    private String checksum;
    private LocalDateTime createdAt;
}
