package com.thefreelancer.microservices.job_proposal.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_milestones")
@Data
@EqualsAndHashCode(exclude = {"job"})
@ToString(exclude = {"job"})
public class JobMilestone {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "suggested_amount_cents")
    private Long suggestedAmountCents;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "estimated_days")
    private Integer estimatedDays;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
