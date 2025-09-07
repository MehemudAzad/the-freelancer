package com.thefreelancer.microservices.gig.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeCreateDto {
    
    @NotBlank(message = "Badge type is required")
    @Size(max = 100, message = "Badge type must not exceed 100 characters")
    private String type;
    
    @DecimalMin(value = "0.0", message = "Score must be positive")
    @DecimalMax(value = "100.0", message = "Score must not exceed 100")
    private Double score;
}
