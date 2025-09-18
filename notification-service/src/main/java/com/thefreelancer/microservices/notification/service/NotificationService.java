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
    
    // Idempotency check to prevent duplicate notifications
    private boolean isDuplicateNotification(Long recipientId, Notification.NotificationType type, 
                                          Long jobId, Long proposalId, Long contractId) {
        return notificationRepository.existsByRecipientIdAndTypeAndJobIdAndProposalIdAndContractId(
                recipientId, type, jobId, proposalId, contractId);
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
    
        // Create different types of notifications based on the 10 specific requirements

    // INVITE NOTIFICATIONS (3 types)
    
    // #1: Invite sent -> client notification (inbox only)
    public Notification createInviteSentNotification(Long clientId, Long freelancerId, Long jobId, 
                                                    String jobTitle, String freelancerName) {
        if (isDuplicateNotification(clientId, Notification.NotificationType.INVITE_SENT, jobId, null, null)) {
            log.info("Duplicate INVITE_SENT notification detected for recipient: {}, jobId: {} - skipping", clientId, jobId);
            return notificationRepository.findByRecipientIdAndTypeAndJobIdOrderByCreatedAtDesc(clientId, 
                    Notification.NotificationType.INVITE_SENT, jobId).stream().findFirst().orElse(null);
        }
        
        Notification notification = Notification.builder()
                .recipientId(clientId)
                .senderId(freelancerId)
                .type(Notification.NotificationType.INVITE_SENT)
                .title("Invite Sent")
                .message(String.format("You have sent an invite to %s for your job '%s'", 
                        freelancerName, jobTitle))
                .jobId(jobId)
                .status(Notification.NotificationStatus.PENDING)
                .metadata(String.format("{\"freelancerName\":\"%s\"}", freelancerName))
                .build();
        
        return createAndSendNotification(notification);
    }
    
    // #2: Invite accepted -> client notification (email)
    public Notification createInviteAcceptedNotification(Long clientId, Long freelancerId, Long jobId, 
                                                        String jobTitle, String freelancerName) {
        if (isDuplicateNotification(clientId, Notification.NotificationType.INVITE_ACCEPTED, jobId, null, null)) {
            log.info("Duplicate INVITE_ACCEPTED notification detected for recipient: {}, jobId: {} - skipping", clientId, jobId);
            return notificationRepository.findByRecipientIdAndTypeAndJobIdOrderByCreatedAtDesc(clientId, 
                    Notification.NotificationType.INVITE_ACCEPTED, jobId).stream().findFirst().orElse(null);
        }
        
        Notification notification = Notification.builder()
                .recipientId(clientId)
                .senderId(freelancerId)
                .type(Notification.NotificationType.INVITE_ACCEPTED)
                .title("Invite Accepted!")
                .message(String.format("%s has accepted your invite for the job '%s'", 
                        freelancerName, jobTitle))
                .jobId(jobId)
                .status(Notification.NotificationStatus.PENDING)
                .metadata(String.format("{\"freelancerName\":\"%s\"}", freelancerName))
                .build();
        
        return createAndSendNotification(notification);
    }
    
    // #3: You have an invite -> freelancer notification (email)
    public Notification createInviteReceivedNotification(Long freelancerId, Long clientId, Long jobId, 
                                                        String jobTitle, String clientName) {
        if (isDuplicateNotification(freelancerId, Notification.NotificationType.INVITE_RECEIVED, jobId, null, null)) {
            log.info("Duplicate INVITE_RECEIVED notification detected for recipient: {}, jobId: {} - skipping", freelancerId, jobId);
            return notificationRepository.findByRecipientIdAndTypeAndJobIdOrderByCreatedAtDesc(freelancerId, 
                    Notification.NotificationType.INVITE_RECEIVED, jobId).stream().findFirst().orElse(null);
        }
        
        Notification notification = Notification.builder()
                .recipientId(freelancerId)
                .senderId(clientId)
                .type(Notification.NotificationType.INVITE_RECEIVED)
                .title("You Have an Invite!")
                .message(String.format("%s has invited you to work on '%s'", 
                        clientName, jobTitle))
                .jobId(jobId)
                .status(Notification.NotificationStatus.PENDING)
                .metadata(String.format("{\"clientName\":\"%s\"}", clientName))
                .build();
        
        return createAndSendNotification(notification);
    }

    // PROPOSAL NOTIFICATIONS (3 types)
    
    // #4: Proposal accepted -> freelancer notification (email)
    public Notification createProposalAcceptedNotification(Long freelancerId, Long clientId, Long jobId, 
                                                          String jobTitle, String clientName) {
        if (isDuplicateNotification(freelancerId, Notification.NotificationType.PROPOSAL_ACCEPTED, jobId, null, null)) {
            log.info("Duplicate PROPOSAL_ACCEPTED notification detected for recipient: {}, jobId: {} - skipping", freelancerId, jobId);
            return notificationRepository.findByRecipientIdAndTypeAndJobIdOrderByCreatedAtDesc(freelancerId, 
                    Notification.NotificationType.PROPOSAL_ACCEPTED, jobId).stream().findFirst().orElse(null);
        }
        
        Notification notification = Notification.builder()
                .recipientId(freelancerId)
                .senderId(clientId)
                .type(Notification.NotificationType.PROPOSAL_ACCEPTED)
                .title("Proposal Accepted!")
                .message(String.format("Congratulations! %s has accepted your proposal for '%s'", 
                        clientName, jobTitle))
                .jobId(jobId)
                .status(Notification.NotificationStatus.PENDING)
                .metadata(String.format("{\"clientName\":\"%s\"}", clientName))
                .build();
        
        return createAndSendNotification(notification);
    }
    
    // #5: Freelancer submitted proposal -> client notification (inbox only)
    public Notification createProposalSubmittedNotification(Long clientId, Long freelancerId, Long jobId, 
                                                           String jobTitle, String freelancerName) {
        if (isDuplicateNotification(clientId, Notification.NotificationType.PROPOSAL_SUBMITTED, jobId, null, null)) {
            log.info("Duplicate PROPOSAL_SUBMITTED notification detected for recipient: {}, jobId: {} - skipping", clientId, jobId);
            return notificationRepository.findByRecipientIdAndTypeAndJobIdOrderByCreatedAtDesc(clientId, 
                    Notification.NotificationType.PROPOSAL_SUBMITTED, jobId).stream().findFirst().orElse(null);
        }
        
        Notification notification = Notification.builder()
                .recipientId(clientId)
                .senderId(freelancerId)
                .type(Notification.NotificationType.PROPOSAL_SUBMITTED)
                .title("New Proposal Received")
                .message(String.format("A freelancer has submitted a proposal on your job '%s'", jobTitle))
                .jobId(jobId)
                .status(Notification.NotificationStatus.PENDING)
                .metadata(String.format("{\"freelancerName\":\"%s\"}", freelancerName))
                .build();
        
        return createAndSendNotification(notification);
    }
    
    // #6: Payment escrow funded after proposal accepted -> client notification (email)
    public Notification createEscrowFundedNotification(Long clientId, Long freelancerId, Long jobId, 
                                                      String jobTitle, String freelancerName) {
        if (isDuplicateNotification(clientId, Notification.NotificationType.ESCROW_FUNDED, jobId, null, null)) {
            log.info("Duplicate ESCROW_FUNDED notification detected for recipient: {}, jobId: {} - skipping", clientId, jobId);
            return notificationRepository.findByRecipientIdAndTypeAndJobIdOrderByCreatedAtDesc(clientId, 
                    Notification.NotificationType.ESCROW_FUNDED, jobId).stream().findFirst().orElse(null);
        }
        
        Notification notification = Notification.builder()
                .recipientId(clientId)
                .senderId(freelancerId)
                .type(Notification.NotificationType.ESCROW_FUNDED)
                .title("Payment Escrow Created")
                .message(String.format("You have accepted the proposal and payment escrow has been made for '%s'", jobTitle))
                .jobId(jobId)
                .status(Notification.NotificationStatus.PENDING)
                .metadata(String.format("{\"freelancerName\":\"%s\"}", freelancerName))
                .build();
        
        return createAndSendNotification(notification);
    }

    // JOB SUBMISSION NOTIFICATIONS (4 types)
    
    // #7: Job submitted -> client notification (email)
    public Notification createJobSubmittedNotification(Long clientId, Long freelancerId, Long jobId, 
                                                      String jobTitle, String freelancerName) {
        if (isDuplicateNotification(clientId, Notification.NotificationType.JOB_SUBMITTED, jobId, null, null)) {
            log.info("Duplicate JOB_SUBMITTED notification detected for recipient: {}, jobId: {} - skipping", clientId, jobId);
            return notificationRepository.findByRecipientIdAndTypeAndJobIdOrderByCreatedAtDesc(clientId, 
                    Notification.NotificationType.JOB_SUBMITTED, jobId).stream().findFirst().orElse(null);
        }
        
        Notification notification = Notification.builder()
                .recipientId(clientId)
                .senderId(freelancerId)
                .type(Notification.NotificationType.JOB_SUBMITTED)
                .title("Job Submitted for Review")
                .message(String.format("Freelancer for '%s' has submitted the work, please review", jobTitle))
                .jobId(jobId)
                .status(Notification.NotificationStatus.PENDING)
                .metadata(String.format("{\"freelancerName\":\"%s\"}", freelancerName))
                .build();
        
        return createAndSendNotification(notification);
    }
    
    // #8: Job rejected/revision -> freelancer notification (inbox only)
    public Notification createJobRejectedNotification(Long freelancerId, Long clientId, Long jobId, 
                                                     String jobTitle, String clientName, String rejectionReason) {
        // Allow multiple rejections/revisions so no duplicate check
        
        Notification notification = Notification.builder()
                .recipientId(freelancerId)
                .senderId(clientId)
                .type(Notification.NotificationType.JOB_REJECTED)
                .title("Job Revision Requested")
                .message(String.format("Client has requested revisions for your job submission '%s'", jobTitle))
                .jobId(jobId)
                .status(Notification.NotificationStatus.PENDING)
                .metadata(String.format("{\"clientName\":\"%s\",\"rejectionReason\":\"%s\"}", 
                        clientName, rejectionReason != null ? rejectionReason : ""))
                .build();
        
        return createAndSendNotification(notification);
    }
    
    // #9: Job accepted -> freelancer notification (email)
    public Notification createJobAcceptedNotification(Long freelancerId, Long clientId, Long jobId, 
                                                     String jobTitle, String clientName) {
        if (isDuplicateNotification(freelancerId, Notification.NotificationType.JOB_ACCEPTED, jobId, null, null)) {
            log.info("Duplicate JOB_ACCEPTED notification detected for recipient: {}, jobId: {} - skipping", freelancerId, jobId);
            return notificationRepository.findByRecipientIdAndTypeAndJobIdOrderByCreatedAtDesc(freelancerId, 
                    Notification.NotificationType.JOB_ACCEPTED, jobId).stream().findFirst().orElse(null);
        }
        
        Notification notification = Notification.builder()
                .recipientId(freelancerId)
                .senderId(clientId)
                .type(Notification.NotificationType.JOB_ACCEPTED)
                .title("Job Accepted - Payment Transferred!")
                .message(String.format("Client has accepted your submission for '%s'. Payment has been transferred!", jobTitle))
                .jobId(jobId)
                .status(Notification.NotificationStatus.PENDING)
                .metadata(String.format("{\"clientName\":\"%s\"}", clientName))
                .build();
        
        return createAndSendNotification(notification);
    }
    
    // #10: Review reminder -> client notification (email)
    public Notification createReviewReminderNotification(Long clientId, Long freelancerId, Long jobId, 
                                                        String jobTitle, String freelancerName) {
        // Allow multiple reminders so no duplicate check for this specific case
        
        Notification notification = Notification.builder()
                .recipientId(clientId)
                .senderId(freelancerId)
                .type(Notification.NotificationType.REVIEW_REMINDER)
                .title("Please Review the Freelancer")
                .message(String.format("After job submission you have accepted for '%s', please go and review the freelancer", jobTitle))
                .jobId(jobId)
                .status(Notification.NotificationStatus.PENDING)
                .metadata(String.format("{\"freelancerName\":\"%s\"}", freelancerName))
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