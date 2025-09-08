package com.thefreelancer.microservices.job_proposal.mapper;

import com.thefreelancer.microservices.job_proposal.dto.ProposalMilestoneCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.ProposalMilestoneResponseDto;
import com.thefreelancer.microservices.job_proposal.dto.ProposalMilestoneUpdateDto;
import com.thefreelancer.microservices.job_proposal.entity.ProposalMilestone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProposalMilestoneMapper {
    
    /**
     * Convert CreateDto to Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProposalMilestone toEntity(ProposalMilestoneCreateDto createDto);
    
    /**
     * Convert Entity to ResponseDto
     */
    ProposalMilestoneResponseDto toResponseDto(ProposalMilestone milestone);
    
    /**
     * Update existing entity from UpdateDto
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "proposalId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ProposalMilestoneUpdateDto updateDto, @MappingTarget ProposalMilestone milestone);
}
