package com.thefreelancer.microservices.payment.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refund {
    
    @Id
    private String id; // ULID/UUID
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escrow_id", nullable = false)
    private Escrow escrow;
    
    @Column(name = "refund_id", unique = true, nullable = false)
    private String refundId;
    
    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RefundStatus status = RefundStatus.INITIATED;
    
    @Column(length = 500)
    private String reason;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public enum RefundStatus {
        INITIATED, SUCCEEDED, FAILED
    }
}
