package com.thefreelancer.microservices.gig.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileUpdateDto {
    
    @Size(max = 200, message = "Headline must not exceed 200 characters")
    private String headline;
    
    @Size(max = 2000, message = "Bio must not exceed 2000 characters")
    private String bio;
    
    @DecimalMin(value = "0", message = "Hourly rate must be positive")
    private Long hourlyRateCents;
    
    @Size(max = 3, message = "Currency must be 3 characters (ISO 4217)")
    private String currency;
    
    private String availability; // FULL_TIME, PART_TIME, OCCASIONAL, UNAVAILABLE
    
    private List<String> languages;
    
    private List<String> skills;
    
    @Size(max = 500, message = "Location text must not exceed 500 characters")
    private String locationText;
    
    @Size(max = 100, message = "GitHub username must not exceed 100 characters")
    private String githubUsername;
    
    @Size(max = 100, message = "GitLab username must not exceed 100 characters")
    private String gitlabUsername;
    
    @Size(max = 500, message = "Website URL must not exceed 500 characters")
    private String websiteUrl;
    
    @Size(max = 500, message = "LinkedIn URL must not exceed 500 characters")
    private String linkedinUrl;
}
