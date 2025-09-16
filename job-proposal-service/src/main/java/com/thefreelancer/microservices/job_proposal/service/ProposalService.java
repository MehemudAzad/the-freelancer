package com.thefreelancer.microservices.job_proposal.service;

import com.thefreelancer.microservices.job_proposal.client.AuthServiceClient;
import com.thefreelancer.microservices.job_proposal.dto.ProposalCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.ProposalCreateWithMilestonesDto;
import com.thefreelancer.microservices.job_proposal.dto.ProposalResponseDto;
import com.thefreelancer.microservices.job_proposal.dto.ProposalUpdateDto;
import com.thefreelancer.microservices.job_proposal.dto.UserResponseDto;
import com.thefreelancer.microservices.job_proposal.model.Job;
import com.thefreelancer.microservices.job_proposal.model.Proposal;
import com.thefreelancer.microservices.job_proposal.model.ProposalMilestone;
import com.thefreelancer.microservices.job_proposal.repository.JobRepository;
import com.thefreelancer.microservices.job_proposal.repository.ProposalMilestoneRepository;
import com.thefreelancer.microservices.job_proposal.repository.ProposalRepository;
import com.thefreelancer.microservices.job_proposal.mapper.ProposalMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProposalService {
    
    private final ProposalRepository proposalRepository;
    private final JobRepository jobRepository;
    private final ProposalMilestoneRepository proposalMilestoneRepository;
    private final ProposalMapper proposalMapper;
    private final EventPublisherService eventPublisherService;
    private final AuthServiceClient authServiceClient;
    
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

        // Check if freelancer already has a proposal for this job
        boolean exists = proposalRepository.existsByJobIdAndFreelancerId(
            proposalCreateDto.getJobId(), proposalCreateDto.getFreelancerId());
        if (exists) {
            throw new RuntimeException("You have already submitted a proposal for this job.");
        }

        // Check if job exists and is still open for proposals
        Job job = jobRepository.findById(proposalCreateDto.getJobId())
            .orElseThrow(() -> new RuntimeException("Job not found: " + proposalCreateDto.getJobId()));
        if (job.getStatus() != Job.JobStatus.OPEN) {
            throw new RuntimeException("Proposals can only be submitted to jobs with status OPEN.");
        }

        Proposal proposal = proposalMapper.toEntity(proposalCreateDto);
        proposal.setStatus(Proposal.ProposalStatus.SUBMITTED);

        Proposal savedProposal = proposalRepository.save(proposal);
        log.info("Proposal created successfully with ID: {}", savedProposal.getId());

<<<<<<< Updated upstream
        // Publish Kafka event
        publishProposalSubmittedEvent(savedProposal, job);

        return proposalMapper.toResponseDto(savedProposal);
=======
        // Create response DTO
        ProposalResponseDto responseDto = proposalMapper.toResponseDto(savedProposal);
        
        // Fetch and add freelancer information from auth-service
        try {
            Optional<UserResponseDto> userOpt = authServiceClient.getUserById(savedProposal.getFreelancerId());
            if (userOpt.isPresent()) {
                ProposalResponseDto.FreelancerInfo freelancerInfo = convertToFreelancerInfo(userOpt.get());
                responseDto.setFreelancerInfo(freelancerInfo);
                log.info("Enhanced proposal response with freelancer info for: {}", savedProposal.getFreelancerId());
            } else {
                log.warn("No user found in auth service for freelancer ID: {}", savedProposal.getFreelancerId());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch freelancer info for proposal {}: {}", savedProposal.getId(), e.getMessage());
            // Continue without freelancer info rather than failing the entire operation
        }
        
        // Send notification asynchronously (fire and forget)
        try {
            sendProposalNotifications(savedProposal, job, responseDto);
            log.info("Notifications sent for proposal: {}", savedProposal.getId());
        } catch (Exception e) {
            log.error("Failed to send notifications for proposal {}: {}", savedProposal.getId(), e.getMessage());
            // Continue without failing the proposal creation
        }

        return responseDto;
    }
    
    private void sendProposalNotifications(Proposal proposal, Job job, ProposalResponseDto responseDto) {
        // TODO: Implement notification system - temporarily disabled
        log.info("Notification system temporarily disabled for proposal: {}", proposal.getId());
    }
    
    /**
     * Convert UserResponseDto from auth service to FreelancerInfo for proposal response
     */
    private ProposalResponseDto.FreelancerInfo convertToFreelancerInfo(UserResponseDto user) {
        ProposalResponseDto.FreelancerInfo freelancerInfo = new ProposalResponseDto.FreelancerInfo();
        freelancerInfo.setId(user.getId());
        freelancerInfo.setName(user.getName());
        freelancerInfo.setEmail(user.getEmail());
        freelancerInfo.setHandle(user.getHandle());
        freelancerInfo.setJoinedAt(user.getCreatedAt());
        // Set default values for fields not available in UserResponseDto
        freelancerInfo.setProfilePicture(null);
        freelancerInfo.setBio(null);
        freelancerInfo.setSkills(null);
        freelancerInfo.setHourlyRate(null);
        freelancerInfo.setLocation(user.getCountry());
        freelancerInfo.setCompletedProjects(0);
        freelancerInfo.setRating(0.0);
        freelancerInfo.setPortfolioUrl(null);
        return freelancerInfo;
    }
    }
    
    @Transactional
    public ProposalResponseDto createProposalWithMilestones(ProposalCreateWithMilestonesDto proposalCreateDto, Long freelancerId) {
        log.info("Creating new proposal with milestones for job: {} by freelancer: {}", proposalCreateDto.getJobId(), freelancerId);

        // Fetch job entity
        Job job = jobRepository.findById(proposalCreateDto.getJobId())
            .orElseThrow(() -> new RuntimeException("Job not found: " + proposalCreateDto.getJobId()));

        Proposal proposal = Proposal.builder()
            .job(job)
            .freelancerId(freelancerId)
            .cover(proposalCreateDto.getCover())
            .totalCents(proposalCreateDto.getTotalCents())
            .deliveryDays(proposalCreateDto.getDeliveryDays())
            .status(Proposal.ProposalStatus.SUBMITTED)
            .build();
        Proposal savedProposal = proposalRepository.save(proposal);

        // Save milestones if provided
        if (proposalCreateDto.getMilestones() != null && !proposalCreateDto.getMilestones().isEmpty()) {
            List<ProposalMilestone> milestones = proposalCreateDto.getMilestones().stream()
                .map(dto -> ProposalMilestone.builder()
                    .proposal(savedProposal)
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .amountCents(dto.getAmountCents())
                    .dueDate(dto.getDueDate())
                    .orderIndex(dto.getOrderIndex())
                    .build())
                .collect(java.util.stream.Collectors.toList());
            proposalMilestoneRepository.saveAll(milestones);
        }

        log.info("Proposal with milestones created successfully with ID: {}", savedProposal.getId());
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
                .map(proposal -> {
                    ProposalResponseDto dto = proposalMapper.toResponseDto(proposal);
                    // Enrich with freelancer info from auth service
                    enrichWithFreelancerInfo(dto, proposal.getFreelancerId());
                    return dto;
                })
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
                .map(proposal -> {
                    ProposalResponseDto dto = proposalMapper.toResponseDto(proposal);
                    // Enrich with freelancer info from auth service
                    enrichWithFreelancerInfo(dto, proposal.getFreelancerId());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    private void publishProposalSubmittedEvent(Proposal proposal, Job job) {
        try {
            // Get freelancer name - you might want to fetch this from user service
            String freelancerName = "Freelancer-" + proposal.getFreelancerId(); // Placeholder
            
            eventPublisherService.publishProposalSubmittedEvent(
                proposal.getId(),
                job.getId(),
                proposal.getFreelancerId(),
                job.getClientId(),
                job.getProjectName(),
                freelancerName
            );
        } catch (Exception e) {
            log.error("Failed to publish proposal submitted event for proposal: {}", proposal.getId(), e);
            // Don't throw exception here to avoid failing the main transaction
        }
    }
    
    /**
     * Enrich ProposalResponseDto with freelancer information from auth service
     */
    private void enrichWithFreelancerInfo(ProposalResponseDto dto, Long freelancerId) {
        try {
            Optional<UserResponseDto> userOpt = authServiceClient.getUserById(freelancerId);
            if (userOpt.isPresent()) {
                ProposalResponseDto.FreelancerInfo freelancerInfo = convertToFreelancerInfo(userOpt.get());
                dto.setFreelancerInfo(freelancerInfo);
                log.debug("Enhanced proposal {} with freelancer info for user: {}", dto.getId(), freelancerId);
            } else {
                log.warn("No user found in auth service for freelancer ID: {}", freelancerId);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch freelancer info for freelancer {}: {}", freelancerId, e.getMessage());
            // Continue without freelancer info rather than failing
        }
    }
}
