package com.thefreelancer.microservices.job_proposal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "client_id", nullable = false)
    private Long clientId;
    
    @Column(name = "project_name", nullable = false)
    private String projectName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    // stack replaced by skills - use `skills` field
    
    @Enumerated(EnumType.STRING)
    @Column(name = "budget_type")
    private BudgetType budgetType;
    
    @Column(name = "min_budget_cents")
    private BigInteger minBudgetCents;
    
    @Column(name = "max_budget_cents")
    private BigInteger maxBudgetCents;
    
    @Column(name = "nda_required")
    @Builder.Default
    private Boolean ndaRequired = false;
    
    @Column(name = "ip_assignment")
    @Builder.Default
    private Boolean ipAssignment = true;
    
    @Column
    private String category;

    @ElementCollection
    @CollectionTable(name = "job_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill")
    private java.util.List<String> skills;



    @Column(name = "is_urgent")
    @Builder.Default
    private Boolean isUrgent = false;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private JobStatus status = JobStatus.OPEN;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;
    
    // Relationships
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JobAttachment> attachments;
    
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Proposal> proposals;
    
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Invite> invites;
    
    public enum BudgetType {
        FIXED, HOURLY
    }
    
    public enum JobStatus {
        DRAFT, OPEN, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
