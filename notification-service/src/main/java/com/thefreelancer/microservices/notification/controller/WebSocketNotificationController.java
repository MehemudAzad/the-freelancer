package com.thefreelancer.microservices.notification.controller;

import com.thefreelancer.microservices.notification.dto.NotificationResponseDto;
import com.thefreelancer.microservices.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationController {
    
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    
    @SubscribeMapping("/user/queue/notifications")
    public void subscribeToNotifications(Principal principal) {
        if (principal != null) {
            log.info("User {} subscribed to notifications", principal.getName());
            
            // Send unread notification count upon subscription
            Long userId = Long.parseLong(principal.getName());
            long unreadCount = notificationService.getUnreadCount(userId);
            
            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/unread-count",
                unreadCount
            );
        }
    }
    
    @MessageMapping("/notifications/mark-read/{notificationId}")
    public void markNotificationAsRead(@DestinationVariable Long notificationId, Principal principal) {
        if (principal != null) {
            try {
                notificationService.markAsRead(notificationId);
                
                // Send updated unread count
                Long userId = Long.parseLong(principal.getName());
                long unreadCount = notificationService.getUnreadCount(userId);
                
                messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/unread-count",
                    unreadCount
                );
                
                log.debug("Marked notification {} as read for user {}", notificationId, principal.getName());
            } catch (Exception e) {
                log.error("Error marking notification as read: {}", e.getMessage());
            }
        }
    }
    
    @MessageMapping("/notifications/mark-all-read")
    public void markAllNotificationsAsRead(Principal principal) {
        if (principal != null) {
            try {
                Long userId = Long.parseLong(principal.getName());
                notificationService.markAllAsRead(userId);
                
                // Send updated unread count (should be 0)
                messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/unread-count",
                    0L
                );
                
                log.debug("Marked all notifications as read for user {}", principal.getName());
            } catch (Exception e) {
                log.error("Error marking all notifications as read: {}", e.getMessage());
            }
        }
    }
    
    @MessageMapping("/notifications/connect")
    public void handleConnection(Principal principal) {
        if (principal != null) {
            log.info("User {} connected to notification WebSocket", principal.getName());
            
            // Send welcome message or initial data
            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/connection",
                "Connected to notification service"
            );
        }
    }
    
    // Method to send notification to specific user (called by service)
    public void sendNotificationToUser(String userId, NotificationResponseDto notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/notifications",
                notification
            );
            log.debug("Sent real-time notification to user {}", userId);
        } catch (Exception e) {
            log.error("Failed to send real-time notification to user {}: {}", userId, e.getMessage());
        }
    }
    
    // Method to broadcast system notifications
    public void broadcastSystemNotification(NotificationResponseDto notification) {
        try {
            messagingTemplate.convertAndSend("/topic/system", notification);
            log.debug("Broadcasted system notification: {}", notification.getTitle());
        } catch (Exception e) {
            log.error("Failed to broadcast system notification: {}", e.getMessage());
        }
    }
}