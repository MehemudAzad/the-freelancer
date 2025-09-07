package com.thefreelancer.microservices.gig.model;

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
@Table(name = "gig_media")
public class GigMedia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "gig_id", nullable = false)
    private Long gigId;
    
    @Column(nullable = false)
    private String url;
    
    @Column(name = "content_type")
    private String contentType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Kind kind;
    
    @Column(name = "order_index")
    private Integer orderIndex;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public enum Kind {
        IMAGE, VIDEO, DOCUMENT
    }
}
