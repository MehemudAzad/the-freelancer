package com.thefreelancer.microservices.payment.repository;

import com.thefreelancer.microservices.payment.model.Escrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EscrowRepository extends JpaRepository<Escrow, String> {
    
    Optional<Escrow> findByJobId(Long jobId);
    
    Optional<Escrow> findByPaymentIntentId(String paymentIntentId);
    
    List<Escrow> findByStatus(Escrow.EscrowStatus status);
    
    @Query("SELECT e FROM Escrow e WHERE e.jobId IN :jobIds")
    List<Escrow> findByJobIds(@Param("jobIds") List<Long> jobIds);
    
    boolean existsByJobId(Long jobId);
}
