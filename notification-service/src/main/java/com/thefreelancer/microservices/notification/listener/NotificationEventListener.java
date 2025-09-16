package com.thefreelancer.microservices.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thefreelancer.microservices.notification.event.*;
import com.thefreelancer.microservices.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {
    
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "proposal-submitted", groupId = "notification-service-group", 
                   containerFactory = "proposalSubmittedKafkaListenerContainerFactory")
    public void handleProposalSubmittedEvent(ProposalSubmittedEvent event) {
        
        try {
            log.info("Received proposal submitted event for proposal: {}", event.getProposalId());
            
            notificationService.createProposalSubmittedNotification(
                event.getJobId(),
                event.getClientId(),
                event.getFreelancerId(),
                event.getProjectName(),
                event.getFreelancerName(),
                null // proposalCoverLetter - not available in simplified event
            );
            
            log.debug("Successfully processed proposal submitted event: {}", event.getProposalId());
        } catch (Exception e) {
            log.error("Error processing proposal submitted event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "proposal-accepted", groupId = "notification-service-group", 
                   containerFactory = "proposalAcceptedKafkaListenerContainerFactory")
    public void handleProposalAcceptedEvent(ProposalAcceptedEvent event) {
        
        try {
            log.info("Received proposal accepted event for proposal: {}", event.getProposalId());
            
            notificationService.createProposalAcceptedNotification(
                event.getJobId(),
                event.getFreelancerId(),
                event.getClientId(),
                event.getProjectName(),
                "Client", // TODO: Get actual client name
                "Your proposal has been accepted!" // Default message since acceptanceMessage might not exist
            );
            
            log.debug("Successfully processed proposal accepted event: {}", event.getProposalId());
            
        } catch (Exception e) {
            log.error("Error processing proposal accepted event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "proposal-rejected", groupId = "notification-service-group", 
                   containerFactory = "proposalRejectedKafkaListenerContainerFactory")
    public void handleProposalRejectedEvent(ProposalRejectedEvent event) {
        
        try {
            log.info("Received proposal rejected event for proposal: {}", event.getProposalId());
            
            notificationService.createProposalRejectedNotification(
                event.getJobId(),
                event.getFreelancerId(),
                null, // clientId - not available in simplified event
                event.getProjectName(),
                "Client", // TODO: Get actual client name
                event.getFeedback() // Use feedback field
            );
            
            log.debug("Successfully processed proposal rejected event: {}", event.getProposalId());
            
        } catch (Exception e) {
            log.error("Error processing proposal rejected event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "job-posted", groupId = "notification-service-group")
    public void handleJobPostedEvent(
            @Payload String eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received job posted event from topic: {}", topic);
            
            JobPostedEvent event = objectMapper.readValue(eventData, JobPostedEvent.class);
            
                // Create budget range string
                String budgetRange = null;
                if (event.getMinBudget() != null && event.getMaxBudget() != null) {
                    budgetRange = String.format("%d-%d %s", 
                        event.getMinBudget(), event.getMaxBudget(), 
                        event.getCurrency() != null ? event.getCurrency() : "USD");
                } else if (event.getMinBudget() != null) {
                    budgetRange = String.format("From %d %s", 
                        event.getMinBudget(), 
                        event.getCurrency() != null ? event.getCurrency() : "USD");
                }
            
                // Create the job posted notification
                notificationService.createJobPostedNotification(
                    event.getJobId(),
                    event.getJobTitle(),
                    event.getClientName(),
                    event.getJobDescription(),
                    event.getRequiredSkills(),
                    budgetRange,
                    event.getJobCategory()
                );
            
                log.info("Created job posted notification for job: {} by {}", event.getJobTitle(), event.getClientName());
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed job posted event: {}", event.getJobId());
            
        } catch (Exception e) {
            log.error("Error processing job posted event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "contract-created", groupId = "notification-service-group")
    public void handleContractCreatedEvent(
            @Payload String eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received contract created event from topic: {}", topic);
            
            ContractCreatedEvent event = objectMapper.readValue(eventData, ContractCreatedEvent.class);
            
            notificationService.createContractCreatedNotification(
                event.getContractId(),
                event.getJobId(),
                event.getClientId(),
                event.getFreelancerId(),
                event.getJobTitle()
            );
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed contract created event: {}", event.getContractId());
            
        } catch (Exception e) {
            log.error("Error processing contract created event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "job-submitted", groupId = "notification-service-group")
    public void handleJobSubmittedEvent(
            @Payload String eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received job submitted event from topic: {}", topic);
            
            JobSubmittedEvent event = objectMapper.readValue(eventData, JobSubmittedEvent.class);
            
            notificationService.createJobSubmittedNotification(
                event.getJobId(),
                null, // contractId - not available in this event, will be null
                event.getClientId(),
                event.getJobTitle(),
                event.getFreelancerName()
            );
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed job submitted event: {}", event.getJobId());
            
        } catch (Exception e) {
            log.error("Error processing job submitted event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "payment-released", groupId = "notification-service-group")
    public void handlePaymentReleasedEvent(
            @Payload String eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received payment released event from topic: {}", topic);
            
            PaymentReleasedEvent event = objectMapper.readValue(eventData, PaymentReleasedEvent.class);
            
            double amount = event.getAmountCents() / 100.0; // Convert cents to dollars
            notificationService.createPaymentReleasedNotification(
                event.getJobId(),
                event.getFreelancerId(),
                amount,
                event.getCurrency(),
                event.getJobTitle()
            );
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed payment released event: {}", event.getPaymentId());
            
        } catch (Exception e) {
            log.error("Error processing payment released event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "job-accepted", groupId = "notification-service-group")
    public void handleJobAcceptedEvent(
            @Payload String eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received job accepted event from topic: {}", topic);
            
            JobAcceptedEvent event = objectMapper.readValue(eventData, JobAcceptedEvent.class);
            
            notificationService.createJobAcceptedNotification(
                event.getJobId(),
                event.getContractId(),
                event.getFreelancerId(),
                event.getJobTitle(),
                event.getClientName()
            );
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed job accepted event: {}", event.getJobId());
            
        } catch (Exception e) {
            log.error("Error processing job accepted event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "job-rejected", groupId = "notification-service-group")
    public void handleJobRejectedEvent(
            @Payload String eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received job rejected event from topic: {}", topic);
            
            JobRejectedEvent event = objectMapper.readValue(eventData, JobRejectedEvent.class);
            
            notificationService.createJobRejectedNotification(
                event.getJobId(),
                event.getContractId(),
                event.getFreelancerId(),
                event.getJobTitle(),
                event.getClientName(),
                event.getFeedback()
            );
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed job rejected event: {}", event.getJobId());
            
        } catch (Exception e) {
            log.error("Error processing job rejected event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "message-sent", groupId = "notification-service-group")
    public void handleMessageSentEvent(
            @Payload String eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received message sent event from topic: {}", topic);
            
            MessageSentEvent event = objectMapper.readValue(eventData, MessageSentEvent.class);
            
            // Only create notification if recipient is different from sender
            if (!event.getSenderId().equals(event.getRecipientId())) {
                String messagePreview = event.getMessageContent().length() > 50 
                    ? event.getMessageContent().substring(0, 50) + "..."
                    : event.getMessageContent();
                    
                notificationService.createMessageReceivedNotification(
                    event.getRecipientId(),
                    event.getSenderId(),
                    event.getSenderName(),
                    event.getRoomId(),
                    messagePreview
                );
            }
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed message sent event: {}", event.getMessageId());
            
        } catch (Exception e) {
            log.error("Error processing message sent event: {}", e.getMessage(), e);
        }
    }
}
