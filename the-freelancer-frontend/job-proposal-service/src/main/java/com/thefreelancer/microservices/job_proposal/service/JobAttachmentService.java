package com.thefreelancer.microservices.job_proposal.service;

import com.thefreelancer.microservices.job_proposal.dto.JobAttachmentCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.JobAttachmentResponseDto;
import com.thefreelancer.microservices.job_proposal.mapper.JobAttachmentMapper;
import com.thefreelancer.microservices.job_proposal.model.Job;
import com.thefreelancer.microservices.job_proposal.model.JobAttachment;
import com.thefreelancer.microservices.job_proposal.repository.JobAttachmentRepository;
import com.thefreelancer.microservices.job_proposal.repository.JobRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.thefreelancer.microservices.job_proposal.service.CloudinaryService.CloudinaryUploadResult;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobAttachmentService {
    
    private final JobAttachmentRepository jobAttachmentRepository;
    private final JobRepository jobRepository;
    private final JobAttachmentMapper jobAttachmentMapper;
    private final CloudinaryService cloudinaryService;
    
    @Transactional
    public JobAttachmentResponseDto createJobAttachment(Long jobId, MultipartFile file, JobAttachmentCreateDto createDto) throws IOException {
        log.info("Creating attachment for jobId: {} with file: {}", jobId, file.getOriginalFilename());

        // Check if job exists
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new RuntimeException("Job not found with ID: " + jobId);
        }

        Job job = jobOpt.get();

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Upload to Cloudinary
        String folder = "jobs/" + jobId + "/files";
        CloudinaryUploadResult uploadResult = cloudinaryService.uploadFile(file, folder);

        // Compute checksum (sha256)
        String checksum = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            checksum = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.warn("SHA-256 algorithm not available, skipping checksum");
        }

        JobAttachment attachment = jobAttachmentMapper.toEntity(createDto);
        attachment.setJob(job);
        attachment.setUrl(uploadResult.getSecureUrl());
        attachment.setCloudinaryPublicId(uploadResult.getPublicId());
        attachment.setCloudinaryResourceType(uploadResult.getResourceType());
        attachment.setContentType(file.getContentType());
    long fileSizeLong = uploadResult.getBytes() != null ? uploadResult.getBytes().longValue() : file.getSize();
    attachment.setBytes(java.math.BigInteger.valueOf(fileSizeLong));
        attachment.setChecksum(checksum);

        JobAttachment savedAttachment = jobAttachmentRepository.save(attachment);
        log.info("Successfully created attachment with ID: {} for jobId: {}", savedAttachment.getId(), jobId);

        return jobAttachmentMapper.toResponseDto(savedAttachment);
    }
    
    public List<JobAttachmentResponseDto> getJobAttachments(Long jobId) {
        log.info("Fetching attachments for jobId: {}", jobId);
        
        return jobAttachmentRepository.findByJobId(jobId)
                .stream()
                .map(jobAttachmentMapper::toResponseDto)
                .toList();
    }
    
    public List<JobAttachmentResponseDto> getJobAttachmentsByKind(Long jobId, JobAttachment.AttachmentKind kind) {
        log.info("Fetching attachments for jobId: {} with kind: {}", jobId, kind);
        
        return jobAttachmentRepository.findByJobIdAndKind(jobId, kind)
                .stream()
                .map(jobAttachmentMapper::toResponseDto)
                .toList();
    }
    
    @Transactional
    public boolean deleteJobAttachment(Long jobId, Long attachmentId) {
        log.info("Deleting attachment with ID: {} for jobId: {}", attachmentId, jobId);
        
        Optional<JobAttachment> attachmentOpt = jobAttachmentRepository.findById(attachmentId);
        
        if (attachmentOpt.isEmpty()) {
            log.warn("Attachment not found with ID: {}", attachmentId);
            return false;
        }
        
        JobAttachment attachment = attachmentOpt.get();
        
        // Verify attachment belongs to the job
        if (!attachment.getJob().getId().equals(jobId)) {
            throw new RuntimeException("Attachment " + attachmentId + " does not belong to job: " + jobId);
        }
        
        // Try deleting from Cloudinary if we have a public id
        if (attachment.getCloudinaryPublicId() != null) {
            try {
                cloudinaryService.deleteFile(attachment.getCloudinaryPublicId(), attachment.getCloudinaryResourceType() != null ? attachment.getCloudinaryResourceType() : "raw");
            } catch (Exception e) {
                log.warn("Failed to delete file from Cloudinary: {}", e.getMessage());
            }
        }

        jobAttachmentRepository.deleteById(attachmentId);
        log.info("Successfully deleted attachment with ID: {}", attachmentId);
        return true;
    }
}
