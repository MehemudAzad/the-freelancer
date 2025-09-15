package com.thefreelancer.microservices.notification.service;

import com.thefreelancer.microservices.notification.dto.NotificationResponseDto;
import com.thefreelancer.microservices.notification.model.Notification;
import com.thefreelancer.microservices.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;
    
    // Retrieve notifications
    public Page<NotificationResponseDto> getNotificationsByRecipient(Long recipientId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable);
        return notifications.map(this::mapToResponseDto);
    }
    
    public Page<NotificationResponseDto> getUnreadNotifications(Long recipientId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(recipientId, pageable);
        return notifications.map(this::mapToResponseDto);
    }
    
    public long getUnreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }
    
    public Optional<Notification> getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId);
    }
    
    // Mark notifications as read
    public Notification markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            return notificationRepository.save(notification);
        }
        return null;
    }
    
    public int markAllAsRead(Long recipientId) {
        return notificationRepository.markAllAsReadForUser(recipientId, LocalDateTime.now());
    }
    
    // Create different types of notifications
    public Notification createProposalSubmittedNotification(Long jobId, Long clientId, Long freelancerId, 
                                                           String jobTitle, String freelancerName, 
                                                           String proposalCover) {
        Notification notification = Notification.builder()
                .recipientId(clientId)
                .senderId(freelancerId)
                .type(Notification.NotificationType.PROPOSAL_SUBMITTED)
                .title("New Proposal Received")
                .message(String.format("You received a new proposal from %s for your job '%s'", 
                        freelancerName, jobTitle))
                .jobId(jobId)
                .status(Notification.NotificationStatus.PENDING)
                .metadata(String.format("{\"proposalCover\":\"%s\"}", proposalCover != null ? proposalCover : ""))
                .build();
        
        return createAndSendNotification(notification);
    }
    
    public Notification createProposalAcceptedNotification(Long jobId, Long freelancerId, Long clientId, 
                                                          String jobTitle, String clientName, 
                                                          String acceptanceMessage) {
        Notification notification = Notification.builder()
                .recipientId(freelancerId)
                .senderId(clientId)
                .type(Notification.NotificationType.PROPOSAL_ACCEPTED)
                .title("Proposal Accepted!")
                .message(String.format("Congratulations! %s has accepted your proposal for '%s'", 
                        clientName, jobTitle))
                .jobId(jobId)
                .status(Notification.NotificationStatus.PENDING)
                .metadata(String.format("{\"acceptanceMessage\":\"%s\"}", acceptanceMessage != null ? acceptanceMessage : ""))
                .build();
        
        return createAndSendNotification(notification);
    }
    
    public Notification createProposalRejectedNotification(Long jobId, Long freelancerId, Long clientId, 
                                                          String jobTitle, String clientName, 
                                                          String rejectionMessage) {
        Notification notification = Notification.builder()
                .recipientId(freelancerId)
                .senderId(clientId)
                .type(Notification.NotificationType.PROPOSAL_REJECTED)
                .title("Proposal Update")
                .message(String.format("Your proposal for '%s' was not selected this time", jobTitle))
                .jobId(jobId)
                .status(Notification.NotificationStatus.PENDING)
                .metadata(String.format("{\"rejectionMessage\":\"%s\"}", rejectionMessage != null ? rejectionMessage : ""))
                .build();
        
        return createAndSendNotification(notification);
    }
    
    public Notification createContractCreatedNotification(Long contractId, Long jobId, Long clientId, 
                                                         Long freelancerId, String jobTitle) {
        // Notify both client and freelancer
        Notification clientNotification = Notification.builder()
                .recipientId(clientId)
                .senderId(freelancerId)
                .type(Notification.NotificationType.CONTRACT_CREATED)
                .title("Contract Created")
                .message(String.format("Contract has been created for job '%s'. Project can now begin!", jobTitle))
                .jobId(jobId)
                .contractId(contractId)
                .status(Notification.NotificationStatus.PENDING)
                .build();
        
        Notification freelancerNotification = Notification.builder()
                .recipientId(freelancerId)
                .senderId(clientId)
                .type(Notification.NotificationType.CONTRACT_CREATED)
                .title("Contract Created")
                .message(String.format("Contract has been created for job '%s'. You can start working!", jobTitle))
                .jobId(jobId)
                .contractId(contractId)
                .status(Notification.NotificationStatus.PENDING)
                .build();
        
        // Send both notifications
        createAndSendNotification(clientNotification);
        return createAndSendNotification(freelancerNotification);
    }
    
    public Notification createJobSubmittedNotification(Long jobId, Long contractId, Long clientId, 
                                                      String jobTitle, String freelancerName) {
        Notification notification = Notification.builder()
                .recipientId(clientId)
                .senderId(null) // System notification
                .type(Notification.NotificationType.JOB_SUBMITTED)
                .title("Job Submitted")
                .message(String.format("%s has submitted the job '%s' for your review", 
                        freelancerName, jobTitle))
                .jobId(jobId)
                .contractId(contractId)
                .status(Notification.NotificationStatus.PENDING)
                .build();
        
        return createAndSendNotification(notification);
    }
    
    public Notification createJobAcceptedNotification(Long jobId, Long contractId, Long freelancerId,
                                                     String jobTitle, String clientName) {
        Notification notification = Notification.builder()
                .recipientId(freelancerId)
                .senderId(null)
                .type(Notification.NotificationType.JOB_ACCEPTED)
                .title("Job Accepted")
                .message(String.format("%s has accepted your job submission for '%s'. Payment will be processed.", 
                        clientName, jobTitle))
                .jobId(jobId)
                .contractId(contractId)
                .status(Notification.NotificationStatus.PENDING)
                .build();
        
        return createAndSendNotification(notification);
    }
    
    public Notification createJobRejectedNotification(Long jobId, Long contractId, Long freelancerId,
                                                     String jobTitle, String clientName, String feedback) {
        Notification notification = Notification.builder()
                .recipientId(freelancerId)
                .senderId(null)
                .type(Notification.NotificationType.JOB_REJECTED)
                .title("Job Needs Revision")
                .message(String.format("%s has requested revisions for job '%s'", 
                        clientName, jobTitle))
                .jobId(jobId)
                .contractId(contractId)
                .metadata(String.format("{\"feedback\": \"%s\"}", feedback != null ? feedback.replace("\"", "\\\"") : ""))
                .status(Notification.NotificationStatus.PENDING)
                .build();
        
        return createAndSendNotification(notification);
    }
    
    public Notification createPaymentReleasedNotification(Long jobId, Long freelancerId, 
                                                         Double amount, String currency, String jobTitle) {
        Notification notification = Notification.builder()
                .recipientId(freelancerId)
                .senderId(null) // System notification
                .type(Notification.NotificationType.PAYMENT_RECEIVED)
                .title("Payment Received")
                .message(String.format("You have received a payment of %.2f %s for completed job '%s'", 
                        amount, currency, jobTitle))
                .jobId(jobId)
                .status(Notification.NotificationStatus.PENDING)
                .metadata(String.format("{\"amount\":%.2f,\"currency\":\"%s\"}", amount, currency))
                .build();
        
        return createAndSendNotification(notification);
    }
    
    public Notification createJobPostedNotification(Long jobId, String jobTitle, String clientName) {
        // This could be used for notifying interested freelancers
        // For now, we'll create a system announcement type
        Notification notification = Notification.builder()
                .recipientId(null) // Will be sent to relevant freelancers
                .senderId(null)
                .type(Notification.NotificationType.JOB_POSTED)
                .title("New Job Posted")
                .message(String.format("New job '%s' posted by %s", jobTitle, clientName))
                .jobId(jobId)
                .status(Notification.NotificationStatus.PENDING)
                .build();
        
        return notificationRepository.save(notification);
    }
    
    public Notification createMessageReceivedNotification(Long recipientId, Long senderId, String senderName,
                                                         Long roomId, String messagePreview) {
        Notification notification = Notification.builder()
                .recipientId(recipientId)
                .senderId(senderId)
                .type(Notification.NotificationType.MESSAGE_RECEIVED)
                .title("New Message")
                .message(String.format("New message from %s: %s", senderName, messagePreview))
                .status(Notification.NotificationStatus.PENDING)
                .metadata(String.format("{\"roomId\":%d}", roomId))
                .build();
        
        return createAndSendNotification(notification);
    }
    
    // Private helper methods
    private Notification createAndSendNotification(Notification notification) {
        // Save notification to database
        Notification savedNotification = notificationRepository.save(notification);
        
        try {
            // Send real-time notification via WebSocket
            sendRealTimeNotification(savedNotification);
            
            // Send email notification
            emailService.sendNotificationEmail(savedNotification);
            
            // Update status to sent
            savedNotification.setStatus(Notification.NotificationStatus.SENT);
            savedNotification.setSentAt(LocalDateTime.now());
            savedNotification = notificationRepository.save(savedNotification);
            
        } catch (Exception e) {
            log.error("Failed to send notification {}: {}", savedNotification.getId(), e.getMessage());
            savedNotification.setStatus(Notification.NotificationStatus.FAILED);
            notificationRepository.save(savedNotification);
        }
        
        return savedNotification;
    }
    
    private void sendRealTimeNotification(Notification notification) {
        try {
            if (notification.getRecipientId() != null) {
                NotificationResponseDto dto = mapToResponseDto(notification);
                messagingTemplate.convertAndSendToUser(
                    notification.getRecipientId().toString(),
                    "/queue/notifications",
                    dto
                );
                log.debug("Real-time notification sent to user {}", notification.getRecipientId());
            }
        } catch (Exception e) {
            log.error("Failed to send real-time notification: {}", e.getMessage());
            throw e;
        }
    }
    
    private NotificationResponseDto mapToResponseDto(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipientId())
                .senderId(notification.getSenderId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .status(notification.getStatus().name())
                .jobId(notification.getJobId())
                .proposalId(notification.getProposalId())
                .contractId(notification.getContractId())
                .referenceUrl(notification.getReferenceUrl())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
    
    // Batch operations for cleanup and maintenance
    public void markNotificationsAsDelivered(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setStatus(Notification.NotificationStatus.DELIVERED);
            notificationRepository.save(notification);
        }
    }
    
    public void retryFailedNotifications() {
        var failedNotifications = notificationRepository.findByStatusOrderByCreatedAtAsc(
            Notification.NotificationStatus.FAILED);
        
        for (Notification notification : failedNotifications) {
            try {
                sendRealTimeNotification(notification);
                emailService.sendNotificationEmail(notification);
                
                notification.setStatus(Notification.NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);
                
                log.info("Successfully retried failed notification {}", notification.getId());
            } catch (Exception e) {
                log.error("Retry failed for notification {}: {}", notification.getId(), e.getMessage());
            }
        }
    }
    
    public void cleanupOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int deletedCount = notificationRepository.deleteNotificationsOlderThan(cutoffDate);
        log.info("Cleaned up {} old notifications older than {} days", deletedCount, daysOld);
    }
}