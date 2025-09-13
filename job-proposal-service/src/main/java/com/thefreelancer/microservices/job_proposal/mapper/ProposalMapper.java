package com.thefreelancer.microservices.job_proposal.mapper;

import com.thefreelancer.microservices.job_proposal.dto.ProposalCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.ProposalResponseDto;
import com.thefreelancer.microservices.job_proposal.dto.ProposalUpdateDto;
import com.thefreelancer.microservices.job_proposal.model.Job;
import com.thefreelancer.microservices.job_proposal.model.Proposal;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
@Component
public interface ProposalMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "job", source = "jobId", qualifiedByName = "jobIdToJob")
    @Mapping(target = "totalCents", source = "proposedRate", qualifiedByName = "rateToCents")
    @Mapping(target = "currency", constant = "USD")
    @Mapping(target = "cover", source = "coverLetter")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "milestones", ignore = true)
    Proposal toEntity(ProposalCreateDto dto);
    
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.projectName")
    @Mapping(target = "proposedRate", source = "totalCents", qualifiedByName = "centsToRate")
    @Mapping(target = "coverLetter", source = "cover")
    @Mapping(target = "freelancerName", constant = "Unknown") // TODO: Get from user service
    @Mapping(target = "submittedAt", source = "createdAt")
    ProposalResponseDto toResponseDto(Proposal entity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "freelancerId", ignore = true)
    @Mapping(target = "totalCents", source = "proposedRate", qualifiedByName = "rateToCents")
    @Mapping(target = "cover", source = "coverLetter")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "milestones", ignore = true)
    void updateEntityFromDto(ProposalUpdateDto dto, @MappingTarget Proposal entity);
    
    @Named("jobIdToJob")
    default Job jobIdToJob(Long jobId) {
        if (jobId == null) return null;
        Job job = new Job();
        job.setId(jobId);
        return job;
    }
    
    @Named("rateToCents")
    default BigInteger rateToCents(java.math.BigDecimal rate) {
        if (rate == null) return null;
        return rate.multiply(java.math.BigDecimal.valueOf(100)).toBigInteger();
    }
    
    @Named("centsToRate")
    default java.math.BigDecimal centsToRate(BigInteger cents) {
        if (cents == null) return null;
        return new java.math.BigDecimal(cents).divide(java.math.BigDecimal.valueOf(100));
    }
}
