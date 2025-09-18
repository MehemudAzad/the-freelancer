package com.thefreelancer.microservices.auth.controller;

import com.thefreelancer.microservices.auth.client.GigServiceClient;
import com.thefreelancer.microservices.auth.dto.ProfileResponseDto;
import com.thefreelancer.microservices.auth.dto.UserProfileDto;
import com.thefreelancer.microservices.auth.dto.UserResponseDto;
import com.thefreelancer.microservices.auth.dto.UserWithProfileResponseDto;
import com.thefreelancer.microservices.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth/public/users")
@RequiredArgsConstructor
@Slf4j
public class PublicUserController {

    private final UserService userService;
    private final GigServiceClient gigServiceClient;

    @GetMapping("/username/{handle}")
    public ResponseEntity<UserResponseDto> getUserByUsername(@PathVariable String handle) {
        log.debug("Getting user by username: {}", handle);

        Optional<UserResponseDto> user = userService.getUserByHandle(handle);

        if (user.isPresent()) {
            log.info("Successfully found user by username: {}", handle);
            return ResponseEntity.ok(user.get());
        } else {
            log.warn("User not found with username: {}", handle);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Search users by handle prefix (case-insensitive) with paging.
     * Example: GET /api/auth/public/users/search?handle=absk&page=0&size=20
     */
    @GetMapping("/search")
    public ResponseEntity<Page<UserResponseDto>> searchUsersByHandle(
            @RequestParam(name = "handle") String handle,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        log.debug("Searching users by handle prefix='{}' page={} size={}", handle, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponseDto> results = userService.searchUsersByHandle(handle, pageable);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserWithProfileResponseDto> getUserById(@PathVariable Long id) {
        Optional<UserWithProfileResponseDto> user = userService.getUserWithProfileById(id);
        return user.map(u -> ResponseEntity.ok(u))
                   .orElse(ResponseEntity.notFound().build());
    }
    /**
     * Get full user profile by username (public endpoint)
     * Similar to /me/profile but accessible by username without authentication
     */
    @GetMapping("/username/{handle}/profile")
    public ResponseEntity<UserProfileDto> getUserProfileByUsername(@PathVariable String handle) {
        log.debug("Getting user profile by username: {}", handle);

        // First, get the user by handle
        Optional<UserResponseDto> userOpt = userService.getUserByHandle(handle);
        if (userOpt.isEmpty()) {
            log.warn("User not found with username: {}", handle);
            return ResponseEntity.notFound().build();
        }

        UserResponseDto user = userOpt.get();
        Long userId = user.getId();
        
        // Build the response with user data from auth service
        UserProfileDto.UserProfileDtoBuilder responseBuilder = UserProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().toString())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt());

        // Try to get profile data from gig service
        try {
            Optional<ProfileResponseDto> profileOpt = gigServiceClient.getProfileByUserId(userId);
            if (profileOpt.isPresent()) {
                ProfileResponseDto profile = profileOpt.get();
                log.debug("Found profile for user: {}", userId);
                
                UserProfileDto.ProfileData profileData = UserProfileDto.ProfileData.builder()
                    .headline(profile.getHeadline())
                    .bio(profile.getBio())
                    .hourlyRateCents(profile.getHourlyRateCents())
                    .currency(profile.getCurrency())
                    .availability(profile.getAvailability())
                    .languages(profile.getLanguages())
                    .skills(profile.getSkills())
                    .locationText(profile.getLocationText())
                    .githubUsername(profile.getGithubUsername())
                    .gitlabUsername(profile.getGitlabUsername())
                    .websiteUrl(profile.getWebsiteUrl())
                    .linkedinUrl(profile.getLinkedinUrl())
                    .profilePictureUrl(profile.getProfilePictureUrl())
                    .deliveryScore(profile.getDeliveryScore())
                    .reviewAvg(profile.getReviewAvg())
                    .reviewsCount(profile.getReviewsCount())
                    .createdAt(profile.getCreatedAt())
                    .updatedAt(profile.getUpdatedAt())
                    .build();
                
                responseBuilder.profile(profileData);
            } else {
                log.debug("No profile found for user: {}", userId);
                // profile will be null, which is fine
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve profile from gig service for user: {}", userId, e);
            // Continue without profile data
        }
        
        UserProfileDto response = responseBuilder.build();
        log.info("Successfully retrieved public user profile for username: {}", handle);
        
        return ResponseEntity.ok(response);
    }
}
