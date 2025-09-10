package com.thefreelancer.microservices.payment.repository;

import com.thefreelancer.microservices.payment.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, String> {
    
    Optional<Refund> findByRefundId(String refundId);
    
    List<Refund> findByEscrowId(String escrowId);
    
    List<Refund> findByStatus(Refund.RefundStatus status);
}
