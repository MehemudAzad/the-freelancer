package com.thefreelancer.microservices.auth.controller;

import com.thefreelancer.microservices.auth.client.GigServiceClient;
import com.thefreelancer.microservices.auth.dto.ProfileResponseDto;
import com.thefreelancer.microservices.auth.dto.UserProfileDto;
import com.thefreelancer.microservices.auth.dto.UserResponseDto;
import com.thefreelancer.microservices.auth.service.UserService;
import com.thefreelancer.microservices.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("/api/auth/me")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final GigServiceClient gigServiceClient;

    @GetMapping
    public ResponseEntity<UserResponseDto> getCurrentUser(HttpServletRequest request) {
        // Log all headers to see what the gateway is sending
        log.debug("=== Headers received from gateway ===");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            log.debug("Header: {} = {}", headerName, request.getHeader(headerName));
        });
        log.debug("=====================================");
        
        // Try to get user ID from X-User-Id header first (from gateway)
        String userIdHeader = request.getHeader("X-User-Id");
        log.debug(userIdHeader);
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            log.debug("hihihihi");
            try {
                Long userId = Long.parseLong(userIdHeader);
                log.debug("Found user ID from header: {}", userId);
                Optional<UserResponseDto> user = userService.getUserById(userId);
                return user.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID in header: {}", userIdHeader);
            }
        }
        
        // Fallback: try to decode JWT from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No Authorization header or X-User-Id header found");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            String token = authHeader.substring(7);
            log.debug("Token received: {}", token.substring(0, Math.min(20, token.length())) + "...");
            
            // Extract user ID from JWT token first
            Long userId = jwtUtil.extractUserId(token);
            log.debug("Extracted user ID from JWT: {}", userId);
            
            // Validate token with extracted user ID
            if (jwtUtil.isTokenValid(token, userId)) {
                Optional<UserResponseDto> user = userService.getUserById(userId);
                return user.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
            } else {
                log.warn("Invalid JWT token for user ID: {}", userId);
                return ResponseEntity.status(401).build();
            }
        } catch (Exception e) {
            log.error("Error processing JWT token", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getUserProfile(HttpServletRequest request) {
        // Extract user ID from request
        Long userId = extractUserIdFromRequest(request);
        if (userId == null) {
            log.warn("No valid user ID found in request");
            return ResponseEntity.badRequest().build();
        }
        
        //! Get user data from Auth Service
        Optional<UserResponseDto> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", userId);
            return ResponseEntity.notFound().build();
        }
        
        UserResponseDto user = userOpt.get();
        log.debug("Found user: {}", user.getEmail());
        
        //! Get profile data from Gig Service
        Optional<ProfileResponseDto> profileOpt = gigServiceClient.getProfileByUserId(userId);
        
        // Build combined response
        UserProfileDto.UserProfileDtoBuilder responseBuilder = UserProfileDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .role(user.getRole().name())
            .isActive(user.isActive())
            .stripeAccountId(user.getStripeAccountId())
            .createdAt(user.getCreatedAt());
        
        // Add profile data if available
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
        
        UserProfileDto response = responseBuilder.build();
        log.info("Successfully retrieved user profile for user: {}", userId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stripe-status")
    public ResponseEntity<?> getStripeAccountStatus(HttpServletRequest request) {
        log.info("Getting Stripe account status for current user");
        
        Long userId = extractUserIdFromRequest(request);
        if (userId == null) {
            log.warn("Could not extract user ID from request");
            return ResponseEntity.status(401).body("Unauthorized: Invalid or missing authentication");
        }
        
        log.debug("Checking Stripe account status for user: {}", userId);
        
        Optional<com.thefreelancer.microservices.auth.dto.StripeAccountStatusDto> statusOpt = userService.getStripeAccountStatus(userId);
        if (statusOpt.isEmpty()) {
            log.warn("User not found: {}", userId);
            return ResponseEntity.notFound().build();
        }
        
        com.thefreelancer.microservices.auth.dto.StripeAccountStatusDto status = statusOpt.get();
        log.info("Retrieved Stripe account status for user {}: hasAccount={}", userId, status.isHasStripeAccount());
        
        return ResponseEntity.ok(status);
    }
    
    private Long extractUserIdFromRequest(HttpServletRequest request) {
        // Try to get user ID from X-User-Id header first (from gateway)
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            try {
                Long userId = Long.parseLong(userIdHeader);
                log.debug("Found user ID from header: {}", userId);
                return userId;
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID in header: {}", userIdHeader);
            }
        }
        
        // Fallback: decode JWT from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No Authorization header or X-User-Id header found");
            return null;
        }
        
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            log.debug("Extracted user ID from JWT: {}", userId);
            
            if (jwtUtil.isTokenValid(token, userId)) {
                return userId;
            } else {
                log.warn("Invalid JWT token for user ID: {}", userId);
                return null;
            }
        } catch (Exception e) {
            log.error("Error processing JWT token", e);
            return null;
        }
    }
}
