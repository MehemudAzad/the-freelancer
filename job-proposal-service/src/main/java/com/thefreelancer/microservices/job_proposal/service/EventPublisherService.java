package com.thefreelancer.microservices.job_proposal.service;

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

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ContractCreatedEvent {
        private Long contractId;
        private Long jobId;
        private Long proposalId;
        private Long clientId;
        private Long freelancerId;
        private String jobTitle;
        private String clientName;
        private String freelancerName;
        private LocalDateTime createdAt;
        private String contractTerms;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Long totalBudget;
        private String currency;
    }
}