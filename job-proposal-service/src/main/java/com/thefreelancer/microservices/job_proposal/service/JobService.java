package com.thefreelancer.microservices.job_proposal.service;

import com.thefreelancer.microservices.job_proposal.dto.JobCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.JobResponseDto;
import com.thefreelancer.microservices.job_proposal.dto.JobUpdateDto;
import com.thefreelancer.microservices.job_proposal.dto.JobWithClientResponseDto;
import com.thefreelancer.microservices.job_proposal.dto.UserResponseDto;
import com.thefreelancer.microservices.job_proposal.mapper.JobMapper;
import com.thefreelancer.microservices.job_proposal.model.Job;
import com.thefreelancer.microservices.job_proposal.repository.JobRepository;
import com.thefreelancer.microservices.job_proposal.client.AuthServiceClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {
    
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final AuthServiceClient authServiceClient;
    
    @Transactional
    public JobResponseDto createJob(JobCreateDto createDto, Long clientId) {
        log.info("Creating job for clientId: {}", clientId);
        
        // Validate budget range
        if (createDto.getMinBudgetCents() != null && createDto.getMaxBudgetCents() != null) {
            if (createDto.getMinBudgetCents().compareTo(createDto.getMaxBudgetCents()) > 0) {
                throw new RuntimeException("Minimum budget cannot be greater than maximum budget");
            }
        }

        Job job = jobMapper.toEntity(createDto);
        job.setClientId(clientId); // Set clientId from authentication
        job.setStatus(Job.JobStatus.DRAFT); // New jobs start as draft
        
        Job savedJob = jobRepository.save(job);
        log.info("Successfully created job with ID: {} for clientId: {}", savedJob.getId(), clientId);
        
        return jobMapper.toResponseDto(savedJob);
    }    public Optional<JobResponseDto> getJobById(Long jobId) {
        log.info("Fetching job with ID: {}", jobId);
        
        return jobRepository.findById(jobId)
                .map(jobMapper::toResponseDto);
    }
    
    @Transactional
    public Optional<JobResponseDto> updateJob(Long jobId, JobUpdateDto updateDto) {
        log.info("Updating job with ID: {}", jobId);
        
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        
        if (jobOpt.isEmpty()) {
            log.warn("Job not found with ID: {}", jobId);
            return Optional.empty();
        }
        
        Job job = jobOpt.get();
        
        // Validate budget range if both are provided
        if (updateDto.getMinBudgetCents() != null && updateDto.getMaxBudgetCents() != null) {
            if (updateDto.getMinBudgetCents().compareTo(updateDto.getMaxBudgetCents()) > 0) {
                throw new RuntimeException("Minimum budget cannot be greater than maximum budget");
            }
        }
        
        jobMapper.updateEntityFromDto(updateDto, job);

    // mark the job as edited now
    job.setEditedAt(java.time.LocalDateTime.now());

    Job updatedJob = jobRepository.save(job);
        log.info("Successfully updated job with ID: {}", jobId);
        
        return Optional.of(jobMapper.toResponseDto(updatedJob));
    }
    
    @Transactional
    public boolean deleteJob(Long jobId) {
        log.info("Deleting job with ID: {}", jobId);
        
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        
        if (jobOpt.isEmpty()) {
            log.warn("Job not found with ID: {}", jobId);
            return false;
        }
        
        Job job = jobOpt.get();
        
        // Check if job can be deleted (should not be in progress or completed)
        if (job.getStatus() == Job.JobStatus.IN_PROGRESS || job.getStatus() == Job.JobStatus.COMPLETED) {
            throw new RuntimeException("Cannot delete job in " + job.getStatus() + " status");
        }
        
        // Soft delete by setting status to CANCELLED
        job.setStatus(Job.JobStatus.CANCELLED);
        jobRepository.save(job);
        
        log.info("Successfully cancelled/deleted job with ID: {}", jobId);
        return true;
    }
    
    public List<JobResponseDto> getJobsByClientId(Long clientId) {
        log.info("Fetching jobs for clientId: {}", clientId);
        
        return jobRepository.findByClientId(clientId)
                .stream()
                .map(jobMapper::toResponseDto)
                .toList();
    }
    
    public List<JobResponseDto> searchJobs(String category, BigInteger minBudget, BigInteger maxBudget, Boolean isUrgent, Job.BudgetType budgetType) {
        log.info("Searching jobs with category: {}, minBudget: {}, maxBudget: {}, isUrgent: {}, budgetType: {}", category, minBudget, maxBudget, isUrgent, budgetType);

        List<Job> jobs;
        
        // If all parameters are null/empty, return all open jobs for efficiency
        if (category == null && minBudget == null && maxBudget == null && isUrgent == null && budgetType == null) {
            log.info("No filters provided - returning all open jobs");
            jobs = jobRepository.findOpenJobs();
        } else {
            // Use filtered search when any parameter is provided
            log.info("Applying filters to search");
            jobs = jobRepository.findOpenJobsByFilters(category, isUrgent, budgetType, minBudget, maxBudget);
        }

        return jobs.stream()
                .map(jobMapper::toResponseDto)
                .toList();
    }
    
    /**
     * Search jobs with client information enrichment
     * This method fetches jobs and enriches them with client data from auth-service
     */
    public List<JobWithClientResponseDto> searchJobsWithClientInfo(String category, BigInteger minBudget, BigInteger maxBudget, Boolean isUrgent, Job.BudgetType budgetType) {
        log.info("Searching jobs with client info - category: {}, minBudget: {}, maxBudget: {}, isUrgent: {}, budgetType: {}", category, minBudget, maxBudget, isUrgent, budgetType);

        List<Job> jobs;
        
        // If all parameters are null/empty, return all open jobs for efficiency
        if (category == null && minBudget == null && maxBudget == null && isUrgent == null && budgetType == null) {
            log.info("No filters provided - returning all open jobs with client info");
            jobs = jobRepository.findOpenJobs();
        } else {
            // Use filtered search when any parameter is provided
            log.info("Applying filters to search with client info");
            jobs = jobRepository.findOpenJobsByFilters(category, isUrgent, budgetType, minBudget, maxBudget);
        }
        
        // Extract unique client IDs
        List<Long> clientIds = jobs.stream()
                .map(Job::getClientId)
                .distinct()
                .toList();
        
        // Fetch client information in batch
        Map<Long, UserResponseDto> clientsMap = authServiceClient.getUsersByIds(clientIds)
                .stream()
                .collect(Collectors.toMap(UserResponseDto::getId, user -> user));
        
        // Map jobs to enhanced DTOs with client information
        return jobs.stream()
                .map(job -> {
                    UserResponseDto clientInfo = clientsMap.get(job.getClientId());
                    
                    // Create client info DTO
                    JobWithClientResponseDto.ClientInfo client = null;
                    if (clientInfo != null) {
                        client = JobWithClientResponseDto.ClientInfo.builder()
                                .id(clientInfo.getId())
                                .name(clientInfo.getName())
                                .email(clientInfo.getEmail())
                                .handle(clientInfo.getHandle())
                                .country(clientInfo.getCountry())
                                .timezone(clientInfo.getTimezone())
                                .isActive(clientInfo.getIsActive())
                                .build();
                    }
                    
                    // Create enhanced job DTO
                    return JobWithClientResponseDto.builder()
                            .id(job.getId())
                            .clientId(job.getClientId())
                            .projectName(job.getProjectName())
                            .description(job.getDescription())
                            .budgetType(job.getBudgetType() != null ? job.getBudgetType().name() : null)
                            .minBudgetCents(job.getMinBudgetCents())
                            .maxBudgetCents(job.getMaxBudgetCents())
                            .category(job.getCategory())
                            .skills(job.getSkills())
                            .isUrgent(job.getIsUrgent())
                            .ndaRequired(job.getNdaRequired())
                            .ipAssignment(job.getIpAssignment())
                            .status(job.getStatus() != null ? job.getStatus().name() : null)
                            .createdAt(job.getCreatedAt())
                            .updatedAt(job.getUpdatedAt())
                            .editedAt(job.getEditedAt())
                            .client(client)
                            .build();
                })
                .toList();
    }
}
