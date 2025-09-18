package com.thefreelancer.microservices.auth.dto;

import com.thefreelancer.microservices.auth.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWithProfileResponseDto {
    // User fields
    private Long id;
    private String email;
    private String name;
    private String handle;
    private String role;
    private String country;
    private String timezone;
    private Boolean isActive;
    private String stripeAccountId;
    private String kycStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Profile fields (nullable if profile doesn't exist)
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
    private String profilePictureUrl;
    private BigDecimal deliveryScore;
    private BigDecimal reviewAvg;
    private Integer reviewsCount;
    private LocalDateTime profileCreatedAt;
    private LocalDateTime profileUpdatedAt;

    public static UserWithProfileResponseDto fromUserAndProfile(User user, ProfileResponseDto profile) {
        UserWithProfileResponseDto dto = new UserWithProfileResponseDto();
        
        // Set user fields
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setHandle(user.getHandle());
        dto.setRole(user.getRole().name());
        dto.setCountry(user.getCountry());
        dto.setTimezone(user.getTimezone());
        dto.setIsActive(user.isActive());
        dto.setStripeAccountId(user.getStripeAccountId());
        dto.setKycStatus(user.getKycStatus() != null ? user.getKycStatus().name() : null);
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        // Set profile fields if profile exists
        if (profile != null) {
            dto.setHeadline(profile.getHeadline());
            dto.setBio(profile.getBio());
            dto.setHourlyRateCents(profile.getHourlyRateCents());
            dto.setCurrency(profile.getCurrency());
            dto.setAvailability(profile.getAvailability());
            dto.setLanguages(profile.getLanguages());
            dto.setSkills(profile.getSkills());
            dto.setLocationText(profile.getLocationText());
            dto.setGithubUsername(profile.getGithubUsername());
            dto.setGitlabUsername(profile.getGitlabUsername());
            dto.setWebsiteUrl(profile.getWebsiteUrl());
            dto.setLinkedinUrl(profile.getLinkedinUrl());
            dto.setProfilePictureUrl(profile.getProfilePictureUrl());
            dto.setDeliveryScore(profile.getDeliveryScore());
            dto.setReviewAvg(profile.getReviewAvg());
            dto.setReviewsCount(profile.getReviewsCount());
            dto.setProfileCreatedAt(profile.getCreatedAt());
            dto.setProfileUpdatedAt(profile.getUpdatedAt());
        }
        
        return dto;
    }
    
    public static UserWithProfileResponseDto fromUser(User user) {
        return fromUserAndProfile(user, null);
    }
}