package com.thefreelancer.microservices.job_proposal.repository;

import com.thefreelancer.microservices.job_proposal.model.ProposalMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposalMilestoneRepository extends JpaRepository<ProposalMilestone, Long> {
    
    /**
     * Find all milestones for a specific proposal, ordered by order index
     */
    List<ProposalMilestone> findByProposalIdOrderByOrderIndexAsc(Long proposalId);
    
    /**
     * Find milestones by proposal ID
     */
    List<ProposalMilestone> findByProposalId(Long proposalId);
    
    /**
     * Count milestones for a proposal
     */
    long countByProposalId(Long proposalId);
    
    /**
     * Check if proposal has any milestones
     */
    boolean existsByProposalId(Long proposalId);
    
    /**
     * Delete all milestones for a proposal
     */
    void deleteByProposalId(Long proposalId);
}
