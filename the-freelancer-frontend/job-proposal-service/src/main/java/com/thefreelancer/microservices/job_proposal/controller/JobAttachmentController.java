package com.thefreelancer.microservices.job_proposal.controller;

import com.thefreelancer.microservices.job_proposal.dto.JobAttachmentCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.JobAttachmentResponseDto;
import com.thefreelancer.microservices.job_proposal.model.JobAttachment;
import com.thefreelancer.microservices.job_proposal.service.JobAttachmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.io.IOException;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobAttachmentController {
    
    private final JobAttachmentService jobAttachmentService;
    
    @PostMapping(value = "/{jobId}/attachments", consumes = {"multipart/form-data"})
    public ResponseEntity<JobAttachmentResponseDto> createJobAttachment(
            @PathVariable Long jobId,
            @RequestPart(name = "file") org.springframework.web.multipart.MultipartFile file,
            @RequestPart(name = "metadata", required = false) JobAttachmentCreateDto createDto) {

        log.info("POST /api/jobs/{}/attachments - Uploading file: {}", jobId, file.getOriginalFilename());

        try {
            JobAttachmentResponseDto attachment = jobAttachmentService.createJobAttachment(jobId, file, createDto == null ? new JobAttachmentCreateDto() : createDto);
            log.info("Attachment successfully created with ID: {} for jobId: {}", attachment.getId(), jobId);
            return ResponseEntity.status(HttpStatus.CREATED).body(attachment);
        } catch (RuntimeException | IOException e) {
            log.warn("Failed to create attachment for jobId {}: {}", jobId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{jobId}/attachments")
    public ResponseEntity<List<JobAttachmentResponseDto>> getJobAttachments(@PathVariable Long jobId) {
        log.info("GET /api/jobs/{}/attachments - Fetching attachments", jobId);
        
        List<JobAttachmentResponseDto> attachments = jobAttachmentService.getJobAttachments(jobId);
        return ResponseEntity.ok(attachments);
    }
    
    @GetMapping("/{jobId}/attachments/{kind}")
    public ResponseEntity<List<JobAttachmentResponseDto>> getJobAttachmentsByKind(
            @PathVariable Long jobId,
            @PathVariable JobAttachment.AttachmentKind kind) {
        
        log.info("GET /api/jobs/{}/attachments/{} - Fetching attachments by kind", jobId, kind);
        
        List<JobAttachmentResponseDto> attachments = jobAttachmentService.getJobAttachmentsByKind(jobId, kind);
        return ResponseEntity.ok(attachments);
    }
    
    @DeleteMapping("/{jobId}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteJobAttachment(
            @PathVariable Long jobId,
            @PathVariable Long attachmentId) {
        
        log.info("DELETE /api/jobs/{}/attachments/{} - Deleting attachment", jobId, attachmentId);
        
        try {
            boolean deleted = jobAttachmentService.deleteJobAttachment(jobId, attachmentId);
            
            if (deleted) {
                log.info("Attachment successfully deleted with ID: {}", attachmentId);
                return ResponseEntity.noContent().build();
            } else {
                log.warn("Attachment not found with ID: {}", attachmentId);
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            log.warn("Failed to delete attachment {}: {}", attachmentId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
