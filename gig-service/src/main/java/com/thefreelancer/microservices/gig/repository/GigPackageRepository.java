package com.thefreelancer.microservices.gig.repository;

import com.thefreelancer.microservices.gig.model.GigPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GigPackageRepository extends JpaRepository<GigPackage, Long> {
    
    List<GigPackage> findByGigId(Long gigId);
    
    List<GigPackage> findByGigIdOrderByTierAsc(Long gigId);
    
    Optional<GigPackage> findByGigIdAndTier(Long gigId, GigPackage.Tier tier);
    
    @Query("SELECT COUNT(gp) FROM GigPackage gp WHERE gp.gigId = :gigId")
    long countByGigId(@Param("gigId") Long gigId);
    
    boolean existsByGigIdAndTier(Long gigId, GigPackage.Tier tier);
}
