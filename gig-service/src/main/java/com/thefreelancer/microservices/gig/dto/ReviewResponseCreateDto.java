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
public class ReviewResponseCreateDto {
    
    @NotBlank(message = "Response is required")
    @Size(max = 1000, message = "Response cannot exceed 1000 characters")
    private String response;
}
