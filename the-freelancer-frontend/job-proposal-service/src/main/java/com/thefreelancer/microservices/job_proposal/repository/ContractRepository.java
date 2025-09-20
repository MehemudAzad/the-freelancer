package com.thefreelancer.microservices.job_proposal.repository;

import com.thefreelancer.microservices.job_proposal.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    
    /**
     * Find contract by job ID
     */
    Optional<Contract> findByJobId(Long jobId);
    
    /**
     * Find contract by proposal ID
     */
    Optional<Contract> findByProposalId(Long proposalId);
    
    /**
     * Find contracts by client ID
     */
    List<Contract> findByClientIdOrderByCreatedAtDesc(Long clientId);
    
    /**
     * Find contracts by freelancer ID
     */
    List<Contract> findByFreelancerIdOrderByCreatedAtDesc(Long freelancerId);
    
    /**
     * Find contracts by client ID and status
     */
    List<Contract> findByClientIdAndStatusOrderByCreatedAtDesc(Long clientId, Contract.ContractStatus status);
    
    /**
     * Find contracts by freelancer ID and status
     */
    List<Contract> findByFreelancerIdAndStatusOrderByCreatedAtDesc(Long freelancerId, Contract.ContractStatus status);
    
    /**
     * Find contracts for a user (either as client or freelancer)
     */
    @Query("SELECT c FROM Contract c WHERE c.clientId = :userId OR c.freelancerId = :userId ORDER BY c.createdAt DESC")
    List<Contract> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    /**
     * Find contracts for a user by status
     */
    @Query("SELECT c FROM Contract c WHERE (c.clientId = :userId OR c.freelancerId = :userId) AND c.status = :status ORDER BY c.createdAt DESC")
    List<Contract> findByUserIdAndStatusOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("status") Contract.ContractStatus status);
    
    /**
     * Check if a contract exists for a job
     */
    boolean existsByJobId(Long jobId);
    
    /**
     * Check if a contract exists for a proposal
     */
    boolean existsByProposalId(Long proposalId);
}
