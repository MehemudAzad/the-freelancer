package com.thefreelancer.microservices.workspace_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "workspace_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "milestone_id")
    private Long milestoneId; // Links to contract milestone from job-proposal-service

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    @Column(name = "assignee_id")
    private String assigneeId; // Usually the freelancer

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "created_by_id", nullable = false)
    private String createdById; // User who created the task

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum TaskStatus {
        TODO,
        IN_PROGRESS,
        REVIEW,
        COMPLETED,
        BLOCKED
    }
}
