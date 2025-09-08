package com.thefreelancer.microservices.job_proposal.repository;

import com.thefreelancer.microservices.job_proposal.model.JobAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobAttachmentRepository extends JpaRepository<JobAttachment, Long> {
    
    List<JobAttachment> findByJobId(Long jobId);
    
    List<JobAttachment> findByJobIdAndKind(Long jobId, JobAttachment.AttachmentKind kind);
    
    @Query("SELECT ja FROM JobAttachment ja WHERE ja.job.id = :jobId")
    List<JobAttachment> findAttachmentsByJobId(@Param("jobId") Long jobId);
}
