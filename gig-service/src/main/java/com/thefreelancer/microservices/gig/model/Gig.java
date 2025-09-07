package com.thefreelancer.microservices.gig.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gigs")
public class Gig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "profile_id", nullable = false)
    private Long profileId; // References Profile.userId
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.DRAFT;
    
    private String category;
    
    @Column(columnDefinition = "TEXT[]")
    private String[] tags;
    
    @Column(name = "review_avg", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal reviewAvg = BigDecimal.ZERO;
    
    @Column(name = "reviews_count")
    @Builder.Default
    private Integer reviewsCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum Status {
        DRAFT, ACTIVE, PAUSED, ARCHIVED
    }
}
