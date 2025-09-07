package com.thefreelancer.microservices.job_proposal.repository;

import com.thefreelancer.microservices.job_proposal.model.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InviteRepository extends JpaRepository<Invite, Long> {
    
    List<Invite> findByJobId(Long jobId);
    
    List<Invite> findByFreelancerId(Long freelancerId);
    
    List<Invite> findByClientId(Long clientId);
    
    List<Invite> findByFreelancerIdAndStatus(Long freelancerId, Invite.InviteStatus status);
    
    boolean existsByJobIdAndFreelancerId(Long jobId, Long freelancerId);
}
