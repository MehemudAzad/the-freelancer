package com.thefreelancer.microservices.job_proposal.mapper;

import com.thefreelancer.microservices.job_proposal.dto.JobMilestoneCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.JobMilestoneResponseDto;
import com.thefreelancer.microservices.job_proposal.dto.JobMilestoneUpdateDto;
import com.thefreelancer.microservices.job_proposal.model.Job;
import com.thefreelancer.microservices.job_proposal.model.JobMilestone;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface JobMilestoneMapper {
    
    @Mapping(source = "job.id", target = "jobId")
    JobMilestoneResponseDto toResponseDto(JobMilestone jobMilestone);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "job", source = "jobId", qualifiedByName = "jobIdToJob")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    JobMilestone toEntity(JobMilestoneCreateDto createDto, Long jobId);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(JobMilestoneUpdateDto updateDto, @MappingTarget JobMilestone jobMilestone);
    
    @Named("jobIdToJob")
    default Job jobIdToJob(Long jobId) {
        if (jobId == null) return null;
        Job job = new Job();
        job.setId(jobId);
        return job;
    }
}
