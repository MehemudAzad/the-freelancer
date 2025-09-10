package com.thefreelancer.microservices.payment.repository;

import com.thefreelancer.microservices.payment.model.Ledger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LedgerRepository extends JpaRepository<Ledger, String> {
    
    List<Ledger> findByType(Ledger.TransactionType type);
    
    List<Ledger> findBySourceRef(String sourceRef);
    
    List<Ledger> findByDestRef(String destRef);
    
    @Query("SELECT l FROM Ledger l WHERE l.createdAt BETWEEN :startDate AND :endDate ORDER BY l.createdAt DESC")
    Page<Ledger> findTransactionsBetweenDates(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    @Query("SELECT l FROM Ledger l WHERE (l.sourceRef = :ref OR l.destRef = :ref) ORDER BY l.createdAt DESC")
    List<Ledger> findTransactionsByReference(@Param("ref") String ref);
}
