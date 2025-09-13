package com.thefreelancer.microservices.workspace_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "direct_messages", indexes = {
    @Index(name = "idx_conversation", columnList = "sender_id, receiver_id, created_at"),
    @Index(name = "idx_receiver_unread", columnList = "receiver_id, is_read, created_at"),
    @Index(name = "idx_sender_messages", columnList = "sender_id, created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectMessage {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @Column(name = "sender_id", nullable = false)
    private String senderId; // User ID from Auth Service

    @Column(name = "receiver_id", nullable = false)
    private String receiverId; // User ID from Auth Service

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private Message.MessageType messageType = Message.MessageType.TEXT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private DirectMessage replyToMessage; // For threaded conversations

    @Column(name = "reply_to_id", insertable = false, updatable = false)
    private String replyToId; // For easier queries

    @Column(name = "attachments", columnDefinition = "jsonb")
    private String attachments; // JSON metadata for file attachments

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Helper method to generate conversation ID for consistent ordering
    public String getConversationId() {
        // Always use lexicographically smaller ID first for consistent conversation ID
        if (senderId.compareTo(receiverId) < 0) {
            return senderId + "_" + receiverId;
        } else {
            return receiverId + "_" + senderId;
        }
    }

    // Helper method to check if current user is the sender
    public boolean isSentBy(String userId) {
        return senderId.equals(userId);
    }

    // Helper method to get the other participant in the conversation
    public String getOtherParticipant(String currentUserId) {
        return senderId.equals(currentUserId) ? receiverId : senderId;
    }

    // Mark message as read
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    // Mark message as edited
    public void markAsEdited() {
        this.editedAt = LocalDateTime.now();
    }
}
