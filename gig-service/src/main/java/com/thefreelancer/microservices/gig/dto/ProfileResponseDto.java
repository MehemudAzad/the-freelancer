package com.thefreelancer.microservices.gig.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private Double deliveryScore;
    private Double reviewAvg;
    private Integer reviewsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
