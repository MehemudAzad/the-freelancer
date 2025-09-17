package com.thefreelancer.microservices.ai_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateCoverLetterRequest {
    @NotBlank(message = "Job description is required")
    @Size(max = 5000, message = "Job description must not exceed 5000 characters")
    private String jobDescription;
    
    @Size(max = 200, message = "Job title must not exceed 200 characters")
    private String jobTitle;
    
    @Size(max = 1000, message = "Freelancer experience must not exceed 1000 characters")
    private String freelancerExperience; // Optional: freelancer's background
    
    @Size(max = 500, message = "Key skills must not exceed 500 characters")
    private String keySkills; // Optional: freelancer's relevant skills
}