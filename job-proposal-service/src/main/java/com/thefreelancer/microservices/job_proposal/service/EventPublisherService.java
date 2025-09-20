package com.thefreelancer.microservices.job_proposal.service;

import com.thefreelancer.microservices.job_proposal.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishProposalSubmittedEvent(Long proposalId, Long jobId, Long freelancerId, Long clientId, String projectName, String freelancerName) {
        try {
            ProposalSubmittedEvent event = ProposalSubmittedEvent.builder()
                    .proposalId(proposalId)
                    .jobId(jobId)
                    .freelancerId(freelancerId)
                    .clientId(clientId)
                    .projectName(projectName)
                    .freelancerName(freelancerName)
                    .build();
            
            kafkaTemplate.send("proposal-submitted", proposalId.toString(), event);
            log.info("Published ProposalSubmittedEvent for proposalId: {}", proposalId);
            
        } catch (Exception e) {
            log.error("Failed to publish ProposalSubmittedEvent for proposalId: {}", proposalId, e);
        }
    }

    public void publishProposalAcceptedEvent(Long proposalId, Long jobId, Long freelancerId, Long clientId, String projectName, String freelancerName) {
        try {
            ProposalAcceptedEvent event = ProposalAcceptedEvent.builder()
                    .proposalId(proposalId)
                    .jobId(jobId)
                    .freelancerId(freelancerId)
                    .clientId(clientId)
                    .projectName(projectName)
                    .freelancerName(freelancerName)
                    .build();

            kafkaTemplate.send("proposal-accepted", proposalId.toString(), event);
            log.info("Published ProposalAcceptedEvent for proposalId: {}", proposalId);
            
        } catch (Exception e) {
            log.error("Failed to publish ProposalAcceptedEvent for proposalId: {}", proposalId, e);
        }
    }

    public void publishContractCreatedEvent(Long contractId, Long jobId, Long proposalId, Long clientId, Long freelancerId, 
                                           String jobTitle, String clientName, String freelancerName, LocalDateTime startDate, 
                                           LocalDateTime endDate, Long totalBudget, String currency, String contractTerms) {
        try {
            ContractCreatedEvent event = ContractCreatedEvent.builder()
                    .contractId(contractId)
                    .jobId(jobId)
                    .proposalId(proposalId)
                    .clientId(clientId)
                    .freelancerId(freelancerId)
                    .jobTitle(jobTitle)
                    .clientName(clientName)
                    .freelancerName(freelancerName)
                    .createdAt(LocalDateTime.now())
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalBudget(totalBudget)
                    .currency(currency)
                    .contractTerms(contractTerms)
                    .build();

            kafkaTemplate.send("contract-created", contractId.toString(), event);
            log.info("Published ContractCreatedEvent for contractId: {}", contractId);
            
        } catch (Exception e) {
            log.error("Failed to publish ContractCreatedEvent for contractId: {}", contractId, e);
        }
    }

    public void publishJobSubmittedEvent(Long contractId, Long jobId, Long clientId, Long freelancerId, String jobTitle, String submissionDescription) {
        try {
            JobSubmittedEvent event = JobSubmittedEvent.builder()
                    .contractId(contractId)
                    .jobId(jobId)
                    .clientId(clientId)
                    .freelancerId(freelancerId)
                    .jobTitle(jobTitle)
                    .submissionDescription(submissionDescription)
                    .submittedAt(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("job-submitted", contractId.toString(), event);
            log.info("Published JobSubmittedEvent for contractId: {}", contractId);
            
        } catch (Exception e) {
            log.error("Failed to publish JobSubmittedEvent for contractId: {}", contractId, e);
        }
    }

    public void publishJobAcceptedEvent(Long contractId, Long jobId, Long clientId, Long freelancerId, String jobTitle) {
        try {
            JobAcceptedEvent event = JobAcceptedEvent.builder()
                    .contractId(contractId)
                    .jobId(jobId)
                    .clientId(clientId)
                    .freelancerId(freelancerId)
                    .jobTitle(jobTitle)
                    .acceptedAt(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("job-accepted", contractId.toString(), event);
            log.info("Published JobAcceptedEvent for contractId: {}", contractId);
            
        } catch (Exception e) {
            log.error("Failed to publish JobAcceptedEvent for contractId: {}", contractId, e);
        }
    }

    public void publishJobRejectedEvent(Long contractId, Long jobId, Long clientId, Long freelancerId, String jobTitle, String rejectionReason, String rejectionFeedback) {
        try {
            JobRejectedEvent event = JobRejectedEvent.builder()
                    .contractId(contractId)
                    .jobId(jobId)
                    .clientId(clientId)
                    .freelancerId(freelancerId)
                    .jobTitle(jobTitle)
                    .rejectionReason(rejectionReason)
                    .rejectionFeedback(rejectionFeedback)
                    .rejectedAt(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("job-rejected", contractId.toString(), event);
            log.info("Published JobRejectedEvent for contractId: {}", contractId);
            
        } catch (Exception e) {
            log.error("Failed to publish JobRejectedEvent for contractId: {}", contractId, e);
        }
    }

    public void publishReviewReminderEvent(Long contractId, Long jobId, Long clientId, Long freelancerId, String jobTitle, String freelancerName) {
        try {
            ReviewReminderEvent event = ReviewReminderEvent.builder()
                    .contractId(contractId.toString())
                    .jobId(jobId.toString())
                    .clientId(clientId.toString())
                    .freelancerId(freelancerId.toString())
                    .jobTitle(jobTitle)
                    .freelancerName(freelancerName)
                    .timestamp(System.currentTimeMillis())
                    .completedAt(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("review-reminder", contractId.toString(), event);
            log.info("Published ReviewReminderEvent for contractId: {}", contractId);
            
        } catch (Exception e) {
            log.error("Failed to publish ReviewReminderEvent for contractId: {}", contractId, e);
        }
    }

    public void publishInviteSentEvent(Long inviteId, Long clientId, Long freelancerId, Long jobId, 
                                      String jobTitle, String freelancerName, String clientName) {
        try {
            InviteSentEvent event = InviteSentEvent.builder()
                    .inviteId(inviteId.toString())
                    .clientId(clientId.toString())
                    .freelancerId(freelancerId.toString())
                    .jobId(jobId.toString())
                    .jobTitle(jobTitle)
                    .freelancerName(freelancerName)
                    .clientName(clientName)
                    .timestamp(System.currentTimeMillis())
                    .build();

            kafkaTemplate.send("invite-sent", inviteId.toString(), event);
            log.info("Published InviteSentEvent for inviteId: {}", inviteId);
            
        } catch (Exception e) {
            log.error("Failed to publish InviteSentEvent for inviteId: {}", inviteId, e);
        }
    }

    public void publishInviteReceivedEvent(Long inviteId, Long clientId, Long freelancerId, Long jobId, 
                                          String jobTitle, String freelancerName, String clientName) {
        try {
            InviteReceivedEvent event = InviteReceivedEvent.builder()
                    .inviteId(inviteId.toString())
                    .clientId(clientId.toString())
                    .freelancerId(freelancerId.toString())
                    .jobId(jobId.toString())
                    .jobTitle(jobTitle)
                    .freelancerName(freelancerName)
                    .clientName(clientName)
                    .timestamp(System.currentTimeMillis())
                    .build();

            kafkaTemplate.send("invite-received", inviteId.toString(), event);
            log.info("Published InviteReceivedEvent for inviteId: {}", inviteId);
            
        } catch (Exception e) {
            log.error("Failed to publish InviteReceivedEvent for inviteId: {}", inviteId, e);
        }
    }
}