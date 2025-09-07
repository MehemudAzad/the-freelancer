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
@Table(name = "profiles")
public class Profile {
    
    @Id
    @Column(name = "user_id")
    private Long userId; // This references the User.id from auth-service
    
    private String headline;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "hourly_rate_cents")
    private Long hourlyRateCents;
    
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Availability availability = Availability.OCCASIONAL;
    
    @Column(columnDefinition = "TEXT[]")
    private String[] languages;
    
    @Column(columnDefinition = "TEXT[]")
    private String[] skills;
    
    @Column(name = "location_text")
    private String locationText;
    
    @Column(name = "github_username")
    private String githubUsername;
    
    @Column(name = "gitlab_username")
    private String gitlabUsername;
    
    @Column(name = "website_url")
    private String websiteUrl;
    
    @Column(name = "linkedin_url")
    private String linkedinUrl;
    
    @Column(name = "delivery_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal deliveryScore = BigDecimal.ZERO;
    
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
    
    public enum Availability {
        FULL_TIME, PART_TIME, OCCASIONAL, UNAVAILABLE
    }
}
