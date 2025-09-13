package com.thefreelancer.microservices.gig.repository;

import com.thefreelancer.microservices.gig.model.GigMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GigMediaRepository extends JpaRepository<GigMedia, Long> {
    
    List<GigMedia> findByGigIdOrderByOrderIndexAsc(Long gigId);
    
    @Query("SELECT COALESCE(MAX(gm.orderIndex), 0) FROM GigMedia gm WHERE gm.gigId = :gigId")
    Integer findMaxOrderIndexByGigId(@Param("gigId") Long gigId);
    
    Optional<GigMedia> findByIdAndGigId(Long id, Long gigId);
    
    void deleteByIdAndGigId(Long id, Long gigId);
    
    long countByGigId(Long gigId);
}
