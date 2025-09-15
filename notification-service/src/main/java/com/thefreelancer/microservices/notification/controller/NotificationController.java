package com.thefreelancer.microservices.notification.controller;

import com.thefreelancer.microservices.notification.dto.NotificationResponseDto;
import com.thefreelancer.microservices.notification.model.Notification;
import com.thefreelancer.microservices.notification.service.NotificationService;
import com.thefreelancer.microservices.notification.service.NotificationDeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Notification Management", description = "API for managing user notifications, delivery tracking, and real-time messaging")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final NotificationDeliveryService deliveryService;
    
    @Operation(summary = "Get user notifications", description = "Retrieves paginated notifications for a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationResponseDto>> getUserNotifications(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        
        Page<NotificationResponseDto> notifications = notificationService.getNotificationsByRecipient(userId, pageable);
        return ResponseEntity.ok(notifications);
    }
    
    @Operation(summary = "Get unread notifications", description = "Retrieves paginated unread notifications for a specific user")
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<Page<NotificationResponseDto>> getUnreadNotifications(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            Pageable pageable) {
        
        Page<NotificationResponseDto> notifications = notificationService.getUnreadNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }
    
    @Operation(summary = "Get unread count", description = "Returns the count of unread notifications for a user")
    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    @Operation(summary = "Get notification by ID", description = "Retrieves a specific notification by its ID")
    @GetMapping("/{notificationId}")
    public ResponseEntity<Notification> getNotification(
            @Parameter(description = "Notification ID", required = true) @PathVariable Long notificationId) {
        Optional<Notification> notification = notificationService.getNotificationById(notificationId);
        return notification.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Mark notification as read", description = "Marks a specific notification as read")
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(
            @Parameter(description = "Notification ID", required = true) @PathVariable Long notificationId) {
        Notification notification = notificationService.markAsRead(notificationId);
        if (notification != null) {
            return ResponseEntity.ok(notification);
        }
        return ResponseEntity.notFound().build();
    }
    
    @Operation(summary = "Mark all notifications as read", description = "Marks all notifications for a user as read")
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        int updatedCount = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("updatedCount", updatedCount));
    }
    
    // Internal API for inter-service communication
    @Tag(name = "Internal APIs", description = "Internal endpoints for inter-service communication")
    @Operation(summary = "Create proposal submitted notification", description = "Internal API to create notification when a proposal is submitted")
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
    
    @PostMapping("/internal/job-submitted")
    public ResponseEntity<Notification> createJobSubmittedNotification(
            @RequestParam Long jobId,
            @RequestParam Long contractId,
            @RequestParam Long clientId,
            @RequestParam String jobTitle,
            @RequestParam String freelancerName) {
        
        Notification notification = notificationService.createJobSubmittedNotification(
            jobId, contractId, clientId, jobTitle, freelancerName);
        
        return ResponseEntity.ok(notification);
    }
    
    @PostMapping("/internal/payment-released")
    public ResponseEntity<Notification> createPaymentReleasedNotification(
            @RequestParam Long jobId,
            @RequestParam Long freelancerId,
            @RequestParam Double amount,
            @RequestParam String currency,
            @RequestParam String jobTitle) {
        
        Notification notification = notificationService.createPaymentReleasedNotification(
            jobId, freelancerId, amount, currency, jobTitle);
        
        return ResponseEntity.ok(notification);
    }
    
    @PutMapping("/{notificationId}/delivered")
    public ResponseEntity<Void> markAsDelivered(@PathVariable Long notificationId) {
        deliveryService.markAsDelivered(notificationId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<NotificationDeliveryService.NotificationStats> getNotificationStats(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        NotificationDeliveryService.NotificationStats stats = deliveryService.getDeliveryStats(userId, days);
        return ResponseEntity.ok(stats);
    }
}
