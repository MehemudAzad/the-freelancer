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
public class ImproveProposalResponse {
    private String originalProposal;
    private String improvedProposal;
    private List<String> improvementSuggestions;
    private List<String> missingElements;
    private Integer strengthScore; // 1-10 rating
    private List<String> strengths;
    private List<String> areasForImprovement;
}