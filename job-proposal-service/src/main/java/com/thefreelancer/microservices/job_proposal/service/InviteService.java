package com.thefreelancer.microservices.job_proposal.service;

import com.thefreelancer.microservices.job_proposal.client.AuthServiceClient;
import com.thefreelancer.microservices.job_proposal.dto.InviteCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.InviteResponseDto;
import com.thefreelancer.microservices.job_proposal.dto.InviteUpdateDto;
import com.thefreelancer.microservices.job_proposal.dto.UserResponseDto;
import com.thefreelancer.microservices.job_proposal.exception.ResourceNotFoundException;
import com.thefreelancer.microservices.job_proposal.mapper.InviteMapper;
import com.thefreelancer.microservices.job_proposal.model.Invite;
import com.thefreelancer.microservices.job_proposal.repository.InviteRepository;
import com.thefreelancer.microservices.job_proposal.repository.JobRepository;
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
public class InviteService {
    
    private final InviteRepository inviteRepository;
    private final JobRepository jobRepository;
    private final InviteMapper inviteMapper;
    private final AuthServiceClient authServiceClient;
    
    @Transactional
    public InviteResponseDto createInvite(InviteCreateDto createDto) {
        log.info("Creating invite for job {} to freelancer {}", createDto.getJobId(), createDto.getFreelancerId());
        
        // Validate job exists
        jobRepository.findById(createDto.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + createDto.getJobId()));
        
        // Check if invite already exists
        if (inviteRepository.existsByJobIdAndFreelancerId(createDto.getJobId(), createDto.getFreelancerId())) {
            throw new IllegalArgumentException("Invite already exists for this job and freelancer");
        }
        
        // Validate freelancer exists
        Optional<UserResponseDto> freelancer = authServiceClient.getUserById(createDto.getFreelancerId());
        if (freelancer.isEmpty()) {
            throw new ResourceNotFoundException("Freelancer not found with id: " + createDto.getFreelancerId());
        }
        
        // Create invite
        Invite invite = inviteMapper.toEntity(createDto);
        invite.setStatus(Invite.InviteStatus.SENT);
        
        Invite savedInvite = inviteRepository.save(invite);
        log.info("Created invite with id: {}", savedInvite.getId());
        
        return enrichInviteResponse(savedInvite);
    }
    
    @Transactional(readOnly = true)
    public List<InviteResponseDto> getInvitesByJob(Long jobId) {
        log.info("Fetching invites for job: {}", jobId);
        
        List<Invite> invites = inviteRepository.findByJobId(jobId);
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
        
        invite.setStatus(newStatus);
        Invite savedInvite = inviteRepository.save(invite);
        
        log.info("Updated invite {} status to: {}", id, newStatus);
        return enrichInviteResponse(savedInvite);
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