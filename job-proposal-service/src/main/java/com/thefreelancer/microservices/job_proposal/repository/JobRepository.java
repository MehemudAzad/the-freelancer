package com.thefreelancer.microservices.job_proposal.repository;

import com.thefreelancer.microservices.job_proposal.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    List<Job> findByClientId(Long clientId);
    
    List<Job> findByStatus(Job.JobStatus status);
    
    List<Job> findByClientIdAndStatus(Long clientId, Job.JobStatus status);
    
    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' AND " +
           "(:stack IS NULL OR EXISTS (SELECT s FROM j.stack s WHERE s IN :stack))")
    List<Job> findOpenJobsByStack(@Param("stack") List<String> stack);
    
    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' AND " +
           "j.minBudgetCents <= :maxBudget AND j.maxBudgetCents >= :minBudget")
    List<Job> findOpenJobsByBudgetRange(@Param("minBudget") BigInteger minBudget, 
                                        @Param("maxBudget") BigInteger maxBudget);
    
    @Query("SELECT j FROM Job j WHERE EXISTS (SELECT s FROM j.stack s WHERE s IN :stack)")
    List<Job> findJobsByStackContaining(@Param("stack") List<String> stack);
    
    @Query("SELECT j FROM Job j WHERE j.minBudgetCents <= :maxBudget AND j.maxBudgetCents >= :minBudget")
    List<Job> findJobsByBudgetRange(@Param("minBudget") BigInteger minBudget, 
                                    @Param("maxBudget") BigInteger maxBudget);
    
    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN'")
    List<Job> findOpenJobs();
}
