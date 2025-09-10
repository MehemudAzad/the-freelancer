package com.thefreelancer.microservices.payment.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "escrow")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Escrow {
    
    @Id
    private String id; // ULID/UUID
    
    @Column(name = "milestone_id", nullable = false)
    private Long milestoneId;
    
    @Column(name = "payment_intent_id", unique = true, nullable = false)
    private String paymentIntentId;
    
    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EscrowStatus status = EscrowStatus.HELD;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum EscrowStatus {
        HELD, RELEASED, REFUNDED, DISPUTED, FAILED, CANCELED
    }
}
