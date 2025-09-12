package com.thefreelancer.microservices.job_proposal.service;

import com.thefreelancer.microservices.job_proposal.dto.ProposalCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.ProposalResponseDto;
import com.thefreelancer.microservices.job_proposal.dto.ProposalUpdateDto;
import com.thefreelancer.microservices.job_proposal.model.Job;
import com.thefreelancer.microservices.job_proposal.model.Proposal;
import com.thefreelancer.microservices.job_proposal.repository.JobRepository;
import com.thefreelancer.microservices.job_proposal.repository.ProposalRepository;
import com.thefreelancer.microservices.job_proposal.mapper.ProposalMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProposalService {
    
    private final ProposalRepository proposalRepository;
    private final JobRepository jobRepository;
    private final ProposalMapper proposalMapper;
    
    @Transactional(readOnly = true)
    public List<ProposalResponseDto> getMyProposals(Long freelancerId, String status) {
        log.info("Fetching proposals for freelancer: {} with status: {}", freelancerId, status);
        
        List<Proposal> proposals;
        if (status != null && !status.trim().isEmpty()) {
            try {
                Proposal.ProposalStatus statusEnum = Proposal.ProposalStatus.valueOf(status.toUpperCase());
                proposals = proposalRepository.findByFreelancerIdAndStatusOrderByCreatedAtDesc(freelancerId, statusEnum);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status: {}", status);
                proposals = List.of(); // Return empty list for invalid status
            }
        } else {
            proposals = proposalRepository.findByFreelancerIdOrderByCreatedAtDesc(freelancerId);
        }
        
        return proposals.stream()
                .map(proposalMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ProposalResponseDto createProposal(ProposalCreateDto proposalCreateDto) {
        log.info("Creating new proposal for job: {} by freelancer: {}", 
                proposalCreateDto.getJobId(), proposalCreateDto.getFreelancerId());
        
        // TODO: Check if freelancer already has a proposal for this job
        // TODO: Check if job exists and is still open for proposals
        
        Proposal proposal = proposalMapper.toEntity(proposalCreateDto);
        proposal.setStatus(Proposal.ProposalStatus.SUBMITTED);
        
        Proposal savedProposal = proposalRepository.save(proposal);
        log.info("Proposal created successfully with ID: {}", savedProposal.getId());
        
        return proposalMapper.toResponseDto(savedProposal);
    }
    
    @Transactional
    public ProposalResponseDto updateProposal(Long proposalId, ProposalUpdateDto proposalUpdateDto) {
        log.info("Updating proposal: {}", proposalId);
        
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found: " + proposalId));
        
        // TODO: Add ownership validation
        // TODO: Check if proposal is still in a state that allows updates (e.g., not ACCEPTED)
        
        proposalMapper.updateEntityFromDto(proposalUpdateDto, proposal);
        
        Proposal updatedProposal = proposalRepository.save(proposal);
        log.info("Proposal updated successfully: {}", proposalId);
        
        return proposalMapper.toResponseDto(updatedProposal);
    }
    
    @Transactional
    public void deleteProposal(Long proposalId) {
        log.info("Deleting proposal: {}", proposalId);
        
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found: " + proposalId));
        
        // TODO: Add ownership validation
        // TODO: Check if proposal can be withdrawn (e.g., not ACCEPTED)
        
        proposalRepository.delete(proposal);
        log.info("Proposal deleted successfully: {}", proposalId);
    }
    
    @Transactional(readOnly = true)
    public Optional<ProposalResponseDto> getProposalById(Long proposalId) {
        log.info("Fetching proposal: {}", proposalId);
        
        return proposalRepository.findById(proposalId)
                .map(proposalMapper::toResponseDto);
    }
    
    @Transactional(readOnly = true)
    public List<ProposalResponseDto> getProposalsForJob(Long jobId) {
        log.info("Fetching proposals for job: {}", jobId);
        
        List<Proposal> proposals = proposalRepository.findByJobIdOrderByCreatedAtDesc(jobId);
        
        return proposals.stream()
                .map(proposalMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ProposalResponseDto> getProposalsForJobByClient(Long jobId, Long clientId) {
        log.info("Fetching proposals for job: {} by client: {}", jobId, clientId);
        
        // First validate that the client owns the job
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));
        
        if (!job.getClientId().equals(clientId)) {
            throw new RuntimeException("Access denied: You can only view proposals for your own jobs");
        }
        
        List<Proposal> proposals = proposalRepository.findByJobIdOrderByCreatedAtDesc(jobId);
        
        return proposals.stream()
                .map(proposalMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}
