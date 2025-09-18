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
    
    // ========== INVITE NOTIFICATIONS (1-3) ==========
    
    @KafkaListener(topics = "invite-sent", groupId = "notification-service-group")
    public void handleInviteSentEvent(
            @Payload String eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received invite sent event from topic: {}", topic);
            
            InviteSentEvent event = objectMapper.readValue(eventData, InviteSentEvent.class);
            
            notificationService.createInviteSentNotification(
                event.getClientId(),
                event.getFreelancerId(),
                event.getJobId(),
                event.getJobTitle(),
                event.getFreelancerName()
            );
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed invite sent event: {}", event.getInviteId());
            
        } catch (Exception e) {
            log.error("Error processing invite sent event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "invite-accepted", groupId = "notification-service-group")
    public void handleInviteAcceptedEvent(
            @Payload String eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received invite accepted event from topic: {}", topic);
            
            InviteAcceptedEvent event = objectMapper.readValue(eventData, InviteAcceptedEvent.class);
            
            notificationService.createInviteAcceptedNotification(
                event.getClientId(),
                event.getFreelancerId(),
                event.getJobId(),
                event.getJobTitle(),
                event.getFreelancerName()
            );
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed invite accepted event: {}", event.getInviteId());
            
        } catch (Exception e) {
            log.error("Error processing invite accepted event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "invite-received", groupId = "notification-service-group")
    public void handleInviteReceivedEvent(
            @Payload String eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received invite received event from topic: {}", topic);
            
            InviteReceivedEvent event = objectMapper.readValue(eventData, InviteReceivedEvent.class);
            
            notificationService.createInviteReceivedNotification(
                event.getFreelancerId(),
                event.getClientId(),
                event.getJobId(),
                event.getJobTitle(),
                event.getClientName()
            );
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed invite received event: {}", event.getInviteId());
            
        } catch (Exception e) {
            log.error("Error processing invite received event: {}", e.getMessage(), e);
        }
    }
    
    // ========== PROPOSAL NOTIFICATIONS (4-6) ==========
    
    @KafkaListener(topics = "proposal-submitted", groupId = "notification-service-group", 
                   containerFactory = "proposalSubmittedKafkaListenerContainerFactory")
    public void handleProposalSubmittedEvent(ProposalSubmittedEvent event) {
        
        try {
            log.info("Received proposal submitted event for proposal: {}", event.getProposalId());
            
            // Notification #5: A freelancer has submitted a proposal on your job -> client (inbox only)
            notificationService.createProposalSubmittedNotification(
                event.getClientId(),
                event.getFreelancerId(),
                event.getJobId(),
                event.getProjectName(),
                event.getFreelancerName()
            );
            
            log.debug("Successfully processed proposal submitted event: {}", event.getProposalId());
        } catch (Exception e) {
            log.error("Error processing proposal submitted event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "proposal-accepted", groupId = "notification-service-group", 
                   containerFactory = "proposalAcceptedKafkaListenerContainerFactory")
    public void handleProposalAcceptedEvent(ProposalAcceptedEvent event, Acknowledgment acknowledgment) {
        
        try {
            log.info("Received proposal accepted event for proposal: {}", event.getProposalId());
            
            // Notification #4: Proposal accepted -> freelancer (email + inbox)
            notificationService.createProposalAcceptedNotification(
                event.getFreelancerId(),
                event.getClientId(),
                event.getJobId(),
                event.getProjectName(),
                "Client" // TODO: Get actual client name from event
            );
            
            log.debug("Successfully processed proposal accepted event: {}", event.getProposalId());
            acknowledgment.acknowledge(); // Acknowledge successful processing
            
        } catch (Exception e) {
            log.error("Error processing proposal accepted event: {}", e.getMessage(), e);
            // Don't acknowledge on error - message will be retried
        }
    }
    
    @KafkaListener(topics = "escrow-funded", groupId = "notification-service-group")
    public void handleEscrowFundedEvent(
            @Payload String eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received escrow funded event from topic: {}", topic);
            
            EscrowFundedEvent event = objectMapper.readValue(eventData, EscrowFundedEvent.class);
            
            // Notification #6: You have accepted the proposal and payment escrow has been made -> client (email + inbox)
            notificationService.createEscrowFundedNotification(
                event.getClientId(),
                event.getFreelancerId(),
                event.getJobId(),
                event.getJobTitle(),
                event.getFreelancerName(),
                event.getAmountCents(),
                event.getCurrency()
            );
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed escrow funded event: {}", event.getEscrowId());
            
        } catch (Exception e) {
            log.error("Error processing escrow funded event: {}", e.getMessage(), e);
        }
    }
    
    // ========== JOB SUBMISSION NOTIFICATIONS (7-10) ==========
    
    @KafkaListener(topics = "job-submitted", groupId = "notification-service-group")
    public void handleJobSubmittedEvent(
            @Payload String eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received job submitted event from topic: {}", topic);
            
            JobSubmittedEvent event = objectMapper.readValue(eventData, JobSubmittedEvent.class);
            
            // Notification #7: Freelancer for jobId has submitted please review -> client (email + inbox)
            notificationService.createJobSubmittedNotification(
                event.getClientId(),
                event.getFreelancerId(),
                event.getJobId(),
                event.getJobTitle(),
                event.getFreelancerName()
            );
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed job submitted event: {}", event.getJobId());
            
        } catch (Exception e) {
            log.error("Error processing job submitted event: {}", e.getMessage(), e);
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
            
            // Notification #9: Client has accepted your submission. Payment has been transferred -> freelancer (email + inbox)
            notificationService.createJobAcceptedNotification(
                event.getFreelancerId(),
                event.getClientId(),
                event.getJobId(),
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
            
            // Notification #8: Client reject/revision your job submission -> freelancer (inbox only)
            notificationService.createJobRejectedNotification(
                event.getFreelancerId(),
                event.getClientId(),
                event.getJobId(),
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
    
    @KafkaListener(topics = "review-reminder", groupId = "notification-service-group")
    public void handleReviewReminderEvent(
            @Payload String eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received review reminder event from topic: {}", topic);
            
            ReviewReminderEvent event = objectMapper.readValue(eventData, ReviewReminderEvent.class);
            
            // Notification #10: After job submission you have accepted, go and review the freelancer -> client (email + inbox)
            notificationService.createReviewReminderNotification(
                event.getClientId(),
                event.getFreelancerId(),
                event.getJobId(),
                event.getJobTitle(),
                event.getFreelancerName()
            );
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed review reminder event: {}", event.getJobId());
            
        } catch (Exception e) {
            log.error("Error processing review reminder event: {}", e.getMessage(), e);
        }
    }
    
}
