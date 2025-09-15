package com.thefreelancer.microservices.notification.service;

import com.thefreelancer.microservices.notification.dto.NotificationResponseDto;
import com.thefreelancer.microservices.notification.model.Notification;
import com.thefreelancer.microservices.notification.model.NotificationStatus;
import com.thefreelancer.microservices.notification.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationService {
    
    // Core CRUD operations
    Notification createNotification(Notification notification);
    Optional<Notification> getNotificationById(Long id);
    Page<NotificationResponseDto> getNotificationsByRecipient(Long recipientId, Pageable pageable);
    Page<NotificationResponseDto> getUnreadNotifications(Long recipientId, Pageable pageable);
    
    // Notification creation methods
    Notification createProposalSubmittedNotification(Long jobId, Long clientId, Long freelancerId, 
        String jobTitle, String freelancerName, String proposalCover);
    
    Notification createProposalAcceptedNotification(Long jobId, Long freelancerId, Long clientId, 
        String jobTitle, String clientName, String acceptanceMessage);
    
    Notification createProposalRejectedNotification(Long jobId, Long freelancerId, Long clientId, 
        String jobTitle, String clientName, String rejectionMessage);
    
    Notification createJobPostedNotification(Long jobId, String jobTitle, String clientName, 
        List<Long> interestedFreelancerIds);
    
    Notification createContractCreatedNotification(Long contractId, Long jobId, Long clientId, 
        Long freelancerId, String jobTitle);
    
    Notification createMilestoneCompletedNotification(Long milestoneId, Long contractId, Long clientId, 
        String milestoneTitle, String freelancerName);
    
    Notification createPaymentReleasedNotification(Long milestoneId, Long freelancerId, 
        Double amount, String currency);
    
    // Status management
    Notification markAsRead(Long notificationId);
    int markAllAsRead(Long recipientId);
    Notification updateStatus(Long notificationId, NotificationStatus status);
    
    // Query methods
    long getUnreadCount(Long recipientId);
    List<Notification> getNotificationsByType(Long recipientId, NotificationType type);
    List<Notification> getNotificationsByJob(Long jobId);
    List<Notification> getNotificationsByStatus(Long recipientId, NotificationStatus status);
    List<Notification> getNotificationsInDateRange(Long recipientId, LocalDateTime start, LocalDateTime end);
    
    // Delivery and retry
    void processNotificationDelivery(Long notificationId);
    void retryFailedNotifications();
    List<Notification> getPendingNotifications();
    
    // Cleanup
    int deleteOldNotifications(LocalDateTime cutoffDate);
    
    // Real-time notifications
    void sendRealtimeNotification(Long recipientId, Notification notification);
    
    // Email notifications
    void sendEmailNotification(Notification notification);
    
    // Push notifications (future)
    void sendPushNotification(Notification notification);
}
