package com.thefreelancer.microservices.auth.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProfileResponseDto {
    private Long userId;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
