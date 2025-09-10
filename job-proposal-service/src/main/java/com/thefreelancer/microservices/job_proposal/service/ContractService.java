package com.thefreelancer.microservices.job_proposal.service;

import com.thefreelancer.microservices.job_proposal.dto.*;
import com.thefreelancer.microservices.job_proposal.model.*;
import com.thefreelancer.microservices.job_proposal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractMilestoneRepository contractMilestoneRepository;
    private final JobRepository jobRepository;
    private final ProposalRepository proposalRepository;
    private final ProposalMilestoneRepository proposalMilestoneRepository;

    /**
     * Create a contract from an accepted proposal
     */
    @Transactional
    public ContractResponseDto createContract(ContractCreateDto createDto, String userId) {
        log.info("Creating contract for job: {} and proposal: {}", createDto.getJobId(), createDto.getProposalId());

        // Validate job exists and user is the client
        Job job = jobRepository.findById(createDto.getJobId())
            .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        
        if (!job.getClientId().equals(userId)) {
            throw new IllegalArgumentException("Only job owner can create contract");
        }

        // Validate proposal exists and is accepted
        Proposal proposal = proposalRepository.findById(createDto.getProposalId())
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        
        if (!proposal.getJob().getId().equals(createDto.getJobId())) {
            throw new IllegalArgumentException("Proposal does not belong to the specified job");
        }
        
        if (proposal.getStatus() != Proposal.ProposalStatus.ACCEPTED) {
            throw new IllegalArgumentException("Only accepted proposals can be converted to contracts");
        }

        // Check if contract already exists
        if (contractRepository.existsByJobId(createDto.getJobId())) {
            throw new IllegalArgumentException("Contract already exists for this job");
        }

        // Create contract
        Contract contract = new Contract();
        contract.setJob(job);
        contract.setProposal(proposal);
        contract.setClientId(job.getClientId());
        contract.setFreelancerId(proposal.getFreelancerId());
        contract.setTotalAmountCents(proposal.getTotalCents());
        contract.setCurrency(proposal.getCurrency());
        contract.setStatus(Contract.ContractStatus.ACTIVE);
        contract.setStartDate(createDto.getStartDate() != null ? createDto.getStartDate() : LocalDate.now());
        contract.setEndDate(createDto.getEndDate());

        Contract savedContract = contractRepository.save(contract);

        // Create contract milestones from proposal milestones
        List<ProposalMilestone> proposalMilestones = proposalMilestoneRepository.findByProposalIdOrderByOrderIndexAsc(proposal.getId());
        List<ContractMilestone> contractMilestones = IntStream.range(0, proposalMilestones.size())
            .mapToObj(i -> {
                ProposalMilestone pm = proposalMilestones.get(i);
                ContractMilestone cm = new ContractMilestone();
                cm.setContract(savedContract);
                cm.setTitle(pm.getTitle());
                cm.setDescription(pm.getDescription());
                cm.setAmountCents(pm.getAmountCents());
                cm.setCurrency(pm.getCurrency());
                cm.setDueDate(pm.getDueDate());
                cm.setOrderIndex(i + 1);
                cm.setStatus(ContractMilestone.MilestoneStatus.FUNDING_REQUIRED);
                return cm;
            })
            .toList();

        contractMilestoneRepository.saveAll(contractMilestones);

        // Update proposal status to CONTRACTED
        proposal.setStatus(Proposal.ProposalStatus.CONTRACTED);
        proposalRepository.save(proposal);

        log.info("Contract created successfully: {}", savedContract.getId());
        return convertToResponseDto(savedContract, true);
    }

    /**
     * Get contract by ID
     */
    public ContractResponseDto getContract(Long contractId, String userId) {
        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        // Check access - only client or freelancer can view
        if (!contract.getClientId().equals(userId) && !contract.getFreelancerId().equals(userId)) {
            throw new IllegalArgumentException("Access denied");
        }

        return convertToResponseDto(contract, true);
    }

    /**
     * Get user's contracts
     */
    public List<ContractResponseDto> getUserContracts(String userId) {
        List<Contract> contracts = contractRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return contracts.stream()
            .map(contract -> convertToResponseDto(contract, false))
            .toList();
    }

    /**
     * Update contract status
     */
    @Transactional
    public ContractResponseDto updateContractStatus(Long contractId, ContractStatusUpdateDto updateDto, String userId) {
        log.info("Updating contract status: {} to {}", contractId, updateDto.getStatus());

        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        // Check access - only client or freelancer can update
        if (!contract.getClientId().equals(userId) && !contract.getFreelancerId().equals(userId)) {
            throw new IllegalArgumentException("Access denied");
        }

        // Validate status transition
        validateStatusTransition(contract.getStatus(), updateDto.getStatus());

        contract.setStatus(updateDto.getStatus());
        Contract savedContract = contractRepository.save(contract);

        log.info("Contract status updated successfully: {}", contractId);
        return convertToResponseDto(savedContract, false);
    }

    /**
     * Convert Contract entity to DTO
     */
    private ContractResponseDto convertToResponseDto(Contract contract, boolean includeMilestones) {
        ContractResponseDto dto = new ContractResponseDto();
        dto.setId(contract.getId());
        dto.setJobId(contract.getJob().getId());
        dto.setJobTitle(contract.getJob().getTitle());
        dto.setProposalId(contract.getProposal().getId());
        dto.setClientId(contract.getClientId());
        dto.setFreelancerId(contract.getFreelancerId());
        dto.setTotalAmountCents(contract.getTotalAmountCents());
        dto.setCurrency(contract.getCurrency());
        dto.setStatus(contract.getStatus());
        dto.setStartDate(contract.getStartDate());
        dto.setEndDate(contract.getEndDate());
        dto.setCreatedAt(contract.getCreatedAt());
        dto.setUpdatedAt(contract.getUpdatedAt());

        // Set milestone counts
        List<ContractMilestone> milestones = contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(contract.getId());
        dto.setTotalMilestones(milestones.size());
        dto.setCompletedMilestones((int) milestones.stream().filter(m -> m.getStatus() == ContractMilestone.MilestoneStatus.PAID).count());
        dto.setActiveMilestones((int) milestones.stream().filter(m -> 
            m.getStatus() == ContractMilestone.MilestoneStatus.IN_PROGRESS || 
            m.getStatus() == ContractMilestone.MilestoneStatus.SUBMITTED).count());

        // Include milestones if requested
        if (includeMilestones) {
            List<ContractMilestoneResponseDto> milestoneDtos = milestones.stream()
                .map(this::convertMilestoneToResponseDto)
                .toList();
            dto.setMilestones(milestoneDtos);
        }

        return dto;
    }

    /**
     * Convert ContractMilestone entity to DTO
     */
    private ContractMilestoneResponseDto convertMilestoneToResponseDto(ContractMilestone milestone) {
        ContractMilestoneResponseDto dto = new ContractMilestoneResponseDto();
        dto.setId(milestone.getId());
        dto.setContractId(milestone.getContract().getId());
        dto.setTitle(milestone.getTitle());
        dto.setDescription(milestone.getDescription());
        dto.setAmountCents(milestone.getAmountCents());
        dto.setCurrency(milestone.getCurrency());
        dto.setStatus(milestone.getStatus());
        dto.setDueDate(milestone.getDueDate());
        dto.setOrderIndex(milestone.getOrderIndex());
        dto.setSubmittedAt(milestone.getSubmittedAt());
        dto.setAcceptedAt(milestone.getAcceptedAt());
        dto.setRejectedAt(milestone.getRejectedAt());
        dto.setRejectionReason(milestone.getRejectionReason());
        dto.setCreatedAt(milestone.getCreatedAt());
        dto.setUpdatedAt(milestone.getUpdatedAt());
        return dto;
    }

    /**
     * Validate contract status transition
     */
    private void validateStatusTransition(Contract.ContractStatus currentStatus, Contract.ContractStatus newStatus) {
        switch (currentStatus) {
            case ACTIVE:
                if (newStatus != Contract.ContractStatus.PAUSED && 
                    newStatus != Contract.ContractStatus.COMPLETED && 
                    newStatus != Contract.ContractStatus.CANCELLED &&
                    newStatus != Contract.ContractStatus.DISPUTED) {
                    throw new IllegalArgumentException("Invalid status transition from ACTIVE to " + newStatus);
                }
                break;
            case PAUSED:
                if (newStatus != Contract.ContractStatus.ACTIVE && 
                    newStatus != Contract.ContractStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from PAUSED to " + newStatus);
                }
                break;
            case COMPLETED:
            case CANCELLED:
                throw new IllegalArgumentException("Cannot change status from " + currentStatus);
            case DISPUTED:
                if (newStatus != Contract.ContractStatus.ACTIVE && 
                    newStatus != Contract.ContractStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from DISPUTED to " + newStatus);
                }
                break;
        }
    }
}
