package com.thefreelancer.microservices.job_proposal.mapper;

import com.thefreelancer.microservices.job_proposal.dto.InviteCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.InviteResponseDto;
import com.thefreelancer.microservices.job_proposal.model.Invite;
import com.thefreelancer.microservices.job_proposal.model.Job;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface InviteMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "job", source = "jobId", qualifiedByName = "jobIdToJob")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Invite toEntity(InviteCreateDto dto);
    
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.projectName")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "clientName", ignore = true) // Will be populated by service
    @Mapping(target = "freelancerName", ignore = true) // Will be populated by service
    InviteResponseDto toResponseDto(Invite invite);
    
    @Named("jobIdToJob")
    default Job jobIdToJob(Long jobId) {
        if (jobId == null) {
            return null;
        }
        Job job = new Job();
        job.setId(jobId);
        return job;
    }
    
    @Named("statusToString")
    default String statusToString(Invite.InviteStatus status) {
        return status != null ? status.name() : null;
    }
}