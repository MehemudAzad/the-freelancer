package com.thefreelancer.microservices.gig.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GigSearchRequestDto {
    private String query;
    private Integer limit = 20;
    private Integer offset = 0;
    private String category;
    private Double minRating;
    private String sortBy = "relevance";
}