package com.thefreelancer.microservices.ai_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for job description enhancement
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobEnhancementResponse {
    
    private String originalTitle;
    private String enhancedTitle;
    
    private String originalDescription;
    private String enhancedDescription;
    
    private List<String> suggestedSkills;
    private String estimatedComplexity; // "beginner", "intermediate", "expert"
    private String suggestedBudgetRange;
    private String estimatedTimeframe;
    
    private List<String> improvementSuggestions;
    private List<String> missingElements;
    
    // AI processing metadata
    private Integer tokensUsed;
    private String model;
    private LocalDateTime processedAt;
    private Double confidenceScore; // 0.0 to 1.0
    
    // Quality metrics
    private QualityMetrics qualityMetrics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityMetrics {
        private Integer clarityScore; // 1-10
        private Integer completenessScore; // 1-10
        private Integer professionalismScore; // 1-10
        private Integer overallScore; // 1-10
        private List<String> strengths;
        private List<String> areasForImprovement;
    }
}