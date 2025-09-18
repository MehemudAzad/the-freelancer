package com.thefreelancer.microservices.gig.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class GigSearchResultDto {
    private Long gigId;
    private Long profileId;
    private String title;
    private String description;
    private String category;
    private List<String> tags;
    private String status;
    private Double reviewAvg;
    private Integer reviewsCount;
    private Double relevanceScore;
    private String matchReason;
    private Boolean hasPackages;
}