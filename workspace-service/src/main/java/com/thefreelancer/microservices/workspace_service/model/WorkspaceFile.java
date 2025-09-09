package com.thefreelancer.microservices.workspace_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "workspace_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "uploader_id", nullable = false)
    private String uploaderId; // User ID from Auth Service

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "file_url", nullable = false)
    private String fileUrl; // S3/CloudFlare R2 URL

    @Column(name = "file_size")
    private Long fileSize; // Size in bytes

    @Column(name = "content_type")
    private String contentType; // MIME type

    @Enumerated(EnumType.STRING)
    @Column(name = "file_category")
    private FileCategory fileCategory = FileCategory.OTHER;

    @Column(name = "milestone_id")
    private Long milestoneId; // Optional: link to specific contract milestone

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Optional file description

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum FileCategory {
        DELIVERABLE,     // Work deliverables from freelancer
        REFERENCE,       // Reference materials (specs, designs)
        FEEDBACK,        // Feedback files from client
        DOCUMENTATION,   // Project documentation
        ASSETS,          // Images, videos, design assets
        OTHER           // Miscellaneous files
    }
}
