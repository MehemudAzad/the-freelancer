package com.thefreelancer.microservices.notification.repository;

import com.thefreelancer.microservices.notification.model.Notification;
import com.thefreelancer.microservices.notification.model.Notification.NotificationStatus;
import com.thefreelancer.microservices.notification.model.Notification.NotificationType;

// import com.thefreelancer.microservices.notification.model.NotificationStatus;
// import com.thefreelancer.microservices.notification.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find notifications by recipient
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
    
    // Find unread notifications
    Page<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
    
    // Find notifications by type
    List<Notification> findByRecipientIdAndTypeOrderByCreatedAtDesc(Long recipientId, NotificationType type);
    
    // Find notifications by status
    List<Notification> findByRecipientIdAndStatusOrderByCreatedAtDesc(Long recipientId, NotificationStatus status);
    
    // Find notifications by job
    List<Notification> findByJobIdOrderByCreatedAtDesc(Long jobId);
    
    // Count unread notifications
    long countByRecipientIdAndIsReadFalse(Long recipientId);
    
    // Count notifications by status
    long countByRecipientIdAndStatus(Long recipientId, NotificationStatus status);
    
    // Mark notification as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.id = :id")
    int markAsRead(@Param("id") Long id, @Param("readAt") LocalDateTime readAt);
    
    // Mark all notifications as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.recipientId = :recipientId AND n.isRead = false")
    int markAllAsReadForUser(@Param("recipientId") Long recipientId, @Param("readAt") LocalDateTime readAt);
    
    // Mark notification as delivered
    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.deliveredAt = :deliveredAt WHERE n.id = :id")
    int markAsDelivered(@Param("id") Long id, @Param("status") NotificationStatus status, @Param("deliveredAt") LocalDateTime deliveredAt);
    
    // Find pending notifications for processing
    List<Notification> findByStatusOrderByCreatedAtAsc(NotificationStatus status);
    
    // Find notifications created within time range
    List<Notification> findByRecipientIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long recipientId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Delete old notifications (cleanup)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteNotificationsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Find notifications that need retry (failed status and retry count < max)
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND n.retryCount < :maxRetries")
    List<Notification> findFailedNotificationsForRetry(@Param("maxRetries") int maxRetries);
    
    // Idempotency check - check if notification already exists for the same event
    boolean existsByRecipientIdAndTypeAndJobIdAndProposalIdAndContractId(
        Long recipientId, NotificationType type, Long jobId, Long proposalId, Long contractId);
    
    // Find specific notification by recipient, type and jobId for duplicate checking
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.type = :type AND n.jobId = :jobId ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientIdAndTypeAndJobIdOrderByCreatedAtDesc(
        @Param("recipientId") Long recipientId, @Param("type") NotificationType type, @Param("jobId") Long jobId);
}
