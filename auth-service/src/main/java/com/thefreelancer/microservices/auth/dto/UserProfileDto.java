package com.thefreelancer.microservices.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserProfileDto {
    // User data from Auth Service
    private Long id;
    private String email;
    private String name;
    private String role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    
    // Profile data from Gig Service (nullable if no profile exists)
    private ProfileData profile;
    
    @Data
    @Builder
    public static class ProfileData {
        private String headline;
        private String bio;
        private Long hourlyRateCents;
        private String currency;
        private String availability;
        private List<String> languages;
        private List<String> skills;
        private String locationText;
        private String githubUsername;
        private String gitlabUsername;
        private String websiteUrl;
        private String linkedinUrl;
        private BigDecimal deliveryScore;
        private BigDecimal reviewAvg;
        private Integer reviewsCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
