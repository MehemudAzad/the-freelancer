package com.thefreelancer.microservices.gig.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GigPackageUpdateDto {
    
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @Positive(message = "Price must be positive")
    private BigDecimal priceCents;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    @Positive(message = "Delivery days must be positive")
    private Integer deliveryDays;
    
    @PositiveOrZero(message = "Revisions must be zero or positive")
    private Integer revisions;
}
