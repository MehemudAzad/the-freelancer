package com.thefreelancer.microservices.gig.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profile_badges")
public class ProfileBadge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String type;
    
    @Column(precision = 4, scale = 1)
    private BigDecimal score;
    
    @Column(name = "issued_at")
    private LocalDateTime issuedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
