package com.thefreelancer.microservices.job_proposal.controller;

import com.thefreelancer.microservices.job_proposal.dto.JobAttachmentCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.JobAttachmentResponseDto;
import com.thefreelancer.microservices.job_proposal.model.JobAttachment;
import com.thefreelancer.microservices.job_proposal.service.JobAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Job Attachments", description = "Job attachment management operations")
public class JobAttachmentController {
    
    private final JobAttachmentService jobAttachmentService;
    
    @Operation(summary = "Upload job attachment", description = "Upload specs, wireframes, or datasets for a job (Job owner only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Attachment uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid attachment data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not your job"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @PostMapping("/{jobId}/attachments")
    public ResponseEntity<JobAttachmentResponseDto> createJobAttachment(
            @Parameter(description = "ID of the job to attach file to") @PathVariable Long jobId,
            @Valid @RequestBody JobAttachmentCreateDto createDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("POST /api/jobs/{}/attachments - Creating attachment with kind: {}", jobId, createDto.getKind());
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for uploading job attachments");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only clients can upload attachments to their jobs
        if (!"CLIENT".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only clients can upload job attachments. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            log.info("Uploading attachment for job: {} by user: {}", jobId, authenticatedUserId);
            
            // TODO: Service should validate job ownership
            JobAttachmentResponseDto attachment = jobAttachmentService.createJobAttachment(jobId, createDto);
            log.info("Attachment successfully created with ID: {} for jobId: {}", attachment.getId(), jobId);
            return ResponseEntity.status(HttpStatus.CREATED).body(attachment);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.warn("Failed to create attachment for jobId {}: {}", jobId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "Get job attachments", description = "Get all attachments for a job (public)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/{jobId}/attachments")
    public ResponseEntity<List<JobAttachmentResponseDto>> getJobAttachments(
            @Parameter(description = "ID of the job to get attachments for") @PathVariable Long jobId) {
        log.info("GET /api/jobs/{}/attachments - Fetching attachments", jobId);
        
        List<JobAttachmentResponseDto> attachments = jobAttachmentService.getJobAttachments(jobId);
        return ResponseEntity.ok(attachments);
    }
    
    @Operation(summary = "Get job attachments by kind", description = "Get attachments for a job filtered by type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/{jobId}/attachments/{kind}")
    public ResponseEntity<List<JobAttachmentResponseDto>> getJobAttachmentsByKind(
            @Parameter(description = "ID of the job") @PathVariable Long jobId,
            @Parameter(description = "Type of attachment to filter by") @PathVariable JobAttachment.AttachmentKind kind) {
        
        log.info("GET /api/jobs/{}/attachments/{} - Fetching attachments by kind", jobId, kind);
        
        List<JobAttachmentResponseDto> attachments = jobAttachmentService.getJobAttachmentsByKind(jobId, kind);
        return ResponseEntity.ok(attachments);
    }
    
    @Operation(summary = "Delete job attachment", description = "Delete an attachment from a job (Job owner only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Attachment deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied - not your job"),
        @ApiResponse(responseCode = "404", description = "Attachment not found")
    })
    @DeleteMapping("/{jobId}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteJobAttachment(
            @Parameter(description = "ID of the job") @PathVariable Long jobId,
            @Parameter(description = "ID of the attachment to delete") @PathVariable Long attachmentId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("DELETE /api/jobs/{}/attachments/{} - Deleting attachment", jobId, attachmentId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for deleting job attachments");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only clients can delete attachments from their jobs
        if (!"CLIENT".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only clients can delete job attachments. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            log.info("Deleting attachment: {} from job: {} by user: {}", attachmentId, jobId, authenticatedUserId);
            
            // TODO: Service should validate job and attachment ownership
            boolean deleted = jobAttachmentService.deleteJobAttachment(jobId, attachmentId);
            
            if (deleted) {
                log.info("Attachment successfully deleted with ID: {}", attachmentId);
                return ResponseEntity.noContent().build();
            } else {
                log.warn("Attachment not found with ID: {}", attachmentId);
                return ResponseEntity.notFound().build();
            }
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.warn("Failed to delete attachment {}: {}", attachmentId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
