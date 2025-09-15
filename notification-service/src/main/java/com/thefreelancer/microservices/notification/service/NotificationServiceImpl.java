package com.thefreelancer.microservices.notification.service;

import com.thefreelancer.microservices.notification.dto.NotificationResponseDto;
import com.thefreelancer.microservices.notification.model.Notification;
import com.thefreelancer.microservices.notification.model.NotificationStatus;
import com.thefreelancer.microservices.notification.model.NotificationType;
import com.thefreelancer.microservices.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebClient webClient;
    private final EmailService emailService;
    
    @Override
    public Notification createNotification(Notification notification) {
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(LocalDateTime.now());
        }
        if (notification.getStatus() == null) {
            notification.setStatus(NotificationStatus.PENDING);
        }
        
        Notification saved = notificationRepository.save(notification);
        
        // Process notification delivery asynchronously
        processNotificationDelivery(saved.getId());
        
        return saved;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getNotificationsByRecipient(Long recipientId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable);
        List<NotificationResponseDto> dtos = notifications.getContent().stream()
            .map(this::enrichNotificationDto)
            .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, notifications.getTotalElements());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getUnreadNotifications(Long recipientId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(recipientId, pageable);
        List<NotificationResponseDto> dtos = notifications.getContent().stream()
            .map(this::enrichNotificationDto)
            .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, notifications.getTotalElements());
    }
    
    @Override
    public Notification createProposalSubmittedNotification(Long jobId, Long clientId, Long freelancerId, 
            String jobTitle, String freelancerName, String proposalCover) {
        
        Notification notification = Notification.builder()
            .recipientId(clientId)
            .senderId(freelancerId)
            .type(NotificationType.PROPOSAL_SUBMITTED)
            .title("New Proposal Received")
            .message(String.format("%s has submitted a proposal for your job '%s'", freelancerName, jobTitle))
            .jobId(jobId)
            .referenceId(jobId) // Proposal ID would be better here
            .referenceType("PROPOSAL")
            .status(NotificationStatus.PENDING)
            .isRead(false)
            .createdAt(LocalDateTime.now())
            .build();
            
        return createNotification(notification);
    }
    
    @Override
    public Notification createProposalAcceptedNotification(Long jobId, Long freelancerId, Long clientId, 
            String jobTitle, String clientName, String acceptanceMessage) {
        
        Notification notification = Notification.builder()
            .recipientId(freelancerId)
            .senderId(clientId)
            .type(NotificationType.PROPOSAL_ACCEPTED)
            .title("Proposal Accepted!")
            .message(String.format("Congratulations! %s has accepted your proposal for '%s'", clientName, jobTitle))
            .jobId(jobId)
            .referenceId(jobId)
            .referenceType("PROPOSAL")
            .status(NotificationStatus.PENDING)
            .isRead(false)
            .createdAt(LocalDateTime.now())
            .build();
            
        return createNotification(notification);
    }
    
    @Override
    public Notification createProposalRejectedNotification(Long jobId, Long freelancerId, Long clientId, 
            String jobTitle, String clientName, String rejectionMessage) {
        
        Notification notification = Notification.builder()
            .recipientId(freelancerId)
            .senderId(clientId)
            .type(NotificationType.PROPOSAL_REJECTED)
            .title("Proposal Update")
            .message(String.format("Your proposal for '%s' was not selected this time", jobTitle))
            .jobId(jobId)
            .referenceId(jobId)
            .referenceType("PROPOSAL")
            .status(NotificationStatus.PENDING)
            .isRead(false)
            .createdAt(LocalDateTime.now())
            .build();
            
        return createNotification(notification);
    }
    
    @Override
    public Notification createJobPostedNotification(Long jobId, String jobTitle, String clientName, 
            List<Long> interestedFreelancerIds) {
        // This method would typically create multiple notifications
        // For now, creating one as example
        return null; // Implementation depends on matching algorithm
    }
    
    @Override
    public Notification createContractCreatedNotification(Long contractId, Long jobId, Long clientId, 
            Long freelancerId, String jobTitle) {
        
        Notification notification = Notification.builder()
            .recipientId(freelancerId)
            .senderId(clientId)
            .type(NotificationType.CONTRACT_CREATED)
            .title("Contract Created")
            .message(String.format("A contract has been created for '%s'. Please review the terms.", jobTitle))
            .jobId(jobId)
            .referenceId(contractId)
            .referenceType("CONTRACT")
            .status(NotificationStatus.PENDING)
            .isRead(false)
            .createdAt(LocalDateTime.now())
            .build();
            
        return createNotification(notification);
    }
    
    @Override
    public Notification createMilestoneCompletedNotification(Long milestoneId, Long contractId, Long clientId, 
            String milestoneTitle, String freelancerName) {
        
        Notification notification = Notification.builder()
            .recipientId(clientId)
            .senderId(null) // System notification
            .type(NotificationType.MILESTONE_COMPLETED)
            .title("Milestone Completed")
            .message(String.format("%s has completed the milestone: %s", freelancerName, milestoneTitle))
            .referenceId(milestoneId)
            .referenceType("MILESTONE")
            .status(NotificationStatus.PENDING)
            .isRead(false)
            .createdAt(LocalDateTime.now())
            .build();
            
        return createNotification(notification);
    }
    
    @Override
    public Notification createPaymentReleasedNotification(Long milestoneId, Long freelancerId, 
            Double amount, String currency) {
        
        Notification notification = Notification.builder()
            .recipientId(freelancerId)
            .senderId(null) // System notification
            .type(NotificationType.PAYMENT_RELEASED)
            .title("Payment Released")
            .message(String.format("Payment of %s %.2f has been released to your account", currency, amount))
            .referenceId(milestoneId)
            .referenceType("PAYMENT")
            .status(NotificationStatus.PENDING)
            .isRead(false)
            .createdAt(LocalDateTime.now())
            .build();
            
        return createNotification(notification);
    }
    
    @Override
    public Notification markAsRead(Long notificationId) {
        Optional<Notification> optNotification = notificationRepository.findById(notificationId);
        if (optNotification.isPresent()) {
            Notification notification = optNotification.get();
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            return notificationRepository.save(notification);
        }
        return null;
    }
    
    @Override
    public int markAllAsRead(Long recipientId) {
        return notificationRepository.markAllAsReadForUser(recipientId, LocalDateTime.now());
    }
    
    @Override
    public Notification updateStatus(Long notificationId, NotificationStatus status) {
        Optional<Notification> optNotification = notificationRepository.findById(notificationId);
        if (optNotification.isPresent()) {
            Notification notification = optNotification.get();
            notification.setStatus(status);
            
            if (status == NotificationStatus.DELIVERED) {
                notification.setDeliveredAt(LocalDateTime.now());
            } else if (status == NotificationStatus.FAILED) {
                notification.setRetryCount(notification.getRetryCount() + 1);
            }
            
            return notificationRepository.save(notification);
        }
        return null;
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByType(Long recipientId, NotificationType type) {
        return notificationRepository.findByRecipientIdAndTypeOrderByCreatedAtDesc(recipientId, type);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByJob(Long jobId) {
        return notificationRepository.findByJobIdOrderByCreatedAtDesc(jobId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByStatus(Long recipientId, NotificationStatus status) {
        return notificationRepository.findByRecipientIdAndStatusOrderByCreatedAtDesc(recipientId, status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsInDateRange(Long recipientId, LocalDateTime start, LocalDateTime end) {
        return notificationRepository.findByRecipientIdAndCreatedAtBetweenOrderByCreatedAtDesc(recipientId, start, end);
    }
    
    @Override
    public void processNotificationDelivery(Long notificationId) {
        Optional<Notification> optNotification = notificationRepository.findById(notificationId);
        if (optNotification.isPresent()) {
            Notification notification = optNotification.get();
            
            try {
                // Send real-time notification via WebSocket
                sendRealtimeNotification(notification.getRecipientId(), notification);
                
                // Send email notification
                sendEmailNotification(notification);
                
                // Update status to delivered
                updateStatus(notificationId, NotificationStatus.DELIVERED);
                
            } catch (Exception e) {
                log.error("Failed to deliver notification {}: {}", notificationId, e.getMessage());
                updateStatus(notificationId, NotificationStatus.FAILED);
            }
        }
    }
    
    @Override
    public void retryFailedNotifications() {
        List<Notification> failedNotifications = notificationRepository.findFailedNotificationsForRetry(3);
        
        for (Notification notification : failedNotifications) {
            log.info("Retrying failed notification: {}", notification.getId());
            processNotificationDelivery(notification.getId());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Notification> getPendingNotifications() {
        return notificationRepository.findByStatusOrderByCreatedAtAsc(NotificationStatus.PENDING);
    }
    
    @Override
    public int deleteOldNotifications(LocalDateTime cutoffDate) {
        return notificationRepository.deleteNotificationsOlderThan(cutoffDate);
    }
    
    @Override
    public void sendRealtimeNotification(Long recipientId, Notification notification) {
        try {
            NotificationResponseDto dto = enrichNotificationDto(notification);
            messagingTemplate.convertAndSend("/topic/notifications/" + recipientId, dto);
            log.debug("Real-time notification sent to user {}", recipientId);
        } catch (Exception e) {
            log.error("Failed to send real-time notification: {}", e.getMessage());
        }
    }
    
    @Override
    public void sendEmailNotification(Notification notification) {
        try {
            emailService.sendNotificationEmail(notification);
            log.debug("Email notification sent for notification {}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send email notification: {}", e.getMessage());
        }
    }
    
    @Override
    public void sendPushNotification(Notification notification) {
        // TODO: Implement push notification service
        log.info("Push notification not yet implemented for notification {}", notification.getId());
    }
    
    private NotificationResponseDto enrichNotificationDto(Notification notification) {
        NotificationResponseDto dto = new NotificationResponseDto();
        dto.setId(notification.getId());
        dto.setRecipientId(notification.getRecipientId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setJobId(notification.getJobId());
        dto.setReferenceId(notification.getReferenceId());
        dto.setReferenceType(notification.getReferenceType());
        dto.setStatus(notification.getStatus());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setReadAt(notification.getReadAt());
        dto.setDeliveredAt(notification.getDeliveredAt());
        
        // Enrich with sender info if available
        if (notification.getSenderId() != null) {
            // TODO: Call auth-service to get sender details
            NotificationResponseDto.SenderInfo senderInfo = new NotificationResponseDto.SenderInfo();
            senderInfo.setId(notification.getSenderId());
            // senderInfo.setName(fetchUserName(notification.getSenderId()));
            // senderInfo.setHandle(fetchUserHandle(notification.getSenderId()));
            dto.setSenderInfo(senderInfo);
        }
        
        // Enrich with job info if available
        if (notification.getJobId() != null) {
            // TODO: Call job-service to get job details
            NotificationResponseDto.JobInfo jobInfo = new NotificationResponseDto.JobInfo();
            jobInfo.setId(notification.getJobId());
            // jobInfo.setTitle(fetchJobTitle(notification.getJobId()));
            dto.setJobInfo(jobInfo);
        }
        
        return dto;
    }
}
