package com.thefreelancer.microservices.notification.controller;

import com.thefreelancer.microservices.notification.dto.NotificationResponseDto;
import com.thefreelancer.microservices.notification.model.Notification;
import com.thefreelancer.microservices.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationResponseDto>> getUserNotifications(
            @PathVariable Long userId,
            Pageable pageable) {
        
        Page<NotificationResponseDto> notifications = notificationService.getNotificationsByRecipient(userId, pageable);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<Page<NotificationResponseDto>> getUnreadNotifications(
            @PathVariable Long userId,
            Pageable pageable) {
        
        Page<NotificationResponseDto> notifications = notificationService.getUnreadNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    @GetMapping("/{notificationId}")
    public ResponseEntity<Notification> getNotification(@PathVariable Long notificationId) {
        Optional<Notification> notification = notificationService.getNotificationById(notificationId);
        return notification.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long notificationId) {
        Notification notification = notificationService.markAsRead(notificationId);
        if (notification != null) {
            return ResponseEntity.ok(notification);
        }
        return ResponseEntity.notFound().build();
    }
    
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(@PathVariable Long userId) {
        int updatedCount = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("updatedCount", updatedCount));
    }
    
    // Internal API for inter-service communication
    @PostMapping("/internal/proposal-submitted")
    public ResponseEntity<Notification> createProposalSubmittedNotification(
            @RequestParam Long jobId,
            @RequestParam Long clientId,
            @RequestParam Long freelancerId,
            @RequestParam String jobTitle,
            @RequestParam String freelancerName,
            @RequestParam(required = false) String proposalCover) {
        
        Notification notification = notificationService.createProposalSubmittedNotification(
            jobId, clientId, freelancerId, jobTitle, freelancerName, proposalCover);
        
        return ResponseEntity.ok(notification);
    }
    
    @PostMapping("/internal/proposal-accepted")
    public ResponseEntity<Notification> createProposalAcceptedNotification(
            @RequestParam Long jobId,
            @RequestParam Long freelancerId,
            @RequestParam Long clientId,
            @RequestParam String jobTitle,
            @RequestParam String clientName,
            @RequestParam(required = false) String acceptanceMessage) {
        
        Notification notification = notificationService.createProposalAcceptedNotification(
            jobId, freelancerId, clientId, jobTitle, clientName, acceptanceMessage);
        
        return ResponseEntity.ok(notification);
    }
    
    @PostMapping("/internal/proposal-rejected")
    public ResponseEntity<Notification> createProposalRejectedNotification(
            @RequestParam Long jobId,
            @RequestParam Long freelancerId,
            @RequestParam Long clientId,
            @RequestParam String jobTitle,
            @RequestParam String clientName,
            @RequestParam(required = false) String rejectionMessage) {
        
        Notification notification = notificationService.createProposalRejectedNotification(
            jobId, freelancerId, clientId, jobTitle, clientName, rejectionMessage);
        
        return ResponseEntity.ok(notification);
    }
    
    @PostMapping("/internal/contract-created")
    public ResponseEntity<Notification> createContractCreatedNotification(
            @RequestParam Long contractId,
            @RequestParam Long jobId,
            @RequestParam Long clientId,
            @RequestParam Long freelancerId,
            @RequestParam String jobTitle) {
        
        Notification notification = notificationService.createContractCreatedNotification(
            contractId, jobId, clientId, freelancerId, jobTitle);
        
        return ResponseEntity.ok(notification);
    }
    
    @PostMapping("/internal/milestone-completed")
    public ResponseEntity<Notification> createMilestoneCompletedNotification(
            @RequestParam Long milestoneId,
            @RequestParam Long contractId,
            @RequestParam Long clientId,
            @RequestParam String milestoneTitle,
            @RequestParam String freelancerName) {
        
        Notification notification = notificationService.createMilestoneCompletedNotification(
            milestoneId, contractId, clientId, milestoneTitle, freelancerName);
        
        return ResponseEntity.ok(notification);
    }
    
    @PostMapping("/internal/payment-released")
    public ResponseEntity<Notification> createPaymentReleasedNotification(
            @RequestParam Long milestoneId,
            @RequestParam Long freelancerId,
            @RequestParam Double amount,
            @RequestParam String currency) {
        
        Notification notification = notificationService.createPaymentReleasedNotification(
            milestoneId, freelancerId, amount, currency);
        
        return ResponseEntity.ok(notification);
    }
}
