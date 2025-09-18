package com.thefreelancer.microservices.gig.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GigSearchResponseDto {
    private String query;
    private String enhancedQuery;
    private List<GigSearchResultDto> results;
    private Integer totalResults;
    private String searchType;
    private String error;
}