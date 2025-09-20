package com.thefreelancer.microservices.job_proposal.mapper;

import com.thefreelancer.microservices.job_proposal.dto.ProposalMilestoneCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.ProposalMilestoneResponseDto;
import com.thefreelancer.microservices.job_proposal.dto.ProposalMilestoneUpdateDto;
import com.thefreelancer.microservices.job_proposal.model.ProposalMilestone;
import com.thefreelancer.microservices.job_proposal.model.Proposal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.math.BigDecimal;
import java.math.BigInteger;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProposalMilestoneMapper {
    
    /**
     * Convert CreateDto to Entity (proposal will be set separately)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "proposal", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProposalMilestone toEntity(ProposalMilestoneCreateDto createDto);
    
    /**
     * Convert Entity to ResponseDto
     */
    @Mapping(target = "proposalId", source = "proposal.id")
    ProposalMilestoneResponseDto toResponseDto(ProposalMilestone milestone);
    
    /**
     * Update existing entity from UpdateDto
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "proposal", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ProposalMilestoneUpdateDto updateDto, @MappingTarget ProposalMilestone milestone);
    
    /**
     * Map Long to Proposal reference
     */
    default Proposal map(Long proposalId) {
        if (proposalId == null) {
            return null;
        }
        return Proposal.builder()
                .id(proposalId)
                .build();
    }
    
    /**
     * Map BigDecimal to BigInteger 
     */
    default BigInteger map(BigDecimal value) {
        return value != null ? value.toBigInteger() : null;
    }
    
    /**
     * Map BigInteger to BigDecimal
     */
    default BigDecimal map(BigInteger value) {
        return value != null ? new BigDecimal(value) : null;
    }
}
