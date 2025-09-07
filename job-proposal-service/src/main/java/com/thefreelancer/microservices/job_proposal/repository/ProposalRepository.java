package com.thefreelancer.microservices.job_proposal.repository;

import com.thefreelancer.microservices.job_proposal.model.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    
    List<Proposal> findByJobId(Long jobId);
    
    List<Proposal> findByFreelancerId(Long freelancerId);
    
    List<Proposal> findByJobIdAndStatus(Long jobId, Proposal.ProposalStatus status);
    
    List<Proposal> findByFreelancerIdAndStatus(Long freelancerId, Proposal.ProposalStatus status);
    
    boolean existsByJobIdAndFreelancerId(Long jobId, Long freelancerId);
}
