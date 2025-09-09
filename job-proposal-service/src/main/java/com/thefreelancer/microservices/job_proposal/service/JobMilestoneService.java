package com.thefreelancer.microservices.job_proposal.service;

import com.thefreelancer.microservices.job_proposal.dto.JobMilestoneCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.JobMilestoneResponseDto;
import com.thefreelancer.microservices.job_proposal.dto.JobMilestoneUpdateDto;
import com.thefreelancer.microservices.job_proposal.mapper.JobMilestoneMapper;
import com.thefreelancer.microservices.job_proposal.model.JobMilestone;
import com.thefreelancer.microservices.job_proposal.repository.JobMilestoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobMilestoneService {
    
    private final JobMilestoneRepository jobMilestoneRepository;
    private final JobMilestoneMapper jobMilestoneMapper;
    
    @Transactional(readOnly = true)
    public List<JobMilestoneResponseDto> getMilestonesByJobId(Long jobId) {
        log.info("Fetching milestones for job: {}", jobId);
        
        List<JobMilestone> milestones = jobMilestoneRepository.findByJobIdOrderByOrderIndex(jobId);
        
        return milestones.stream()
                .map(jobMilestoneMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public JobMilestoneResponseDto createMilestone(Long jobId, JobMilestoneCreateDto createDto) {
        log.info("Creating milestone for job: {} with title: {}", jobId, createDto.getTitle());
        
        // TODO: Validate that job exists and user owns it
        // TODO: Check if order index is already taken
        
        JobMilestone milestone = jobMilestoneMapper.toEntity(createDto, jobId);
        JobMilestone savedMilestone = jobMilestoneRepository.save(milestone);
        
        log.info("Milestone created successfully with ID: {}", savedMilestone.getId());
        return jobMilestoneMapper.toResponseDto(savedMilestone);
    }
    
    @Transactional
    public JobMilestoneResponseDto updateMilestone(Long jobId, Long milestoneId, JobMilestoneUpdateDto updateDto) {
        log.info("Updating milestone: {} for job: {}", milestoneId, jobId);
        
        JobMilestone milestone = jobMilestoneRepository.findByJobIdAndId(jobId, milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found: " + milestoneId + " for job: " + jobId));
        
        // TODO: Validate that user owns the job
        
        jobMilestoneMapper.updateEntityFromDto(updateDto, milestone);
        JobMilestone updatedMilestone = jobMilestoneRepository.save(milestone);
        
        log.info("Milestone updated successfully: {}", milestoneId);
        return jobMilestoneMapper.toResponseDto(updatedMilestone);
    }
    
    @Transactional
    public void deleteMilestone(Long jobId, Long milestoneId) {
        log.info("Deleting milestone: {} for job: {}", milestoneId, jobId);
        
        JobMilestone milestone = jobMilestoneRepository.findByJobIdAndId(jobId, milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found: " + milestoneId + " for job: " + jobId));
        
        // TODO: Validate that user owns the job
        
        jobMilestoneRepository.delete(milestone);
        log.info("Milestone deleted successfully: {}", milestoneId);
    }
    
    @Transactional
    public void deleteAllMilestonesByJobId(Long jobId) {
        log.info("Deleting all milestones for job: {}", jobId);
        
        jobMilestoneRepository.deleteByJobId(jobId);
        log.info("All milestones deleted for job: {}", jobId);
    }
}
