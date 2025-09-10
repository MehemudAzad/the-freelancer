package com.thefreelancer.microservices.payment.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ledger")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ledger {
    
    @Id
    private String id; // ULID/UUID
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    
    @Column(name = "source_ref")
    private String sourceRef;
    
    @Column(name = "dest_ref")
    private String destRef;
    
    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Column(columnDefinition = "TEXT")
    private String meta; // JSON string for additional metadata
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public enum TransactionType {
        CHARGE, TRANSFER, REFUND, FEE, REVERSE_TRANSFER
    }
}
