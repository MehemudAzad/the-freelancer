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

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobAttachmentService {
    
    private final JobAttachmentRepository jobAttachmentRepository;
    private final JobRepository jobRepository;
    private final JobAttachmentMapper jobAttachmentMapper;
    
    @Transactional
    public JobAttachmentResponseDto createJobAttachment(Long jobId, JobAttachmentCreateDto createDto) {
        log.info("Creating attachment for jobId: {} with kind: {}", jobId, createDto.getKind());
        
        // Check if job exists
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new RuntimeException("Job not found with ID: " + jobId);
        }
        
        Job job = jobOpt.get();
        
        JobAttachment attachment = jobAttachmentMapper.toEntity(createDto);
        attachment.setJob(job);
        
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
        
        jobAttachmentRepository.deleteById(attachmentId);
        log.info("Successfully deleted attachment with ID: {}", attachmentId);
        return true;
    }
}
