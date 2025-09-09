package com.thefreelancer.microservices.job_proposal.repository;

import com.thefreelancer.microservices.job_proposal.model.JobMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobMilestoneRepository extends JpaRepository<JobMilestone, Long> {
    
    @Query("SELECT jm FROM JobMilestone jm WHERE jm.job.id = :jobId ORDER BY jm.orderIndex ASC")
    List<JobMilestone> findByJobIdOrderByOrderIndex(@Param("jobId") Long jobId);
    
    @Query("SELECT jm FROM JobMilestone jm WHERE jm.job.id = :jobId AND jm.id = :milestoneId")
    Optional<JobMilestone> findByJobIdAndId(@Param("jobId") Long jobId, @Param("milestoneId") Long milestoneId);
    
    @Modifying
    @Query("DELETE FROM JobMilestone jm WHERE jm.job.id = :jobId")
    void deleteByJobId(@Param("jobId") Long jobId);
    
    @Query("SELECT COUNT(jm) FROM JobMilestone jm WHERE jm.job.id = :jobId")
    long countByJobId(@Param("jobId") Long jobId);
    
    @Query("SELECT CASE WHEN COUNT(jm) > 0 THEN true ELSE false END FROM JobMilestone jm WHERE jm.job.id = :jobId AND jm.orderIndex = :orderIndex")
    boolean existsByJobIdAndOrderIndex(@Param("jobId") Long jobId, @Param("orderIndex") Integer orderIndex);
}
