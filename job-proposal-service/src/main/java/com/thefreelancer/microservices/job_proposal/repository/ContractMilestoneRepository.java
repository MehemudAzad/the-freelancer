package com.thefreelancer.microservices.job_proposal.repository;

import com.thefreelancer.microservices.job_proposal.model.ContractMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractMilestoneRepository extends JpaRepository<ContractMilestone, Long> {
    
    /**
     * Find milestones by contract ID
     */
    List<ContractMilestone> findByContractIdOrderByOrderIndexAsc(Long contractId);
    
    /**
     * Find milestones by contract ID and status
     */
    List<ContractMilestone> findByContractIdAndStatusOrderByOrderIndexAsc(Long contractId, ContractMilestone.MilestoneStatus status);
    
    /**
     * Find milestones by status
     */
    List<ContractMilestone> findByStatusOrderByDueDateAsc(ContractMilestone.MilestoneStatus status);
    
    /**
     * Find overdue milestones
     */
    @Query("SELECT cm FROM ContractMilestone cm WHERE cm.dueDate < :currentDate AND cm.status IN :statuses ORDER BY cm.dueDate ASC")
    List<ContractMilestone> findOverdueMilestones(@Param("currentDate") LocalDate currentDate, 
                                                  @Param("statuses") List<ContractMilestone.MilestoneStatus> statuses);
    
    /**
     * Find milestones due soon
     */
    @Query("SELECT cm FROM ContractMilestone cm WHERE cm.dueDate BETWEEN :startDate AND :endDate AND cm.status IN :statuses ORDER BY cm.dueDate ASC")
    List<ContractMilestone> findMilestonesDueSoon(@Param("startDate") LocalDate startDate, 
                                                  @Param("endDate") LocalDate endDate,
                                                  @Param("statuses") List<ContractMilestone.MilestoneStatus> statuses);
    
    /**
     * Count milestones by contract and status
     */
    long countByContractIdAndStatus(Long contractId, ContractMilestone.MilestoneStatus status);
    
    /**
     * Find next milestone in order for a contract
     */
    Optional<ContractMilestone> findFirstByContractIdAndStatusOrderByOrderIndexAsc(Long contractId, ContractMilestone.MilestoneStatus status);
    
    /**
     * Find milestones by freelancer (through contract)
     */
    @Query("SELECT cm FROM ContractMilestone cm WHERE cm.contract.freelancerId = :freelancerId ORDER BY cm.dueDate ASC")
    List<ContractMilestone> findByFreelancerIdOrderByDueDateAsc(@Param("freelancerId") Long freelancerId);
    
    /**
     * Find milestones by client (through contract)
     */
    @Query("SELECT cm FROM ContractMilestone cm WHERE cm.contract.clientId = :clientId ORDER BY cm.dueDate ASC")
    List<ContractMilestone> findByClientIdOrderByDueDateAsc(@Param("clientId") Long clientId);
    
    /**
     * Find submitted milestones for review
     */
    @Query("SELECT cm FROM ContractMilestone cm WHERE cm.contract.clientId = :clientId AND cm.status = 'SUBMITTED' ORDER BY cm.submittedAt ASC")
    List<ContractMilestone> findSubmittedMilestonesForClient(@Param("clientId") Long clientId);
}
