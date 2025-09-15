package com.thefreelancer.microservices.gig.controller;

import com.thefreelancer.microservices.gig.dto.BadgeCreateDto;
import com.thefreelancer.microservices.gig.dto.BadgeResponseDto;
import com.thefreelancer.microservices.gig.dto.ProfileResponseDto;
import com.thefreelancer.microservices.gig.dto.ProfileUpdateDto;
import com.thefreelancer.microservices.gig.service.BadgeService;
import com.thefreelancer.microservices.gig.service.CloudinaryService;
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
    private final CloudinaryService cloudinaryService;
    
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

    @PostMapping(value = "/profile-picture", consumes = "multipart/form-data")
    public ResponseEntity<ProfileResponseDto> uploadProfilePicture(
            @RequestPart(name = "file") org.springframework.web.multipart.MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userIdHeader == null) {
            log.warn("Authentication required for profile picture upload");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long userId;
        try {
            userId = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        log.info("POST /api/profiles/profile-picture - Uploading profile picture for userId: {}", userId);
        try {
            com.thefreelancer.microservices.gig.service.CloudinaryService.CloudinaryUploadResult uploadResult = cloudinaryService.uploadFile(file, "profile-pictures");
            Optional<ProfileResponseDto> updatedProfile = profileService.updateProfilePictureUrl(userId, uploadResult.getSecureUrl());
            if (updatedProfile.isPresent()) {
                log.info("Profile picture updated for userId: {}", userId);
                return ResponseEntity.ok(updatedProfile.get());
            } else {
                log.warn("Profile not found for userId: {}", userId);
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException | java.io.IOException e) {
            log.error("Failed to upload profile picture for userId {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    @PatchMapping("/{userId}/profile-picture")
    public ResponseEntity<ProfileResponseDto> updateProfilePictureUrl(
            @PathVariable Long userId,
            @RequestBody String profilePictureUrl) {
        log.info("PATCH /api/profiles/{}/profile-picture - Updating profile picture URL", userId);
        try {
            Optional<ProfileResponseDto> updatedProfile = profileService.updateProfilePictureUrl(userId, profilePictureUrl);
            if (updatedProfile.isPresent()) {
                log.info("Profile picture updated for userId: {}", userId);
                return ResponseEntity.ok(updatedProfile.get());
            } else {
                log.warn("Profile not found for userId: {}", userId);
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            log.error("Failed to update profile picture for userId {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/me")
    public ResponseEntity<ProfileResponseDto> updateMyProfile(
            @Valid @RequestBody ProfileUpdateDto updateDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("PUT /api/profiles/me - Updating authenticated user's profile");
        
        // Check authentication
        if (userIdHeader == null) {
            log.warn("Authentication required for updating profile");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = Long.parseLong(userIdHeader);
            log.info("Updating profile for authenticated user: {}", userId);
            
            Optional<ProfileResponseDto> updatedProfile = profileService.updateProfile(userId, updateDto);
            
            if (updatedProfile.isPresent()) {
                log.info("Profile successfully updated for userId: {}", userId);
                return ResponseEntity.ok(updatedProfile.get());
            } else {
                log.warn("Profile not found for userId: {}", userId);
                return ResponseEntity.notFound().build();
            }
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/me/badges")
    public ResponseEntity<BadgeResponseDto> addBadgeToMyProfile(
            @Valid @RequestBody BadgeCreateDto badgeCreateDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("POST /api/profiles/me/badges - Adding badge to authenticated user's profile");
        
        // Check authentication
        if (userIdHeader == null) {
            log.warn("Authentication required for adding badges");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = Long.parseLong(userIdHeader);
            log.info("Adding badge for authenticated user: {}", userId);
            
            BadgeResponseDto badge = badgeService.addBadgeToProfile(userId, badgeCreateDto);
            log.info("Badge successfully added for userId: {}", userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(badge);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.warn("Failed to add badge for userId {}: {}", userIdHeader, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/me/badges/{badgeId}")
    public ResponseEntity<Void> removeBadgeFromMyProfile(
            @PathVariable Long badgeId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("DELETE /api/profiles/me/badges/{} - Removing badge from authenticated user's profile", badgeId);
        
        // Check authentication
        if (userIdHeader == null) {
            log.warn("Authentication required for removing badges");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = Long.parseLong(userIdHeader);
            log.info("Removing badge {} for authenticated user: {}", badgeId, userId);
            
            badgeService.removeBadgeFromProfile(userId, badgeId);
            log.info("Badge successfully removed for userId: {}", userId);
            return ResponseEntity.noContent().build();
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.warn("Failed to remove badge for userId {}: {}", userIdHeader, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/me/badges")
    public ResponseEntity<List<BadgeResponseDto>> getMyBadges(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/profiles/me/badges - Fetching authenticated user's badges");
        
        // Check authentication
        if (userIdHeader == null) {
            log.warn("Authentication required for fetching user badges");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = Long.parseLong(userIdHeader);
            log.info("Fetching badges for authenticated user: {}", userId);
            
            List<BadgeResponseDto> badges = badgeService.getUserBadges(userId);
            return ResponseEntity.ok(badges);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
