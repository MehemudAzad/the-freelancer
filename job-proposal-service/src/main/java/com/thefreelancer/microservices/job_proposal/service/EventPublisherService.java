package com.thefreelancer.microservices.job_proposal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

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
            
            String eventString = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("proposal-submitted", proposalId.toString(), eventString)
                    .thenAccept(result -> log.info("Successfully published proposal-submitted event for proposal: {}", proposalId))
                    .exceptionally(ex -> {
                        log.error("Failed to publish proposal-submitted event for proposal: {}", proposalId, ex);
                        return null;
                    });
            
        } catch (Exception e) {
            log.error("Error creating proposal-submitted event for proposal: {}", proposalId, e);
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

            String eventString = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("proposal-accepted", proposalId.toString(), eventString)
                    .thenAccept(result -> log.info("Successfully published proposal-accepted event for proposal: {}", proposalId))
                    .exceptionally(ex -> {
                        log.error("Failed to publish proposal-accepted event for proposal: {}", proposalId, ex);
                        return null;
                    });
            
        } catch (Exception e) {
            log.error("Error creating proposal-accepted event for proposal: {}", proposalId, e);
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

            String eventString = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("proposal-rejected", proposalId.toString(), eventString)
                    .thenAccept(result -> log.info("Successfully published proposal-rejected event for proposal: {}", proposalId))
                    .exceptionally(ex -> {
                        log.error("Failed to publish proposal-rejected event for proposal: {}", proposalId, ex);
                        return null;
                    });
            
        } catch (Exception e) {
            log.error("Error creating proposal-rejected event for proposal: {}", proposalId, e);
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