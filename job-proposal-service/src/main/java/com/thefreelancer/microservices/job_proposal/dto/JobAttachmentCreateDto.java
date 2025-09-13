package com.thefreelancer.microservices.job_proposal.dto;

import com.thefreelancer.microservices.job_proposal.model.JobAttachment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobAttachmentCreateDto {
    
    private JobAttachment.AttachmentKind kind;
    
    private String url;
    
    private String filename;
    
    private BigInteger bytes;
    
    private String checksum;
}
