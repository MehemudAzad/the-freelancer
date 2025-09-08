package com.thefreelancer.microservices.job_proposal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "proposal_milestones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProposalMilestone {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "proposal_id", nullable = false)
    private Long proposalId;
    
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @Column(name = "due_date")
    private LocalDate dueDate;
    
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
    
    @Column(name = "dod", columnDefinition = "TEXT")
    private String dod; // Definition of Done - stored as JSON string
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Foreign key constraint will be handled at database level
    // Note: Proposal relationship removed to avoid circular dependency
}
