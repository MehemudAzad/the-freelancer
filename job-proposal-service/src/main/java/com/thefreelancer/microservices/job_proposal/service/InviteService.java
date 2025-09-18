package com.thefreelancer.microservices.job_proposal.service;

import com.thefreelancer.microservices.job_proposal.client.AuthServiceClient;
import com.thefreelancer.microservices.job_proposal.dto.*;
import com.thefreelancer.microservices.job_proposal.exception.ResourceNotFoundException;
import com.thefreelancer.microservices.job_proposal.mapper.InviteMapper;
import com.thefreelancer.microservices.job_proposal.model.Invite;
import com.thefreelancer.microservices.job_proposal.model.Job;
import com.thefreelancer.microservices.job_proposal.repository.InviteRepository;
import com.thefreelancer.microservices.job_proposal.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InviteService {
    
    private final InviteRepository inviteRepository;
    private final JobRepository jobRepository;
    private final InviteMapper inviteMapper;
    private final AuthServiceClient authServiceClient;
    
    @Autowired
    @Lazy
    private ContractService contractService;
    
    @Transactional
    public InviteResponseDto createInvite(InviteCreateDto createDto) {
        log.info("Creating invite for job {} to freelancer {}", createDto.getJobId(), createDto.getFreelancerId());
        
        // Validate job exists
        Job job = jobRepository.findById(createDto.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + createDto.getJobId()));
        
        // Check if invite already exists
        if (inviteRepository.existsByJob_IdAndFreelancerId(createDto.getJobId(), createDto.getFreelancerId())) {
            throw new IllegalArgumentException("Invite already exists for this job and freelancer");
        }
        
        // Validate freelancer exists
        Optional<UserResponseDto> freelancer = authServiceClient.getUserById(createDto.getFreelancerId());
        if (freelancer.isEmpty()) {
            throw new ResourceNotFoundException("Freelancer not found with id: " + createDto.getFreelancerId());
        }
        
        // Create invite
        Invite invite = inviteMapper.toEntity(createDto);
        invite.setJob(job); // Set the Job entity
        invite.setStatus(Invite.InviteStatus.SENT);
        
        Invite savedInvite = inviteRepository.save(invite);
        log.info("Created invite with id: {}", savedInvite.getId());
        
        return enrichInviteResponse(savedInvite);
    }
    
    @Transactional(readOnly = true)
    public List<InviteResponseDto> getInvitesByJob(Long jobId) {
        log.info("Fetching invites for job: {}", jobId);
        
        List<Invite> invites = inviteRepository.findByJob_Id(jobId);
        return invites.stream()
                .map(this::enrichInviteResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<InviteResponseDto> getInvitesForFreelancer(Long freelancerId) {
        log.info("Fetching invites for freelancer: {}", freelancerId);
        
        List<Invite> invites = inviteRepository.findByFreelancerId(freelancerId);
        return invites.stream()
                .map(this::enrichInviteResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<InviteResponseDto> getInvitesByClient(Long clientId) {
        log.info("Fetching invites sent by client: {}", clientId);
        
        List<Invite> invites = inviteRepository.findByClientId(clientId);
        return invites.stream()
                .map(this::enrichInviteResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public InviteResponseDto getInviteById(Long id) {
        log.info("Fetching invite with id: {}", id);
        
        Invite invite = inviteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found with id: " + id));
        
        return enrichInviteResponse(invite);
    }
    
    @Transactional
    public InviteResponseDto updateInviteStatus(Long id, InviteUpdateDto updateDto) {
        log.info("Updating invite {} status to: {}", id, updateDto.getStatus());
        
        Invite invite = inviteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found with id: " + id));
        
        // Validate status transition
        Invite.InviteStatus newStatus;
        try {
            newStatus = Invite.InviteStatus.valueOf(updateDto.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + updateDto.getStatus());
        }
        
        // Only allow certain status transitions
        if (invite.getStatus() != Invite.InviteStatus.SENT) {
            throw new IllegalArgumentException("Can only update status of SENT invites");
        }
        
        if (newStatus != Invite.InviteStatus.ACCEPTED && newStatus != Invite.InviteStatus.DECLINED) {
            throw new IllegalArgumentException("Can only accept or decline invites");
        }
        
        // If invitation is being accepted, create a contract automatically
        if (newStatus == Invite.InviteStatus.ACCEPTED) {
            log.info("Invitation {} accepted - creating contract automatically", id);
            
            // Get job details for contract creation
            Job job = invite.getJob(); // Use the relationship
            if (job == null) {
                throw new IllegalStateException("Job relationship is null for invite: " + id);
            }
            
            // Create contract request based on invitation
            // Use max budget if available, otherwise min budget, otherwise default amount
            Long totalAmountCents;
            if (job.getMaxBudgetCents() != null) {
                totalAmountCents = job.getMaxBudgetCents().longValue();
            } else if (job.getMinBudgetCents() != null) {
                totalAmountCents = job.getMinBudgetCents().longValue();
            } else {
                // Default amount if no budget is set (e.g., $1000 = 100000 cents)
                totalAmountCents = 100000L;
                log.warn("No budget found for job {}, using default amount: {}", job.getId(), totalAmountCents);
            }
            
            ContractCreateDto contractRequest = ContractCreateDto.builder()
                    .jobId(job.getId())
                    .freelancerId(invite.getFreelancerId())
                    .clientId(invite.getClientId())
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(30)) // Default 30 days, can be adjusted
                    .totalAmountCents(totalAmountCents)
                    .build();
            
            try {
                // Create the contract (this will also invalidate other invites for the job)
                ContractResponseDto contract = contractService.createContractFromInvite(contractRequest);
                log.info("Successfully created contract {} from accepted invitation {}", contract.getId(), id);
                
                // Update the invite status
                invite.setStatus(newStatus);
                Invite savedInvite = inviteRepository.save(invite);
                
                log.info("Updated invite {} status to: {} and created contract {}", id, newStatus, contract.getId());
                return enrichInviteResponse(savedInvite);
                
            } catch (Exception e) {
                log.error("Failed to create contract from accepted invitation {}: {}", id, e.getMessage());
                throw new IllegalStateException("Failed to create contract from accepted invitation: " + e.getMessage());
            }
        } else {
            // For declined invitations, just update the status
            invite.setStatus(newStatus);
            Invite savedInvite = inviteRepository.save(invite);
            
            log.info("Updated invite {} status to: {}", id, newStatus);
            return enrichInviteResponse(savedInvite);
        }
    }
    
    @Transactional
    public void deleteInvite(Long id) {
        log.info("Deleting invite with id: {}", id);
        
        Invite invite = inviteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found with id: " + id));
        
        // Only allow deletion of SENT invites
        if (invite.getStatus() != Invite.InviteStatus.SENT) {
            throw new IllegalArgumentException("Can only delete SENT invites");
        }
        
        inviteRepository.delete(invite);
        log.info("Deleted invite with id: {}", id);
    }
    
    /**
     * Invalidate all pending invites for a job when a contract is created
     * This is called internally when a contract is created either from proposal or invitation
     */
    @Transactional
    public void invalidateInvitesForJob(Long jobId, String reason) {
        log.info("Invalidating all pending invites for job {} - reason: {}", jobId, reason);
        
        List<Invite> pendingInvites = inviteRepository.findByJob_IdAndStatus(jobId, Invite.InviteStatus.SENT);
        
        if (!pendingInvites.isEmpty()) {
            int invalidatedCount = 0;
            for (Invite invite : pendingInvites) {
                invite.setStatus(Invite.InviteStatus.EXPIRED);
                invalidatedCount++;
                log.debug("Invalidating invite {} for freelancer {} on job {}", 
                        invite.getId(), invite.getFreelancerId(), jobId);
            }
            
            inviteRepository.saveAll(pendingInvites);
            log.info("Successfully invalidated {} invites for job {} - {}", invalidatedCount, jobId, reason);
        } else {
            log.info("No pending invites found for job {} to invalidate", jobId);
        }
    }
    
    private InviteResponseDto enrichInviteResponse(Invite invite) {
        InviteResponseDto response = inviteMapper.toResponseDto(invite);
        
        // Enrich with client name
        Optional<UserResponseDto> client = authServiceClient.getUserById(invite.getClientId());
        client.ifPresent(user -> response.setClientName(user.getName()));
        
        // Enrich with freelancer name
        Optional<UserResponseDto> freelancer = authServiceClient.getUserById(invite.getFreelancerId());
        freelancer.ifPresent(user -> response.setFreelancerName(user.getName()));
        
        return response;
    }
}