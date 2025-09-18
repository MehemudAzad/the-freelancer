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
        // Invite Related (3 types)
        INVITE_SENT,              // #1: Client sent invite - client notification
        INVITE_ACCEPTED,          // #2: Freelancer accepted invite - client notification (email)
        INVITE_RECEIVED,          // #3: Freelancer received invite - freelancer notification (email)
        
        // Proposal Related (3 types)
        PROPOSAL_SUBMITTED,       // #5: Freelancer submitted proposal - client notification (inbox only)
        PROPOSAL_ACCEPTED,        // #4: Client accepted proposal - freelancer notification (email)
        ESCROW_FUNDED,           // #6: Payment escrow made after proposal accepted - client notification (email)
        
        // Job Submission Related (4 types)
        JOB_SUBMITTED,           // #7: Freelancer submitted job - client notification (email)
        JOB_REJECTED,            // #8: Client rejected job submission - freelancer notification (inbox only)
        JOB_ACCEPTED,            // #9: Client accepted job submission - freelancer notification (email)
        REVIEW_REMINDER          // #10: Remind client to review freelancer - client notification (email)
    }

    public enum NotificationStatus {
        PENDING,    // Created but not yet sent
        SENT,       // Successfully sent via various channels
        DELIVERED,  // Confirmed delivered (for push notifications)
        FAILED,     // Failed to send
        CANCELLED   // Cancelled before sending
    }
}
