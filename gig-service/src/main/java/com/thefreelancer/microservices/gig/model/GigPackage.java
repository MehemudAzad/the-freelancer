package com.thefreelancer.microservices.gig.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gig_packages")
public class GigPackage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "gig_id", nullable = false)
    private Long gigId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tier tier;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "price_cents", nullable = false)
    private Long priceCents;
    
    @Column(nullable = false)
    private String currency;
    
    @Column(name = "delivery_days", nullable = false)
    private Integer deliveryDays;
    
    private Integer revisions;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum Tier {
        BASIC, STANDARD, PREMIUM
    }
}
