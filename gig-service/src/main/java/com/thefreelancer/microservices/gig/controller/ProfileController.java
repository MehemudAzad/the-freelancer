package com.thefreelancer.microservices.gig.controller;

import com.thefreelancer.microservices.gig.dto.BadgeCreateDto;
import com.thefreelancer.microservices.gig.dto.BadgeResponseDto;
import com.thefreelancer.microservices.gig.dto.ProfileResponseDto;
import com.thefreelancer.microservices.gig.dto.ProfileUpdateDto;
import com.thefreelancer.microservices.gig.service.BadgeService;
import com.thefreelancer.microservices.gig.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    
    private final ProfileService profileService;
    private final BadgeService badgeService;
    
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponseDto> getProfile(@PathVariable Long userId) {
        log.info("GET /api/profiles/{} - Fetching profile", userId);
        
        Optional<ProfileResponseDto> profile = profileService.getProfileByUserId(userId);
        
        if (profile.isPresent()) {
            log.info("Profile found for userId: {}", userId);
            return ResponseEntity.ok(profile.get());
        } else {
            log.warn("Profile not found for userId: {}", userId);
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{userId}")
    public ResponseEntity<ProfileResponseDto> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody ProfileUpdateDto updateDto) {
        
        log.info("PUT /api/profiles/{} - Updating profile", userId);
        
        Optional<ProfileResponseDto> updatedProfile = profileService.updateProfile(userId, updateDto);
        
        if (updatedProfile.isPresent()) {
            log.info("Profile successfully updated for userId: {}", userId);
            return ResponseEntity.ok(updatedProfile.get());
        } else {
            log.warn("Profile not found for userId: {}", userId);
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{userId}/badges")
    public ResponseEntity<BadgeResponseDto> addBadge(
            @PathVariable Long userId,
            @Valid @RequestBody BadgeCreateDto badgeCreateDto) {
        
        log.info("POST /api/profiles/{}/badges - Adding badge", userId);
        
        try {
            BadgeResponseDto badge = badgeService.addBadgeToProfile(userId, badgeCreateDto);
            log.info("Badge successfully added for userId: {}", userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(badge);
        } catch (RuntimeException e) {
            log.warn("Failed to add badge for userId {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{userId}/badges/{badgeId}")
    public ResponseEntity<Void> removeBadge(
            @PathVariable Long userId,
            @PathVariable Long badgeId) {
        
        log.info("DELETE /api/profiles/{}/badges/{} - Removing badge", userId, badgeId);
        
        try {
            badgeService.removeBadgeFromProfile(userId, badgeId);
            log.info("Badge successfully removed for userId: {}", userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.warn("Failed to remove badge for userId {}: {}", userId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{userId}/badges")
    public ResponseEntity<List<BadgeResponseDto>> getUserBadges(@PathVariable Long userId) {
        log.info("GET /api/profiles/{}/badges - Fetching user badges", userId);
        
        List<BadgeResponseDto> badges = badgeService.getUserBadges(userId);
        return ResponseEntity.ok(badges);
    }
}
