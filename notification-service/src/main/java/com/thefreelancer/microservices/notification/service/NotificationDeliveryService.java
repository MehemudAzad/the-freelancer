package com.thefreelancer.microservices.notification.service;

import com.thefreelancer.microservices.notification.model.Notification;
import com.thefreelancer.microservices.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDeliveryService {
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    
    @Value("${app.notification.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${app.notification.retry.delay-minutes:5}")
    private int retryDelayMinutes;
    
    @Value("${app.notification.cleanup.days:30}")
    private int cleanupDays;
    
    /**
     * Retry failed notifications periodically
     */
    @Scheduled(fixedDelayString = "${app.notification.retry.interval:300000}") // 5 minutes
    @Transactional
    public void retryFailedNotifications() {
        try {
            List<Notification> failedNotifications = notificationRepository
                .findFailedNotificationsForRetry(maxRetryAttempts);
            
            if (failedNotifications.isEmpty()) {
                return;
            }
            
            log.info("Found {} failed notifications to retry", failedNotifications.size());
            
            for (Notification notification : failedNotifications) {
                if (shouldRetryNotification(notification)) {
                    retryNotificationDelivery(notification);
                }
            }
            
        } catch (Exception e) {
            log.error("Error during failed notification retry process: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Clean up old notifications
     */
    @Scheduled(cron = "${app.notification.cleanup.cron:0 0 2 * * ?}") // Daily at 2 AM
    @Transactional
    public void cleanupOldNotifications() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(cleanupDays);
            int deletedCount = notificationRepository.deleteNotificationsOlderThan(cutoffDate);
            
            if (deletedCount > 0) {
                log.info("Cleaned up {} old notifications older than {} days", deletedCount, cleanupDays);
            }
            
        } catch (Exception e) {
            log.error("Error during notification cleanup: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Process pending notifications
     */
    @Scheduled(fixedDelayString = "${app.notification.process.interval:60000}") // 1 minute
    @Transactional
    public void processPendingNotifications() {
        try {
            List<Notification> pendingNotifications = notificationRepository
                .findByStatusOrderByCreatedAtAsc(Notification.NotificationStatus.PENDING);
            
            if (pendingNotifications.isEmpty()) {
                return;
            }
            
            log.debug("Processing {} pending notifications", pendingNotifications.size());
            
            for (Notification notification : pendingNotifications) {
                processNotificationAsync(notification);
            }
            
        } catch (Exception e) {
            log.error("Error processing pending notifications: {}", e.getMessage(), e);
        }
    }
    
    @Async
    public CompletableFuture<Void> processNotificationAsync(Notification notification) {
        try {
            // Send email notification
            emailService.sendNotificationEmail(notification);
            
            // Update status to sent
            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            
            log.debug("Successfully processed notification {}", notification.getId());
            
        } catch (Exception e) {
            log.error("Failed to process notification {}: {}", notification.getId(), e.getMessage());
            
            // Mark as failed and increment retry count
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    private boolean shouldRetryNotification(Notification notification) {
        // Don't retry if max attempts reached
        if (notification.getRetryCount() >= maxRetryAttempts) {
            log.debug("Max retry attempts reached for notification {}", notification.getId());
            return false;
        }
        
        // Don't retry if it's too recent (wait for delay)
        if (notification.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(retryDelayMinutes))) {
            return false;
        }
        
        return true;
    }
    
    private void retryNotificationDelivery(Notification notification) {
        try {
            log.debug("Retrying notification delivery for notification {}", notification.getId());
            
            // Reset status to pending for retry
            notification.setStatus(Notification.NotificationStatus.PENDING);
            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);
            
            // Process the notification
            processNotificationAsync(notification);
            
        } catch (Exception e) {
            log.error("Error retrying notification {}: {}", notification.getId(), e.getMessage());
            
            // Mark as failed again
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notificationRepository.save(notification);
        }
    }
    
    /**
     * Mark notification as delivered (e.g., when user opens email or WebSocket confirms delivery)
     */
    public void markAsDelivered(Long notificationId) {
        try {
            var notificationOpt = notificationRepository.findById(notificationId);
            if (notificationOpt.isPresent()) {
                Notification notification = notificationOpt.get();
                notification.setStatus(Notification.NotificationStatus.DELIVERED);
                notification.setDeliveredAt(LocalDateTime.now());
                notificationRepository.save(notification);
                
                log.debug("Marked notification {} as delivered", notificationId);
            }
        } catch (Exception e) {
            log.error("Error marking notification {} as delivered: {}", notificationId, e.getMessage());
        }
    }
    
    /**
     * Get delivery statistics
     */
    public NotificationStats getDeliveryStats(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        
        List<Notification> notifications = notificationRepository
            .findByRecipientIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, since, LocalDateTime.now());
        
        long totalCount = notifications.size();
        long sentCount = notifications.stream()
            .mapToLong(n -> n.getStatus() == Notification.NotificationStatus.SENT ? 1 : 0)
            .sum();
        long deliveredCount = notifications.stream()
            .mapToLong(n -> n.getStatus() == Notification.NotificationStatus.DELIVERED ? 1 : 0)
            .sum();
        long failedCount = notifications.stream()
            .mapToLong(n -> n.getStatus() == Notification.NotificationStatus.FAILED ? 1 : 0)
            .sum();
        long readCount = notifications.stream()
            .mapToLong(n -> n.getIsRead() ? 1 : 0)
            .sum();
        
        return NotificationStats.builder()
            .totalNotifications(totalCount)
            .sentNotifications(sentCount)
            .deliveredNotifications(deliveredCount)
            .failedNotifications(failedCount)
            .readNotifications(readCount)
            .deliveryRate(totalCount > 0 ? (double) sentCount / totalCount : 0.0)
            .readRate(totalCount > 0 ? (double) readCount / totalCount : 0.0)
            .build();
    }
    
    public static class NotificationStats {
        private long totalNotifications;
        private long sentNotifications;
        private long deliveredNotifications;
        private long failedNotifications;
        private long readNotifications;
        private double deliveryRate;
        private double readRate;
        
        public static NotificationStatsBuilder builder() {
            return new NotificationStatsBuilder();
        }
        
        public static class NotificationStatsBuilder {
            private long totalNotifications;
            private long sentNotifications;
            private long deliveredNotifications;
            private long failedNotifications;
            private long readNotifications;
            private double deliveryRate;
            private double readRate;
            
            public NotificationStatsBuilder totalNotifications(long totalNotifications) {
                this.totalNotifications = totalNotifications;
                return this;
            }
            
            public NotificationStatsBuilder sentNotifications(long sentNotifications) {
                this.sentNotifications = sentNotifications;
                return this;
            }
            
            public NotificationStatsBuilder deliveredNotifications(long deliveredNotifications) {
                this.deliveredNotifications = deliveredNotifications;
                return this;
            }
            
            public NotificationStatsBuilder failedNotifications(long failedNotifications) {
                this.failedNotifications = failedNotifications;
                return this;
            }
            
            public NotificationStatsBuilder readNotifications(long readNotifications) {
                this.readNotifications = readNotifications;
                return this;
            }
            
            public NotificationStatsBuilder deliveryRate(double deliveryRate) {
                this.deliveryRate = deliveryRate;
                return this;
            }
            
            public NotificationStatsBuilder readRate(double readRate) {
                this.readRate = readRate;
                return this;
            }
            
            public NotificationStats build() {
                NotificationStats stats = new NotificationStats();
                stats.totalNotifications = this.totalNotifications;
                stats.sentNotifications = this.sentNotifications;
                stats.deliveredNotifications = this.deliveredNotifications;
                stats.failedNotifications = this.failedNotifications;
                stats.readNotifications = this.readNotifications;
                stats.deliveryRate = this.deliveryRate;
                stats.readRate = this.readRate;
                return stats;
            }
        }
        
        // Getters
        public long getTotalNotifications() { return totalNotifications; }
        public long getSentNotifications() { return sentNotifications; }
        public long getDeliveredNotifications() { return deliveredNotifications; }
        public long getFailedNotifications() { return failedNotifications; }
        public long getReadNotifications() { return readNotifications; }
        public double getDeliveryRate() { return deliveryRate; }
        public double getReadRate() { return readRate; }
    }
}