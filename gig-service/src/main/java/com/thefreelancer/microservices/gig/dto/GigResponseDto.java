package com.thefreelancer.microservices.gig.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GigResponseDto {
    
    private Long id;
    private Long profileId;
    private String title;
    private String description;
    private String status;
    private String category;
    private List<String> tags;
    private Double reviewAvg;
    private Integer reviewsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
