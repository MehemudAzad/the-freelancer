package com.thefreelancer.microservices.job_proposal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_milestones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "amount_cents", nullable = false)
    private BigInteger amountCents;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MilestoneStatus status = MilestoneStatus.PENDING;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum MilestoneStatus {
        PENDING,           // Initial state after contract creation
        FUNDING_REQUIRED,  // Client needs to fund this milestone
        FUNDED,           // Escrow funded, ready for work
        IN_PROGRESS,      // Freelancer is working on it
        SUBMITTED,        // Freelancer submitted deliverable
        ACCEPTED,         // Client accepted the work
        REJECTED,         // Client rejected the work
        DISPUTED,         // In dispute resolution
        PAID             // Payment released to freelancer
    }
}
