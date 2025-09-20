package com.thefreelancer.microservices.job_proposal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thefreelancer.microservices.job_proposal.dto.*;
import com.thefreelancer.microservices.job_proposal.dto.workspace.RoomCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.workspace.RoomResponseDto;
import com.thefreelancer.microservices.job_proposal.exception.ResourceNotFoundException;
import com.thefreelancer.microservices.job_proposal.model.*;
import com.thefreelancer.microservices.job_proposal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {
    
    private final ContractRepository contractRepository;
    private final ContractMilestoneRepository contractMilestoneRepository;
    private final JobRepository jobRepository;
    private final ProposalRepository proposalRepository;
    private final ProposalMilestoneRepository proposalMilestoneRepository;
    private final ObjectMapper objectMapper;
    private final WorkspaceClient workspaceClient;

    /**
     * Create a new contract from an accepted proposal
     */
    @Transactional
    public ContractResponseDto createContract(ContractCreateDto request) {
        log.info("Creating contract from proposal: {}", request.getProposalId());
        
        // Validate the job and proposal exist and match
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + request.getJobId()));
        
        Proposal proposal = proposalRepository.findById(request.getProposalId())
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + request.getProposalId()));
        
        // Verify the proposal belongs to the job
        if (!proposal.getJob().getId().equals(job.getId())) {
            throw new IllegalArgumentException("Proposal does not belong to the specified job");
        }
        
        // Verify job is still open
        if (job.getStatus() != Job.JobStatus.OPEN) {
            throw new IllegalStateException("Cannot create contract for job that is not open");
        }
        
        // Create the contract
        Contract contract = new Contract();
        contract.setJob(job);
        contract.setProposal(proposal);
        contract.setClientId(job.getClientId());
        contract.setFreelancerId(proposal.getFreelancerId());
        
        // Set total amount from the request or fallback to proposal total
        if (request.getTotalAmountCents() != null) {
            contract.setTotalAmountCents(java.math.BigInteger.valueOf(request.getTotalAmountCents()));
        } else {
            contract.setTotalAmountCents(proposal.getTotalCents());
        }
        
        // Currency is always USD - set explicitly
        contract.setCurrency("USD");
        
        contract.setStatus(Contract.ContractStatus.ACTIVE);
        contract.setStartDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now());
        contract.setEndDate(request.getEndDate());
        contract.setPaymentModel(Contract.PaymentModel.FIXED);
        
        // Set terms as JSON
        if (request.getTerms() != null) {
            try {
                contract.setTermsJson(objectMapper.writeValueAsString(request.getTerms()));
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize contract terms", e);
            }
        }
        
        // Save the contract first
        Contract savedContract = contractRepository.save(contract);
        log.info("Contract created with ID: {}", savedContract.getId());
        
        // Copy milestones from proposal to contract
        List<ProposalMilestone> proposalMilestones = proposalMilestoneRepository.findByProposalIdOrderByOrderIndexAsc(proposal.getId());
        
        if (!proposalMilestones.isEmpty()) {
            log.info("Copying {} milestones from proposal to contract", proposalMilestones.size());
            
            List<ContractMilestone> contractMilestones = proposalMilestones.stream()
                .map(proposalMilestone -> {
                    ContractMilestone contractMilestone = new ContractMilestone();
                    contractMilestone.setContract(savedContract);
                    contractMilestone.setTitle(proposalMilestone.getTitle());
                    contractMilestone.setDescription(proposalMilestone.getDescription());
                    contractMilestone.setAmountCents(proposalMilestone.getAmountCents());
                    contractMilestone.setCurrency("USD"); // Always USD
                    contractMilestone.setDueDate(proposalMilestone.getDueDate());
                    contractMilestone.setOrderIndex(proposalMilestone.getOrderIndex());
                    contractMilestone.setStatus(ContractMilestone.MilestoneStatus.PENDING);
                    return contractMilestone;
                })
                .collect(java.util.stream.Collectors.toList());
            
            contractMilestoneRepository.saveAll(contractMilestones);
            log.info("Successfully created {} contract milestones", contractMilestones.size());
        } else {
            log.warn("No milestones found in proposal {} - contract created without milestones", proposal.getId());
        }
        
        // Update proposal status to CONTRACTED
        proposal.setStatus(Proposal.ProposalStatus.CONTRACTED);
        proposalRepository.save(proposal);
        
        // Update job status to IN_PROGRESS
        job.setStatus(Job.JobStatus.IN_PROGRESS);
        jobRepository.save(job);
        
        // Create workspace room for the contract
        try {
            RoomCreateDto roomCreateDto = RoomCreateDto.builder()
                .contractId(savedContract.getId())
                .jobTitle(job.getProjectName())
                .clientId(savedContract.getClientId().toString())
                .freelancerId(savedContract.getFreelancerId().toString())
                .build();
            
            RoomResponseDto roomResponse = workspaceClient.createRoom(roomCreateDto);
            log.info("Successfully created workspace room {} for contract {}", 
                    roomResponse.getId(), savedContract.getId());
        } catch (Exception e) {
            log.error("Failed to create workspace room for contract {}: {}", 
                    savedContract.getId(), e.getMessage(), e);
            // Don't fail the contract creation if room creation fails
            // The room can be created manually later if needed
        }
        
        log.info("Contract creation completed successfully: {}", savedContract.getId());
        return convertToResponseDto(savedContract, true); // Include milestones in response
    }    /**
     * Get contract by ID
     */
    public ContractResponseDto getContract(Long contractId, String userId) {
        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        // Convert String userId to Long for comparison
        Long userIdLong = Long.parseLong(userId);

        // Check access - only client or freelancer can view
        if (!contract.getClientId().equals(userIdLong) && !contract.getFreelancerId().equals(userIdLong)) {
            throw new IllegalArgumentException("Access denied");
        }

        return convertToResponseDto(contract, true);
    }

    /**
     * Get user's contracts
     */
    public List<ContractResponseDto> getUserContracts(Long userId) {
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
        log.info("user id {}", userId);
        log.info("Fetched contract: {} with current status: {} clientId: {}", contractId, contract.getStatus(), contract.getClientId());
        
        // Convert String userId to Long for comparison
        Long userIdLong = Long.parseLong(userId);
        
        // Check access - only client can update
        if (!contract.getClientId().equals(userIdLong)) {
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
    dto.setJobTitle(contract.getJob().getProjectName());
        dto.setProposalId(contract.getProposal().getId());
        dto.setClientId(contract.getClientId());
        dto.setFreelancerId(contract.getFreelancerId());
        dto.setTotalAmountCents(contract.getTotalAmountCents());
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
