package com.thefreelancer.microservices.job_proposal.service;

import com.thefreelancer.microservices.job_proposal.dto.*;
import com.thefreelancer.microservices.job_proposal.model.*;
import com.thefreelancer.microservices.job_proposal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractMilestoneService {

    private final ContractMilestoneRepository contractMilestoneRepository;
    private final ContractRepository contractRepository;

    /**
     * Add milestone to contract
     */
    @Transactional
    public ContractMilestoneResponseDto addMilestone(Long contractId, ContractMilestoneCreateDto createDto, String userId) {
        log.info("Adding milestone to contract: {}", contractId);

        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        // Check access - only client can add milestones
        if (!contract.getClientId().equals(userId)) {
            throw new IllegalArgumentException("Only contract client can add milestones");
        }

        // Contract must be active
        if (contract.getStatus() != Contract.ContractStatus.ACTIVE) {
            throw new IllegalArgumentException("Can only add milestones to active contracts");
        }

        // Get next order index
        List<ContractMilestone> existingMilestones = contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(contractId);
        int nextOrderIndex = existingMilestones.isEmpty() ? 1 : 
            existingMilestones.get(existingMilestones.size() - 1).getOrderIndex() + 1;

        ContractMilestone milestone = new ContractMilestone();
        milestone.setContract(contract);
        milestone.setTitle(createDto.getTitle());
        milestone.setDescription(createDto.getDescription());
        milestone.setAmountCents(createDto.getAmountCents());
        milestone.setCurrency("USD"); // Always USD
        milestone.setDueDate(createDto.getDueDate());
        milestone.setOrderIndex(createDto.getOrderIndex() != null ? createDto.getOrderIndex() : nextOrderIndex);
        milestone.setStatus(ContractMilestone.MilestoneStatus.FUNDING_REQUIRED);

        ContractMilestone savedMilestone = contractMilestoneRepository.save(milestone);
        log.info("Milestone added successfully: {}", savedMilestone.getId());

        return convertToResponseDto(savedMilestone);
    }

    /**
     * Get contract milestones
     */
    public List<ContractMilestoneResponseDto> getContractMilestones(Long contractId, String userId) {
        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        // Check access - only client or freelancer can view
        if (!contract.getClientId().equals(userId) && !contract.getFreelancerId().equals(userId)) {
            throw new IllegalArgumentException("Access denied");
        }

        List<ContractMilestone> milestones = contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(contractId);
        return milestones.stream()
            .map(this::convertToResponseDto)
            .toList();
    }

    /**
     * Submit milestone deliverable (Freelancer action)
     */
    @Transactional
    public ContractMilestoneResponseDto submitMilestone(Long milestoneId, String userId) {
        log.info("Submitting milestone: {}", milestoneId);

        ContractMilestone milestone = contractMilestoneRepository.findById(milestoneId)
            .orElseThrow(() -> new IllegalArgumentException("Milestone not found"));

        // Check access - only freelancer can submit
        if (!milestone.getContract().getFreelancerId().equals(userId)) {
            throw new IllegalArgumentException("Only assigned freelancer can submit milestone");
        }

        // Validate current status
        if (milestone.getStatus() != ContractMilestone.MilestoneStatus.IN_PROGRESS &&
            milestone.getStatus() != ContractMilestone.MilestoneStatus.REJECTED) {
            throw new IllegalArgumentException("Milestone must be IN_PROGRESS or REJECTED to submit");
        }

        milestone.setStatus(ContractMilestone.MilestoneStatus.SUBMITTED);
        milestone.setSubmittedAt(LocalDateTime.now());
        milestone.setRejectedAt(null); // Clear previous rejection
        milestone.setRejectionReason(null);

        ContractMilestone savedMilestone = contractMilestoneRepository.save(milestone);
        log.info("Milestone submitted successfully: {}", milestoneId);

        return convertToResponseDto(savedMilestone);
    }

    /**
     * Accept milestone (Client action)
     */
    @Transactional
    public ContractMilestoneResponseDto acceptMilestone(Long milestoneId, String userId) {
        log.info("Accepting milestone: {}", milestoneId);

        ContractMilestone milestone = contractMilestoneRepository.findById(milestoneId)
            .orElseThrow(() -> new IllegalArgumentException("Milestone not found"));

        // Check access - only client can accept
        if (!milestone.getContract().getClientId().equals(userId)) {
            throw new IllegalArgumentException("Only contract client can accept milestone");
        }

        // Validate current status
        if (milestone.getStatus() != ContractMilestone.MilestoneStatus.SUBMITTED) {
            throw new IllegalArgumentException("Only submitted milestones can be accepted");
        }

        milestone.setStatus(ContractMilestone.MilestoneStatus.ACCEPTED);
        milestone.setAcceptedAt(LocalDateTime.now());

        ContractMilestone savedMilestone = contractMilestoneRepository.save(milestone);
        log.info("Milestone accepted successfully: {}", milestoneId);

        // TODO: Trigger payment release event to payment-service
        // publishMilestoneAcceptedEvent(savedMilestone);

        return convertToResponseDto(savedMilestone);
    }

    /**
     * Reject milestone (Client action)
     */
    @Transactional
    public ContractMilestoneResponseDto rejectMilestone(Long milestoneId, ContractMilestoneUpdateDto updateDto, String userId) {
        log.info("Rejecting milestone: {}", milestoneId);

        ContractMilestone milestone = contractMilestoneRepository.findById(milestoneId)
            .orElseThrow(() -> new IllegalArgumentException("Milestone not found"));

        // Check access - only client can reject
        if (!milestone.getContract().getClientId().equals(userId)) {
            throw new IllegalArgumentException("Only contract client can reject milestone");
        }

        // Validate current status
        if (milestone.getStatus() != ContractMilestone.MilestoneStatus.SUBMITTED) {
            throw new IllegalArgumentException("Only submitted milestones can be rejected");
        }

        milestone.setStatus(ContractMilestone.MilestoneStatus.REJECTED);
        milestone.setRejectedAt(LocalDateTime.now());
        milestone.setRejectionReason(updateDto.getRejectionReason());
        milestone.setSubmittedAt(null); // Clear submission time

        ContractMilestone savedMilestone = contractMilestoneRepository.save(milestone);
        log.info("Milestone rejected successfully: {}", milestoneId);

        return convertToResponseDto(savedMilestone);
    }

    /**
     * Update milestone status (Generic status update)
     */
    @Transactional
    public ContractMilestoneResponseDto updateMilestoneStatus(Long milestoneId, ContractMilestoneUpdateDto updateDto, String userId) {
        log.info("Updating milestone status: {} to {}", milestoneId, updateDto.getStatus());

        ContractMilestone milestone = contractMilestoneRepository.findById(milestoneId)
            .orElseThrow(() -> new IllegalArgumentException("Milestone not found"));

        // Check access
        if (!milestone.getContract().getClientId().equals(userId) && !milestone.getContract().getFreelancerId().equals(userId)) {
            throw new IllegalArgumentException("Access denied");
        }

        // Validate status transition based on user role
        validateStatusUpdate(milestone, updateDto.getStatus(), userId);

        milestone.setStatus(updateDto.getStatus());

        // Set appropriate timestamps based on new status
        switch (updateDto.getStatus()) {
            case SUBMITTED:
                if (milestone.getContract().getFreelancerId().equals(userId)) {
                    milestone.setSubmittedAt(LocalDateTime.now());
                }
                break;
            case ACCEPTED:
                if (milestone.getContract().getClientId().equals(userId)) {
                    milestone.setAcceptedAt(LocalDateTime.now());
                }
                break;
            case REJECTED:
                if (milestone.getContract().getClientId().equals(userId)) {
                    milestone.setRejectedAt(LocalDateTime.now());
                    milestone.setRejectionReason(updateDto.getRejectionReason());
                }
                break;
            case IN_PROGRESS:
                // Clear previous submission/rejection data
                milestone.setSubmittedAt(null);
                milestone.setRejectedAt(null);
                milestone.setRejectionReason(null);
                break;
            case PENDING:
            case FUNDING_REQUIRED:
            case FUNDED:
            case DISPUTED:
            case PAID:
                // These status changes are typically handled by payment service
                // No specific timestamp updates needed here
                break;
        }

        ContractMilestone savedMilestone = contractMilestoneRepository.save(milestone);
        log.info("Milestone status updated successfully: {}", milestoneId);

        return convertToResponseDto(savedMilestone);
    }

    /**
     * Convert ContractMilestone entity to DTO
     */
    private ContractMilestoneResponseDto convertToResponseDto(ContractMilestone milestone) {
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
     * Validate milestone status update based on user role
     */
    private void validateStatusUpdate(ContractMilestone milestone, ContractMilestone.MilestoneStatus newStatus, String userId) {
        boolean isClient = milestone.getContract().getClientId().equals(userId);
        boolean isFreelancer = milestone.getContract().getFreelancerId().equals(userId);

        switch (newStatus) {
            case SUBMITTED:
                if (!isFreelancer) {
                    throw new IllegalArgumentException("Only freelancer can submit milestone");
                }
                if (milestone.getStatus() != ContractMilestone.MilestoneStatus.IN_PROGRESS &&
                    milestone.getStatus() != ContractMilestone.MilestoneStatus.REJECTED) {
                    throw new IllegalArgumentException("Invalid status transition to SUBMITTED");
                }
                break;
            case ACCEPTED:
                if (!isClient) {
                    throw new IllegalArgumentException("Only client can accept milestone");
                }
                if (milestone.getStatus() != ContractMilestone.MilestoneStatus.SUBMITTED) {
                    throw new IllegalArgumentException("Only submitted milestones can be accepted");
                }
                break;
            case REJECTED:
                if (!isClient) {
                    throw new IllegalArgumentException("Only client can reject milestone");
                }
                if (milestone.getStatus() != ContractMilestone.MilestoneStatus.SUBMITTED) {
                    throw new IllegalArgumentException("Only submitted milestones can be rejected");
                }
                break;
            case IN_PROGRESS:
                if (!isFreelancer) {
                    throw new IllegalArgumentException("Only freelancer can start working on milestone");
                }
                if (milestone.getStatus() != ContractMilestone.MilestoneStatus.FUNDED &&
                    milestone.getStatus() != ContractMilestone.MilestoneStatus.REJECTED) {
                    throw new IllegalArgumentException("Invalid status transition to IN_PROGRESS");
                }
                break;
            case FUNDED:
            case PAID:
                throw new IllegalArgumentException("Status " + newStatus + " is managed by the payment system");
            default:
                throw new IllegalArgumentException("Invalid milestone status: " + newStatus);
        }
    }
}
