package com.thefreelancer.microservices.job_proposal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobAttachment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;
    
    @Enumerated(EnumType.STRING)
    private AttachmentKind kind;
    
    @Column(nullable = false)
    private String url;
    
    private String filename;
    
    private BigInteger bytes;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "cloudinary_public_id")
    private String cloudinaryPublicId;

    @Column(name = "cloudinary_resource_type")
    private String cloudinaryResourceType;

    private String checksum;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public enum AttachmentKind {
        SPEC, WIREFRAME, DATASET, OTHER
    }
}
