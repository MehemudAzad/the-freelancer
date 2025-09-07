package com.thefreelancer.microservices.gig.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GigPackageResponseDto {
    private Long id;
    private Long gigId;
    private String tier;
    private String title;
    private String description;
    private Double priceCents;
    private String currency;
    private Integer deliveryDays;
    private Integer revisions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
