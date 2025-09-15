package com.thefreelancer.microservices.payment.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payouts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payout {
    
    @Id
    private String id; // ULID/UUID
    
    @Column(name = "job_id", nullable = false)
    private Long jobId;
    
    @Column(name = "transfer_id", unique = true, nullable = false)
    private String transferId;
    
    @Column(name = "destination_account_id", nullable = false)
    private String destinationAccountId;
    
    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;
    
    @Column(name = "fee_cents", nullable = false)
    private Long feeCents;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PayoutStatus status = PayoutStatus.INITIATED;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum PayoutStatus {
        INITIATED, PAID, FAILED, REVERSED
    }
}
