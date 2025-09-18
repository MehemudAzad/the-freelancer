package com.thefreelancer.microservices.payment.repository;

import com.thefreelancer.microservices.payment.model.Payout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, String> {
    
    Optional<Payout> findByJobId(Long jobId);
    
    Optional<Payout> findByTransferId(String transferId);
    
    List<Payout> findByStatus(Payout.PayoutStatus status);
    
    List<Payout> findByDestinationAccountId(String destinationAccountId);
}
