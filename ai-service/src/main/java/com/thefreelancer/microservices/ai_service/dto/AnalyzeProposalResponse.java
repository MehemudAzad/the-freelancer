package com.thefreelancer.microservices.ai_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzeProposalResponse {
    private Integer overallScore; // 1-10
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> missingElements;
    private List<String> recommendations;
    
    // Detailed scores
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailedScores {
        private Integer clarityScore; // 1-10
        private Integer relevanceScore; // 1-10
        private Integer professionalismScore; // 1-10
        private Integer completenessScore; // 1-10
    }
    
    private DetailedScores detailedScores;
}