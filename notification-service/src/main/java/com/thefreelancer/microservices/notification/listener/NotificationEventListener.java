package com.thefreelancer.microservices.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thefreelancer.microservices.notification.event.JobPostedEvent;
import com.thefreelancer.microservices.notification.event.ProposalAcceptedEvent;
import com.thefreelancer.microservices.notification.event.ProposalRejectedEvent;
import com.thefreelancer.microservices.notification.event.ProposalSubmittedEvent;
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
    
    @KafkaListener(topics = "proposal-submitted", groupId = "notification-service-group")
    public void handleProposalSubmittedEvent(
            @Payload String eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received proposal submitted event from topic: {}, partition: {}, offset: {}", 
                    topic, partition, offset);
            
            ProposalSubmittedEvent event = objectMapper.readValue(eventData, ProposalSubmittedEvent.class);
            
            notificationService.createProposalSubmittedNotification(
                event.getJobId(),
                event.getClientId(),
                event.getFreelancerId(),
                event.getJobTitle(),
                event.getFreelancerName(),
                event.getProposalCoverLetter()
            );
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed proposal submitted event: {}", event.getProposalId());
            
        } catch (Exception e) {
            log.error("Error processing proposal submitted event: {}", e.getMessage(), e);
            // Don't acknowledge - let Kafka retry
        }
    }
    
    @KafkaListener(topics = "proposal-accepted", groupId = "notification-service-group")
    public void handleProposalAcceptedEvent(
            @Payload String eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received proposal accepted event from topic: {}", topic);
            
            ProposalAcceptedEvent event = objectMapper.readValue(eventData, ProposalAcceptedEvent.class);
            
            notificationService.createProposalAcceptedNotification(
                event.getJobId(),
                event.getFreelancerId(),
                event.getClientId(),
                event.getJobTitle(),
                "Client", // TODO: Get actual client name
                event.getAcceptanceMessage()
            );
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed proposal accepted event: {}", event.getProposalId());
            
        } catch (Exception e) {
            log.error("Error processing proposal accepted event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "proposal-rejected", groupId = "notification-service-group")
    public void handleProposalRejectedEvent(
            @Payload String eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received proposal rejected event from topic: {}", topic);
            
            ProposalRejectedEvent event = objectMapper.readValue(eventData, ProposalRejectedEvent.class);
            
            notificationService.createProposalRejectedNotification(
                event.getJobId(),
                event.getFreelancerId(),
                event.getClientId(),
                event.getJobTitle(),
                "Client", // TODO: Get actual client name
                event.getRejectionMessage()
            );
            
            acknowledgment.acknowledge();
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
            
            // TODO: Implement job matching algorithm to find relevant freelancers
            // For now, just log the event
            log.info("Job posted: {} by {}", event.getJobTitle(), event.getClientName());
            
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
            
            // Parse contract event (would need to create ContractCreatedEvent class)
            // For now, just acknowledge
            acknowledgment.acknowledge();
            log.debug("Successfully processed contract created event");
            
        } catch (Exception e) {
            log.error("Error processing contract created event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "milestone-completed", groupId = "notification-service-group")
    public void handleMilestoneCompletedEvent(
            @Payload String eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received milestone completed event from topic: {}", topic);
            
            // Parse milestone event (would need to create MilestoneCompletedEvent class)
            // For now, just acknowledge
            acknowledgment.acknowledge();
            log.debug("Successfully processed milestone completed event");
            
        } catch (Exception e) {
            log.error("Error processing milestone completed event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "payment-released", groupId = "notification-service-group")
    public void handlePaymentReleasedEvent(
            @Payload String eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received payment released event from topic: {}", topic);
            
            // Parse payment event (would need to create PaymentReleasedEvent class)
            // For now, just acknowledge
            acknowledgment.acknowledge();
            log.debug("Successfully processed payment released event");
            
        } catch (Exception e) {
            log.error("Error processing payment released event: {}", e.getMessage(), e);
        }
    }
}
