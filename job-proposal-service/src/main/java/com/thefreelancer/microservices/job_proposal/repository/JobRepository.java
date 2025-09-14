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
           "(:skills IS NULL OR EXISTS (SELECT s FROM j.skills s WHERE s IN :skills))")
    List<Job> findOpenJobsBySkills(@Param("skills") List<String> skills);
    
    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' AND " +
           "j.minBudgetCents <= :maxBudget AND j.maxBudgetCents >= :minBudget")
    List<Job> findOpenJobsByBudgetRange(@Param("minBudget") BigInteger minBudget, 
                                        @Param("maxBudget") BigInteger maxBudget);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' AND " +
           "(:category IS NULL OR j.category = :category) AND " +
           "(:isUrgent IS NULL OR j.isUrgent = :isUrgent) AND " +
           "(:budgetType IS NULL OR j.budgetType = :budgetType) AND " +
           "(:minBudget IS NULL OR :maxBudget IS NULL OR (j.minBudgetCents <= :maxBudget AND j.maxBudgetCents >= :minBudget))")
    List<Job> findOpenJobsByFilters(@Param("category") String category,
                                    @Param("isUrgent") Boolean isUrgent,
                                    @Param("budgetType") Job.BudgetType budgetType,
                                    @Param("minBudget") BigInteger minBudget,
                                    @Param("maxBudget") BigInteger maxBudget);
    
       @Query("SELECT j FROM Job j WHERE EXISTS (SELECT s FROM j.skills s WHERE s IN :skills)")
       List<Job> findJobsBySkillsContaining(@Param("skills") List<String> skills);
    
    @Query("SELECT j FROM Job j WHERE j.minBudgetCents <= :maxBudget AND j.maxBudgetCents >= :minBudget")
    List<Job> findJobsByBudgetRange(@Param("minBudget") BigInteger minBudget, 
                                    @Param("maxBudget") BigInteger maxBudget);
    
    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN'")
    List<Job> findOpenJobs();
}
