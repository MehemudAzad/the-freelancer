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
    
    @NotNull(message = "Attachment kind is required")
    private JobAttachment.AttachmentKind kind;
    
    @NotBlank(message = "URL is required")
    private String url;
    
    @NotBlank(message = "Filename is required")
    private String filename;
    
    private BigInteger bytes;
}
