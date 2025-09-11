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
    
    // ========== PUBLIC READ-ONLY ENDPOINT ==========
    
    @GetMapping("/{gigId}/packages")
    public ResponseEntity<List<GigPackageResponseDto>> getGigPackages(@PathVariable Long gigId) {
        log.info("GET /api/gigs/{}/packages - Fetching packages", gigId);
        
        List<GigPackageResponseDto> packages = gigPackageService.getGigPackages(gigId);
        return ResponseEntity.ok(packages);
    }

    // ========== SECURE ENDPOINTS FOR PACKAGE MANAGEMENT ==========
    
    @PostMapping("/my-gigs/{gigId}/packages")
    public ResponseEntity<GigPackageResponseDto> createMyGigPackage(
            @PathVariable Long gigId,
            @Valid @RequestBody GigPackageCreateDto createDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("POST /api/gigs/my-gigs/{}/packages - Creating package for authenticated user's gig", gigId);
        
        // Check authentication
        if (userIdHeader == null) {
            log.warn("Authentication required for creating packages");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = Long.parseLong(userIdHeader);
            log.info("Creating package for gig {} owned by user: {}", gigId, userId);
            
            // The service should validate that the user owns this gig
            // For now, we'll use the existing method - TODO: Add ownership validation in service
            GigPackageResponseDto packageDto = gigPackageService.createGigPackage(gigId, createDto);
            log.info("Package successfully created with ID: {} for gigId: {} owned by userId: {}", packageDto.getId(), gigId, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(packageDto);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.warn("Failed to create package for gigId {}: {}", gigId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/my-gigs/{gigId}/packages/{packageId}")
    public ResponseEntity<GigPackageResponseDto> updateMyGigPackage(
            @PathVariable Long gigId,
            @PathVariable Long packageId,
            @Valid @RequestBody GigPackageUpdateDto updateDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("PUT /api/gigs/my-gigs/{}/packages/{} - Updating package for authenticated user's gig", gigId, packageId);
        
        // Check authentication
        if (userIdHeader == null) {
            log.warn("Authentication required for updating packages");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = Long.parseLong(userIdHeader);
            log.info("Updating package {} for gig {} owned by user: {}", packageId, gigId, userId);
            
            // The service should validate that the user owns this gig
            // For now, we'll use the existing method - 
            // TODO: Add ownership validation in service
            Optional<GigPackageResponseDto> updatedPackage = gigPackageService.updateGigPackage(gigId, packageId, updateDto);
            
            if (updatedPackage.isPresent()) {
                log.info("Package successfully updated with ID: {} for gigId: {} owned by userId: {}", packageId, gigId, userId);
                return ResponseEntity.ok(updatedPackage.get());
            } else {
                log.warn("Package not found or access denied for packageId: {} gigId: {} userId: {}", packageId, gigId, userId);
                return ResponseEntity.notFound().build();
            }
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.warn("Failed to update package {}: {}", packageId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/my-gigs/{gigId}/packages/{packageId}")
    public ResponseEntity<Void> deleteMyGigPackage(
            @PathVariable Long gigId,
            @PathVariable Long packageId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("DELETE /api/gigs/my-gigs/{}/packages/{} - Deleting package from authenticated user's gig", gigId, packageId);
        
        // Check authentication
        if (userIdHeader == null) {
            log.warn("Authentication required for deleting packages");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = Long.parseLong(userIdHeader);
            log.info("Deleting package {} for gig {} owned by user: {}", packageId, gigId, userId);
            
            // The service should validate that the user owns this gig
            // For now, we'll use the existing method - 
            // TODO: Add ownership validation in service
            boolean deleted = gigPackageService.deleteGigPackage(gigId, packageId);
            
            if (deleted) {
                log.info("Package successfully deleted with ID: {} for gigId: {} owned by userId: {}", packageId, gigId, userId);
                return ResponseEntity.noContent().build();
            } else {
                log.warn("Package not found or access denied for packageId: {} gigId: {} userId: {}", packageId, gigId, userId);
                return ResponseEntity.notFound().build();
            }
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.warn("Failed to delete package {}: {}", packageId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
