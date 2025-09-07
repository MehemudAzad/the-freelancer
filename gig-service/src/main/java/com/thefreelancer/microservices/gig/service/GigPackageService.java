package com.thefreelancer.microservices.gig.service;

import com.thefreelancer.microservices.gig.dto.GigPackageCreateDto;
import com.thefreelancer.microservices.gig.dto.GigPackageResponseDto;
import com.thefreelancer.microservices.gig.dto.GigPackageUpdateDto;
import com.thefreelancer.microservices.gig.mapper.GigPackageMapper;
import com.thefreelancer.microservices.gig.model.GigPackage;
import com.thefreelancer.microservices.gig.repository.GigPackageRepository;
import com.thefreelancer.microservices.gig.repository.GigRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GigPackageService {
    
    private final GigPackageRepository gigPackageRepository;
    private final GigRepository gigRepository;
    private final GigPackageMapper gigPackageMapper;
    
    @Transactional
    public GigPackageResponseDto createGigPackage(Long gigId, GigPackageCreateDto createDto) {
        log.info("Creating package for gigId: {} with tier: {}", gigId, createDto.getTier());
        
        // Check if gig exists
        if (!gigRepository.existsById(gigId)) {
            throw new RuntimeException("Gig not found with ID: " + gigId);
        }
        
        // Check if package with this tier already exists for this gig
        if (gigPackageRepository.existsByGigIdAndTier(gigId, createDto.getTier())) {
            throw new RuntimeException("Package with tier " + createDto.getTier() + " already exists for gig: " + gigId);
        }
        
        GigPackage gigPackage = gigPackageMapper.toEntity(createDto);
        gigPackage.setGigId(gigId);
        
        GigPackage savedPackage = gigPackageRepository.save(gigPackage);
        log.info("Successfully created package with ID: {} for gigId: {}", savedPackage.getId(), gigId);
        
        return gigPackageMapper.toResponseDto(savedPackage);
    }
    
    public List<GigPackageResponseDto> getGigPackages(Long gigId) {
        log.info("Fetching packages for gigId: {}", gigId);
        
        return gigPackageRepository.findByGigIdOrderByTierAsc(gigId)
                .stream()
                .map(gigPackageMapper::toResponseDto)
                .toList();
    }
    
    @Transactional
    public Optional<GigPackageResponseDto> updateGigPackage(Long gigId, Long packageId, GigPackageUpdateDto updateDto) {
        log.info("Updating package with ID: {} for gigId: {}", packageId, gigId);
        
        Optional<GigPackage> packageOpt = gigPackageRepository.findById(packageId);
        
        if (packageOpt.isEmpty()) {
            log.warn("Package not found with ID: {}", packageId);
            return Optional.empty();
        }
        
        GigPackage gigPackage = packageOpt.get();
        
        // Verify package belongs to the gig
        if (!gigPackage.getGigId().equals(gigId)) {
            throw new RuntimeException("Package " + packageId + " does not belong to gig: " + gigId);
        }
        
        gigPackageMapper.updateEntityFromDto(updateDto, gigPackage);
        
        GigPackage updatedPackage = gigPackageRepository.save(gigPackage);
        log.info("Successfully updated package with ID: {}", packageId);
        
        return Optional.of(gigPackageMapper.toResponseDto(updatedPackage));
    }
    
    @Transactional
    public boolean deleteGigPackage(Long gigId, Long packageId) {
        log.info("Deleting package with ID: {} for gigId: {}", packageId, gigId);
        
        Optional<GigPackage> packageOpt = gigPackageRepository.findById(packageId);
        
        if (packageOpt.isEmpty()) {
            log.warn("Package not found with ID: {}", packageId);
            return false;
        }
        
        GigPackage gigPackage = packageOpt.get();
        
        // Verify package belongs to the gig
        if (!gigPackage.getGigId().equals(gigId)) {
            throw new RuntimeException("Package " + packageId + " does not belong to gig: " + gigId);
        }
        
        gigPackageRepository.deleteById(packageId);
        log.info("Successfully deleted package with ID: {}", packageId);
        return true;
    }
}
