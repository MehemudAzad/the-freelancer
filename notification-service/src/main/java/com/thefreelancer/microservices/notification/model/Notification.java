package com.thefreelancer.microservices.notification.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "sender_id")
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "proposal_id")
    private Long proposalId;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "reference_url")
    private String referenceUrl;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    public enum NotificationType {
        // Job & Proposal Related
        PROPOSAL_SUBMITTED,
        PROPOSAL_ACCEPTED, 
        PROPOSAL_REJECTED,
        JOB_POSTED,
        JOB_SUBMITTED,
        JOB_ACCEPTED,
        JOB_REJECTED,
        JOB_UPDATED,
        JOB_CLOSED,
        
        // Contract Related
        CONTRACT_CREATED,
        
        // Payment Related
        PAYMENT_RECEIVED,
        PAYMENT_SENT,
        ESCROW_FUNDED,
        ESCROW_RELEASED,
        
        // General
        SYSTEM_ANNOUNCEMENT,
        PROFILE_UPDATE_REQUIRED,
        REVIEW_RECEIVED,
        MESSAGE_RECEIVED
    }

    public enum NotificationStatus {
        PENDING,    // Created but not yet sent
        SENT,       // Successfully sent via various channels
        DELIVERED,  // Confirmed delivered (for push notifications)
        FAILED,     // Failed to send
        CANCELLED   // Cancelled before sending
    }
}
