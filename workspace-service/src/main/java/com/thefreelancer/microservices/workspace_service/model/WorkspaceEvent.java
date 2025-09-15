package com.thefreelancer.microservices.workspace_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "workspace_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
public class WorkspaceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "attendees", columnDefinition = "jsonb")
    private String attendees; // JSON array of user emails

    @Column(name = "created_by_id", nullable = false)
    private String createdById;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum EventType {
        MILESTONE_DEADLINE,
        MEETING,
        STANDUP,
        REVIEW,
        PRESENTATION,
        DEADLINE,
        REMINDER
    }
}
