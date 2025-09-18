package com.thefreelancer.microservices.notification.controller;

import com.thefreelancer.microservices.notification.dto.NotificationResponseDto;
import com.thefreelancer.microservices.notification.model.Notification;
import com.thefreelancer.microservices.notification.service.NotificationService;
import com.thefreelancer.microservices.notification.service.NotificationDeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    
    @Operation(summary = "Get user notifications", description = "Retrieves paginated notifications for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/user")
    public ResponseEntity<Page<NotificationResponseDto>> getUserNotifications(
            @Parameter(description = "Pagination parameters") Pageable pageable,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/notifications/user - Getting authenticated user's notifications");
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for getting notifications");
            return ResponseEntity.status(401).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            Page<NotificationResponseDto> notifications = notificationService.getNotificationsByRecipient(authenticatedUserId, pageable);
            return ResponseEntity.ok(notifications);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(400).build();
        }
    }
    
    @Operation(summary = "Get unread notifications", description = "Retrieves paginated unread notifications for the authenticated user")
    @GetMapping("/user/unread")
    public ResponseEntity<Page<NotificationResponseDto>> getUnreadNotifications(
            Pageable pageable,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/notifications/user/unread - Getting authenticated user's unread notifications");
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for getting unread notifications");
            return ResponseEntity.status(401).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            Page<NotificationResponseDto> notifications = notificationService.getUnreadNotifications(authenticatedUserId, pageable);
            return ResponseEntity.ok(notifications);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(400).build();
        }
    }
    
    @Operation(summary = "Get unread count", description = "Returns the count of unread notifications for the authenticated user")
    @GetMapping("/user/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/notifications/user/unread/count - Getting authenticated user's unread count");
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for getting unread count");
            return ResponseEntity.status(401).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            long count = notificationService.getUnreadCount(authenticatedUserId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(400).build();
        }
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
    
    @Operation(summary = "Mark all notifications as read", description = "Marks all notifications for the authenticated user as read")
    @PutMapping("/user/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("PUT /api/notifications/user/read-all - Marking all notifications as read for authenticated user");
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for marking all notifications as read");
            return ResponseEntity.status(401).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            int updatedCount = notificationService.markAllAsRead(authenticatedUserId);
            return ResponseEntity.ok(Map.of("updatedCount", updatedCount));
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(400).build();
        }
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

    // API to create a message received notification (no email)
    @PostMapping("/internal/message-received")
    public ResponseEntity<Notification> createMessageReceivedNotification(
            @RequestParam Long recipientId,
            @RequestParam Long senderId,
            @RequestParam String senderName,
            @RequestParam Long roomId,
            @RequestParam String messagePreview) {
        Notification notification = notificationService.createMessageReceivedNotification(
                recipientId, senderId, senderName, roomId, messagePreview);
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
    
        @Operation(
            summary = "Create Job Posted Notification",
            description = "Creates a notification when a client posts a new job. This is used internally by the job service.",
            tags = {"Internal APIs"}
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job posted notification created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @PostMapping("/internal/job-posted")
        public ResponseEntity<Notification> createJobPostedNotification(
                @Parameter(description = "Job ID", required = true)
                @RequestParam Long jobId,
                @Parameter(description = "Job title", required = true)
                @RequestParam String jobTitle,
                @Parameter(description = "Client name", required = true)
                @RequestParam String clientName,
                @Parameter(description = "Job description")
                @RequestParam(required = false) String jobDescription,
                @Parameter(description = "Required skills for the job")
                @RequestParam(required = false) String[] requiredSkills,
                @Parameter(description = "Budget range (e.g., '$1000-$5000')")
                @RequestParam(required = false) String budgetRange,
                @Parameter(description = "Job category")
                @RequestParam(required = false) String category) {
        
            Notification notification = notificationService.createJobPostedNotification(
                jobId, jobTitle, clientName, jobDescription, requiredSkills, budgetRange, category);
        
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
    
    @GetMapping("/user/stats")
    public ResponseEntity<NotificationDeliveryService.NotificationStats> getNotificationStats(
            @RequestParam(defaultValue = "30") int days,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/notifications/user/stats - Getting notification stats for authenticated user");
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for getting notification stats");
            return ResponseEntity.status(401).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            NotificationDeliveryService.NotificationStats stats = deliveryService.getDeliveryStats(authenticatedUserId, days);
            return ResponseEntity.ok(stats);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(400).build();
        }
    }
}
