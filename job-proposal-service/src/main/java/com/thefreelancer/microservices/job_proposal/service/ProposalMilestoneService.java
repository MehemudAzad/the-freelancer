package com.thefreelancer.microservices.job_proposal.service;

import com.thefreelancer.microservices.job_proposal.dto.ProposalMilestoneCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.ProposalMilestoneResponseDto;
import com.thefreelancer.microservices.job_proposal.dto.ProposalMilestoneUpdateDto;
import com.thefreelancer.microservices.job_proposal.model.ProposalMilestone;
import com.thefreelancer.microservices.job_proposal.model.Proposal;
import com.thefreelancer.microservices.job_proposal.mapper.ProposalMilestoneMapper;
import com.thefreelancer.microservices.job_proposal.repository.ProposalMilestoneRepository;
import com.thefreelancer.microservices.job_proposal.repository.ProposalRepository;
import com.thefreelancer.microservices.job_proposal.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProposalMilestoneService {
    
    private final ProposalMilestoneRepository proposalMilestoneRepository;
    private final ProposalRepository proposalRepository;
    private final ProposalMilestoneMapper proposalMilestoneMapper;
    
    public ProposalMilestoneResponseDto createMilestone(Long proposalId, ProposalMilestoneCreateDto createDto) {
        log.info("Creating milestone for proposal: {}", proposalId);
        
        // Validate proposal exists
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with ID: " + proposalId));
        
        // TODO: Add validation for proposal ownership by authenticated user
        
        // Create milestone entity from DTO
        ProposalMilestone milestone = proposalMilestoneMapper.toEntity(createDto);
        milestone.setProposal(proposal); // Set the proposal relationship
        
        ProposalMilestone savedMilestone = proposalMilestoneRepository.save(milestone);
        
        log.info("Milestone created successfully with ID: {}", savedMilestone.getId());
        return proposalMilestoneMapper.toResponseDto(savedMilestone);
    }
    
    @Transactional(readOnly = true)
    public List<ProposalMilestoneResponseDto> getMilestonesByProposalId(Long proposalId) {
        log.info("Fetching milestones for proposal: {}", proposalId);
        
        // Validate proposal exists
        if (!proposalRepository.existsById(proposalId)) {
            throw new ResourceNotFoundException("Proposal not found with ID: " + proposalId);
        }
        
        // TODO: Add validation for proposal access by authenticated user
        
        List<ProposalMilestone> milestones = proposalMilestoneRepository.findByProposalIdOrderByOrderIndexAsc(proposalId);
        
        log.info("Found {} milestones for proposal: {}", milestones.size(), proposalId);
        return milestones.stream()
                .map(proposalMilestoneMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    
    public ProposalMilestoneResponseDto updateMilestone(Long milestoneId, ProposalMilestoneUpdateDto updateDto) {
        log.info("Updating milestone: {}", milestoneId);
        
        ProposalMilestone existingMilestone = proposalMilestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with ID: " + milestoneId));
        
        // TODO: Add validation for milestone ownership by authenticated user
        
        proposalMilestoneMapper.updateEntityFromDto(updateDto, existingMilestone);
        ProposalMilestone updatedMilestone = proposalMilestoneRepository.save(existingMilestone);
        
        log.info("Milestone updated successfully: {}", milestoneId);
        return proposalMilestoneMapper.toResponseDto(updatedMilestone);
    }
    
    public void deleteMilestone(Long milestoneId) {
        log.info("Deleting milestone: {}", milestoneId);
        
        ProposalMilestone existingMilestone = proposalMilestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with ID: " + milestoneId));
        
        // TODO: Add validation for milestone ownership by authenticated user
        
        proposalMilestoneRepository.delete(existingMilestone);
        log.info("Milestone deleted successfully: {}", milestoneId);
    }
    
    @Transactional(readOnly = true)
    public ProposalMilestoneResponseDto getMilestoneById(Long milestoneId) {
        log.info("Fetching milestone: {}", milestoneId);
        
        ProposalMilestone milestone = proposalMilestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with ID: " + milestoneId));
        
        return proposalMilestoneMapper.toResponseDto(milestone);
    }
}
