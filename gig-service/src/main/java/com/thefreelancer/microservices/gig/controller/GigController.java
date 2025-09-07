package com.thefreelancer.microservices.gig.controller;

import com.thefreelancer.microservices.gig.dto.GigCreateDto;
import com.thefreelancer.microservices.gig.dto.GigResponseDto;
import com.thefreelancer.microservices.gig.dto.GigUpdateDto;
import com.thefreelancer.microservices.gig.service.GigService;
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
public class GigController {
    
    private final GigService gigService;
    
    @PostMapping
    public ResponseEntity<GigResponseDto> createGig(
            @RequestParam Long userId,
            @Valid @RequestBody GigCreateDto gigCreateDto) {
        
        log.info("POST /api/gigs - Creating gig for userId: {}", userId);
        
        try {
            GigResponseDto gig = gigService.createGig(userId, gigCreateDto);
            log.info("Gig successfully created with ID: {} for userId: {}", gig.getId(), userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(gig);
        } catch (RuntimeException e) {
            log.warn("Failed to create gig for userId {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{gigId}")
    public ResponseEntity<GigResponseDto> getGig(@PathVariable Long gigId) {
        log.info("GET /api/gigs/{} - Fetching gig", gigId);
        
        Optional<GigResponseDto> gig = gigService.getGigById(gigId);
        
        if (gig.isPresent()) {
            log.info("Gig found with ID: {}", gigId);
            return ResponseEntity.ok(gig.get());
        } else {
            log.warn("Gig not found with ID: {}", gigId);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GigResponseDto>> getUserGigs(@PathVariable Long userId) {
        log.info("GET /api/gigs/user/{} - Fetching gigs for user", userId);
        
        List<GigResponseDto> gigs = gigService.getGigsByUserId(userId);
        return ResponseEntity.ok(gigs);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<GigResponseDto>> searchGigs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> tags) {
        
        log.info("GET /api/gigs/search - Searching gigs with category: {}, tags: {}", category, tags);
        
        List<GigResponseDto> gigs;
        
        if (category != null && !category.trim().isEmpty()) {
            gigs = gigService.getGigsByCategory(category);
        } else if (tags != null && !tags.isEmpty()) {
            gigs = gigService.searchGigsByTags(tags);
        } else {
            gigs = gigService.getActiveGigs();
        }
        
        return ResponseEntity.ok(gigs);
    }
    
    @PutMapping("/{gigId}")
    public ResponseEntity<GigResponseDto> updateGig(
            @PathVariable Long gigId,
            @Valid @RequestBody GigUpdateDto gigUpdateDto) {
        
        log.info("PUT /api/gigs/{} - Updating gig", gigId);
        
        try {
            Optional<GigResponseDto> updatedGig = gigService.updateGig(gigId, gigUpdateDto);
            
            if (updatedGig.isPresent()) {
                log.info("Gig successfully updated with ID: {}", gigId);
                return ResponseEntity.ok(updatedGig.get());
            } else {
                log.warn("Gig not found with ID: {}", gigId);
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            log.warn("Failed to update gig {}: {}", gigId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{gigId}")
    public ResponseEntity<Void> deleteGig(@PathVariable Long gigId) {
        log.info("DELETE /api/gigs/{} - Deleting gig", gigId);
        
        boolean deleted = gigService.deleteGig(gigId);
        
        if (deleted) {
            log.info("Gig successfully deleted with ID: {}", gigId);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Gig not found with ID: {}", gigId);
            return ResponseEntity.notFound().build();
        }
    }
}
