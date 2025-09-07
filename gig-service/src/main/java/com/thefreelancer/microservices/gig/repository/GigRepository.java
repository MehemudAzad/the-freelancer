package com.thefreelancer.microservices.gig.repository;

import com.thefreelancer.microservices.gig.model.Gig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GigRepository extends JpaRepository<Gig, Long> {
    
    List<Gig> findByProfileId(Long profileId);
    
    List<Gig> findByStatus(Gig.Status status);
    
    List<Gig> findByCategory(String category);
    
    @Query("SELECT g FROM Gig g WHERE g.status = 'ACTIVE' AND " +
           "(:category IS NULL OR g.category = :category)")
    List<Gig> findActiveGigsByCategory(@Param("category") String category);
    
    @Query("SELECT g FROM Gig g WHERE g.status = 'ACTIVE' AND " +
           "EXISTS (SELECT t FROM g.tags t WHERE t IN :tags)")
    List<Gig> findActiveGigsByTags(@Param("tags") List<String> tags);
}
