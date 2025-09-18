package com.thefreelancer.microservices.job_proposal.mapper;

import com.thefreelancer.microservices.job_proposal.dto.*;
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
    @Mapping(target = "cover", source = "coverLetter")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "milestones", ignore = true)
    Proposal toEntity(ProposalCreateDto dto);
    
    @Mapping(target = "id", source = "proposal.id")
    @Mapping(target = "jobId", source = "proposal.job.id")
    @Mapping(target = "jobTitle", source = "proposal.job.projectName")
    @Mapping(target = "proposedRate", source = "proposal.totalCents", qualifiedByName = "centsToRate")
    @Mapping(target = "coverLetter", source = "proposal.cover")
    @Mapping(target = "createdAt", source = "proposal.createdAt", qualifiedByName = "dateToString")
    @Mapping(target = "updatedAt", source = "proposal.updatedAt", qualifiedByName = "dateToString")
    @Mapping(target = "freelancerId", source = "proposal.freelancerId")
    @Mapping(target = "deliveryDays", source = "proposal.deliveryDays")
    @Mapping(target = "status", source = "proposal.status")
    @Mapping(target = "portfolioLinks", constant = "")
    @Mapping(target = "additionalNotes", constant = "")
    @Mapping(target = "contractId", ignore = true)
    @Mapping(target = "freelancerInfo", ignore = true)
    ProposalResponseDto toResponseDto(Proposal proposal);
    
    // New method that includes user information
    @Mapping(target = "id", source = "proposal.id")
    @Mapping(target = "jobId", source = "proposal.job.id")
    @Mapping(target = "jobTitle", source = "proposal.job.projectName")
    @Mapping(target = "proposedRate", source = "proposal.totalCents", qualifiedByName = "centsToRate")
    @Mapping(target = "coverLetter", source = "proposal.cover")
    @Mapping(target = "createdAt", source = "proposal.createdAt", qualifiedByName = "dateToString")
    @Mapping(target = "updatedAt", source = "proposal.updatedAt", qualifiedByName = "dateToString")
    @Mapping(target = "freelancerId", source = "proposal.freelancerId")
    @Mapping(target = "deliveryDays", source = "proposal.deliveryDays")
    @Mapping(target = "status", source = "proposal.status")
    @Mapping(target = "portfolioLinks", constant = "")
    @Mapping(target = "additionalNotes", constant = "")
    @Mapping(target = "contractId", ignore = true)
    @Mapping(target = "freelancerInfo", source = "userInfo", qualifiedByName = "userToFreelancerInfo")
    ProposalResponseDto toResponseDtoWithUserInfo(Proposal proposal, UserResponseDto userInfo);
    
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
    
    @Named("dateToString")
    default String dateToString(java.time.LocalDateTime date) {
        if (date == null) return null;
        return date.toString();
    }
    
    @Named("userToFreelancerInfo")
    default FreelancerInfoDto userToFreelancerInfo(UserResponseDto user) {
        if (user == null) return null;
        FreelancerInfoDto info = new FreelancerInfoDto();
        info.setId(user.getId());
        info.setName(user.getName());
        info.setEmail(user.getEmail());
        info.setProfilePictureUrl(null); // Not available in UserResponseDto
        info.setJoinedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        return info;
    }
}
