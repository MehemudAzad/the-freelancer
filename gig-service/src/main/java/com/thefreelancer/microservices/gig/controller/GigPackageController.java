package com.thefreelancer.microservices.gig.controller;

import com.thefreelancer.microservices.gig.dto.GigPackageCreateDto;
import com.thefreelancer.microservices.gig.dto.GigPackageResponseDto;
import com.thefreelancer.microservices.gig.dto.GigPackageUpdateDto;
import com.thefreelancer.microservices.gig.service.GigPackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/gigs")
@RequiredArgsConstructor
@Slf4j
public class GigPackageController {
    
    private final GigPackageService gigPackageService;
    
    @PostMapping("/{gigId}/packages")
    public ResponseEntity<GigPackageResponseDto> createGigPackage(
            @PathVariable Long gigId,
            @Valid @RequestBody GigPackageCreateDto createDto) {
        
        log.info("POST /api/gigs/{}/packages - Creating package with tier: {}", gigId, createDto.getTier());
        
        try {
            GigPackageResponseDto packageDto = gigPackageService.createGigPackage(gigId, createDto);
            log.info("Package successfully created with ID: {} for gigId: {}", packageDto.getId(), gigId);
            return ResponseEntity.status(HttpStatus.CREATED).body(packageDto);
        } catch (RuntimeException e) {
            log.warn("Failed to create package for gigId {}: {}", gigId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{gigId}/packages")
    public ResponseEntity<List<GigPackageResponseDto>> getGigPackages(@PathVariable Long gigId) {
        log.info("GET /api/gigs/{}/packages - Fetching packages", gigId);
        
        List<GigPackageResponseDto> packages = gigPackageService.getGigPackages(gigId);
        return ResponseEntity.ok(packages);
    }
    
    @PutMapping("/{gigId}/packages/{packageId}")
    public ResponseEntity<GigPackageResponseDto> updateGigPackage(
            @PathVariable Long gigId,
            @PathVariable Long packageId,
            @Valid @RequestBody GigPackageUpdateDto updateDto) {
        
        log.info("PUT /api/gigs/{}/packages/{} - Updating package", gigId, packageId);
        
        try {
            Optional<GigPackageResponseDto> updatedPackage = gigPackageService.updateGigPackage(gigId, packageId, updateDto);
            
            if (updatedPackage.isPresent()) {
                log.info("Package successfully updated with ID: {}", packageId);
                return ResponseEntity.ok(updatedPackage.get());
            } else {
                log.warn("Package not found with ID: {}", packageId);
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            log.warn("Failed to update package {}: {}", packageId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{gigId}/packages/{packageId}")
    public ResponseEntity<Void> deleteGigPackage(
            @PathVariable Long gigId,
            @PathVariable Long packageId) {
        
        log.info("DELETE /api/gigs/{}/packages/{} - Deleting package", gigId, packageId);
        
        try {
            boolean deleted = gigPackageService.deleteGigPackage(gigId, packageId);
            
            if (deleted) {
                log.info("Package successfully deleted with ID: {}", packageId);
                return ResponseEntity.noContent().build();
            } else {
                log.warn("Package not found with ID: {}", packageId);
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            log.warn("Failed to delete package {}: {}", packageId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
