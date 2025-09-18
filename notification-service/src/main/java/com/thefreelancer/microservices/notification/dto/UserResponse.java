package com.thefreelancer.microservices.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
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
}