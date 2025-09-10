package com.thefreelancer.microservices.payment.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

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
    private LedgerType type;
    
    @Column(name = "source_ref")
    private String sourceRef;
    
    @Column(name = "dest_ref")
    private String destRef;
    
    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @ElementCollection
    @CollectionTable(name = "ledger_meta", joinColumns = @JoinColumn(name = "ledger_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value")
    private Map<String, String> meta;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public enum LedgerType {
        CHARGE, TRANSFER, REFUND, FEE, REVERSE_TRANSFER, DISPUTE
    }
}
