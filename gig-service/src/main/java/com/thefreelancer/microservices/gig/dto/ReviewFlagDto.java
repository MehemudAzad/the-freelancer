package com.thefreelancer.microservices.gig.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewFlagDto {
    
    @NotBlank(message = "Flag reason is required")
    @Size(max = 500, message = "Flag reason cannot exceed 500 characters")
    private String reason;
    
    @Size(max = 1000, message = "Additional details cannot exceed 1000 characters")
    private String details;
}
