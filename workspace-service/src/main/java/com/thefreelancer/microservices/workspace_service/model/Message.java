package com.thefreelancer.microservices.workspace_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "room_id", insertable = false, updatable = false)
    private Long roomId; // For easier queries

    @Column(name = "sender_id", nullable = false)
    private Long senderId; // User ID from Auth Service (numeric)

    @Column(name = "sender_name", nullable = false)
    private String senderName; // Display name of the sender

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private Message replyToMessage; // For threaded conversations

    @Column(name = "reply_to_id", insertable = false, updatable = false)
    private Long replyToId; // For easier queries (stores the message ID)

    @Column(name = "attachments", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode attachments; // JSON metadata for file attachments (jsonb)

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum MessageType {
        TEXT,
        FILE,
        MILESTONE_UPDATE,
        SYSTEM,
        IMAGE,
        VIDEO,
        DOCUMENT
    }
}
