package com.thefreelancer.microservices.job_proposal.mapper;

import com.thefreelancer.microservices.job_proposal.dto.JobCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.JobResponseDto;
import com.thefreelancer.microservices.job_proposal.dto.JobUpdateDto;
import com.thefreelancer.microservices.job_proposal.model.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface JobMapper {
    
    @Mapping(target = "budgetType", source = "budgetType", qualifiedByName = "budgetTypeToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    JobResponseDto toResponseDto(Job job);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "proposals", ignore = true)
    @Mapping(target = "invites", ignore = true)
    Job toEntity(JobCreateDto createDto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clientId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "proposals", ignore = true)
    @Mapping(target = "invites", ignore = true)
    void updateEntityFromDto(JobUpdateDto updateDto, @MappingTarget Job job);
    
    @Named("budgetTypeToString")
    default String budgetTypeToString(Job.BudgetType budgetType) {
        return budgetType != null ? budgetType.toString() : null;
    }
    
    @Named("statusToString")
    default String statusToString(Job.JobStatus status) {
        return status != null ? status.toString() : null;
    }
}
