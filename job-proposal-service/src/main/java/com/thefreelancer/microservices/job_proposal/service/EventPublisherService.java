package com.thefreelancer.microservices.job_proposal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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

    public void publishProposalRejectedEvent(Long proposalId, Long jobId, Long freelancerId, String projectName, String freelancerName, String feedback) {
        try {
            ProposalRejectedEvent event = ProposalRejectedEvent.builder()
                    .proposalId(proposalId)
                    .jobId(jobId)
                    .freelancerId(freelancerId)
                    .projectName(projectName)
                    .freelancerName(freelancerName)
                    .feedback(feedback)
                    .build();

            kafkaTemplate.send("proposal-rejected", proposalId.toString(), event);
            log.info("Published ProposalRejectedEvent for proposalId: {}", proposalId);
            
        } catch (Exception e) {
            log.error("Failed to publish ProposalRejectedEvent for proposalId: {}", proposalId, e);
        }
    }

    // Event DTOs
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProposalSubmittedEvent {
        private Long proposalId;
        private Long jobId;
        private Long freelancerId;
        private Long clientId;
        private String projectName;
        private String freelancerName;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProposalAcceptedEvent {
        private Long proposalId;
        private Long jobId;
        private Long freelancerId;
        private Long clientId;
        private String projectName;
        private String freelancerName;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProposalRejectedEvent {
        private Long proposalId;
        private Long jobId;
        private Long freelancerId;
        private String projectName;
        private String freelancerName;
        private String feedback;
    }
}