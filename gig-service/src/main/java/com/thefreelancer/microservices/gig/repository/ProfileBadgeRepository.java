package com.thefreelancer.microservices.gig.repository;

import com.thefreelancer.microservices.gig.model.ProfileBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileBadgeRepository extends JpaRepository<ProfileBadge, Long> {
    
    List<ProfileBadge> findByUserId(Long userId);
    
    void deleteByIdAndUserId(Long id, Long userId);
    
    boolean existsByUserIdAndType(Long userId, String type);
}
