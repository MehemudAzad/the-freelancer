package com.thefreelancer.microservices.workspace_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workspace_files") // Use the more descriptive name
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "uploader_id", nullable = false)
    private Long uploaderId;

    @Column(nullable = false)
    private String filename;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String url;
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    @Column(name = "cloudinary_public_id")
    private String cloudinaryPublicId;
    
    @Column(name = "cloudinary_resource_type")
    private String cloudinaryResourceType;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "milestone_id")
    private Long milestoneId; // Optional: link to specific contract milestone

    private String checksum;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
