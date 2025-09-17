package com.thefreelancer.microservices.ai_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thefreelancer.microservices.ai_service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service for proposal writing assistance using AI
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProposalAssistantService {
    
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Improve an existing proposal
     */
    public ImproveProposalResponse improveProposal(ImproveProposalRequest request) {
        log.info("Improving proposal content");
        try {
            String prompt = buildImproveProposalPrompt(request);
            String aiResponse = chatClient.prompt()
                    .system("You are an expert proposal writer who helps freelancers win more projects. Improve the proposal while keeping the freelancer's voice and style. Make it more compelling, professional, and persuasive.")
                    .user(prompt)
                    .call()
                    .content();
                    
            return parseImproveProposalResponse(request.getProposalContent(), aiResponse);
            
        } catch (Exception e) {
            log.error("Error improving proposal: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to improve proposal: " + e.getMessage(), e);
        }
    }
    
    /**
     * Analyze a proposal and provide feedback
     */
    public AnalyzeProposalResponse analyzeProposal(AnalyzeProposalRequest request) {
        log.info("Analyzing proposal content");
        try {
            String prompt = buildAnalyzeProposalPrompt(request);
            String aiResponse = chatClient.prompt()
                    .system("You are an expert proposal evaluator. Analyze the proposal and provide constructive feedback in JSON format.")
                    .user(prompt)
                    .call()
                    .content();
                    
            return parseAnalyzeProposalResponse(aiResponse);
            
        } catch (Exception e) {
            log.error("Error analyzing proposal: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to analyze proposal: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate a cover letter template
     */
    public GenerateCoverLetterResponse generateCoverLetter(GenerateCoverLetterRequest request) {
        log.info("Generating cover letter for job: {}", request.getJobTitle());
        try {
            String prompt = buildCoverLetterPrompt(request);
            String aiResponse = chatClient.prompt()
                    .system("You are an expert at writing compelling cover letters for freelance proposals. Write a professional, engaging cover letter that highlights relevant experience and shows enthusiasm for the project.")
                    .user(prompt)
                    .call()
                    .content();
                    
            return GenerateCoverLetterResponse.builder()
                    .coverLetter(aiResponse.trim())
                    .tone("professional")
                    .wordCount(countWords(aiResponse))
                    .build();
                    
        } catch (Exception e) {
            log.error("Error generating cover letter: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate cover letter: " + e.getMessage(), e);
        }
    }
    
    private String buildImproveProposalPrompt(ImproveProposalRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Please improve this freelance proposal:\n\n");
        prompt.append("**Original Proposal:**\n").append(request.getProposalContent()).append("\n\n");
        
        if (request.getJobTitle() != null) {
            prompt.append("**Job Title:** ").append(request.getJobTitle()).append("\n");
        }
        
        if (request.getJobDescription() != null) {
            prompt.append("**Job Description:** ").append(request.getJobDescription()).append("\n\n");
        }
        
        prompt.append("Provide:\n");
        prompt.append("1. An improved version of the proposal\n");
        prompt.append("2. Specific improvement suggestions\n");
        prompt.append("3. Missing elements that should be added\n");
        prompt.append("4. Strengths of the original proposal\n");
        prompt.append("5. Areas for improvement\n");
        prompt.append("6. A strength score from 1-10\n");
        
        return prompt.toString();
    }
    
    private String buildAnalyzeProposalPrompt(AnalyzeProposalRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this freelance proposal and provide detailed feedback:\n\n");
        prompt.append("**Proposal:**\n").append(request.getProposalContent()).append("\n\n");
        
        if (request.getJobDescription() != null) {
            prompt.append("**Job Description:** ").append(request.getJobDescription()).append("\n\n");
        }
        
        prompt.append("Provide analysis in JSON format with:\n");
        prompt.append("{\n");
        prompt.append("  \"overallScore\": 1-10,\n");
        prompt.append("  \"strengths\": [\"strength1\", \"strength2\"],\n");
        prompt.append("  \"weaknesses\": [\"weakness1\", \"weakness2\"],\n");
        prompt.append("  \"missingElements\": [\"element1\", \"element2\"],\n");
        prompt.append("  \"recommendations\": [\"rec1\", \"rec2\"],\n");
        prompt.append("  \"detailedScores\": {\n");
        prompt.append("    \"clarityScore\": 1-10,\n");
        prompt.append("    \"relevanceScore\": 1-10,\n");
        prompt.append("    \"professionalismScore\": 1-10,\n");
        prompt.append("    \"completenessScore\": 1-10\n");
        prompt.append("  }\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    private String buildCoverLetterPrompt(GenerateCoverLetterRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a compelling cover letter for this freelance job:\n\n");
        
        if (request.getJobTitle() != null) {
            prompt.append("**Job Title:** ").append(request.getJobTitle()).append("\n");
        }
        
        prompt.append("**Job Description:** ").append(request.getJobDescription()).append("\n\n");
        
        if (request.getFreelancerExperience() != null) {
            prompt.append("**Freelancer Experience:** ").append(request.getFreelancerExperience()).append("\n");
        }
        
        if (request.getKeySkills() != null) {
            prompt.append("**Key Skills:** ").append(request.getKeySkills()).append("\n");
        }
        
        prompt.append("\nWrite a professional cover letter that:\n");
        prompt.append("- Shows understanding of the project requirements\n");
        prompt.append("- Highlights relevant experience and skills\n");
        prompt.append("- Demonstrates enthusiasm for the project\n");
        prompt.append("- Includes a clear call to action\n");
        prompt.append("- Is concise but compelling (200-300 words)\n");
        
        return prompt.toString();
    }
    
    private ImproveProposalResponse parseImproveProposalResponse(String originalProposal, String aiResponse) {
        // For now, use simple text parsing. In production, you might want to use JSON format
        String[] sections = aiResponse.split("\n\n");
        
        return ImproveProposalResponse.builder()
                .originalProposal(originalProposal)
                .improvedProposal(extractImprovedProposal(aiResponse))
                .improvementSuggestions(extractListFromText(aiResponse, "improvement"))
                .missingElements(extractListFromText(aiResponse, "missing"))
                .strengths(extractListFromText(aiResponse, "strength"))
                .areasForImprovement(extractListFromText(aiResponse, "area"))
                .strengthScore(8) // Default score, could be extracted from AI response
                .build();
    }
    
    private AnalyzeProposalResponse parseAnalyzeProposalResponse(String aiResponse) {
        try {
            // Clean the response
            String cleanedResponse = aiResponse.trim()
                    .replaceAll("^```json\\s*", "")
                    .replaceAll("^```\\s*", "")
                    .replaceAll("```\\s*$", "")
                    .trim();
                    
            JsonNode jsonNode = objectMapper.readTree(cleanedResponse);
            
            AnalyzeProposalResponse.DetailedScores detailedScores = null;
            if (jsonNode.has("detailedScores")) {
                JsonNode scoresNode = jsonNode.get("detailedScores");
                detailedScores = AnalyzeProposalResponse.DetailedScores.builder()
                        .clarityScore(scoresNode.get("clarityScore").asInt())
                        .relevanceScore(scoresNode.get("relevanceScore").asInt())
                        .professionalismScore(scoresNode.get("professionalismScore").asInt())
                        .completenessScore(scoresNode.get("completenessScore").asInt())
                        .build();
            }
            
            return AnalyzeProposalResponse.builder()
                    .overallScore(jsonNode.get("overallScore").asInt())
                    .strengths(jsonArrayToList(jsonNode.get("strengths")))
                    .weaknesses(jsonArrayToList(jsonNode.get("weaknesses")))
                    .missingElements(jsonArrayToList(jsonNode.get("missingElements")))
                    .recommendations(jsonArrayToList(jsonNode.get("recommendations")))
                    .detailedScores(detailedScores)
                    .build();
                    
        } catch (JsonProcessingException e) {
            log.error("Failed to parse analysis response as JSON: {}", aiResponse, e);
            // Return fallback response
            return createFallbackAnalysisResponse();
        }
    }
    
    private List<String> jsonArrayToList(JsonNode arrayNode) {
        List<String> list = new ArrayList<>();
        if (arrayNode != null && arrayNode.isArray()) {
            arrayNode.forEach(node -> list.add(node.asText()));
        }
        return list;
    }
    
    private String extractImprovedProposal(String aiResponse) {
        // Simple extraction - look for the improved proposal section
        String[] lines = aiResponse.split("\n");
        StringBuilder improved = new StringBuilder();
        boolean capturing = false;
        
        for (String line : lines) {
            if (line.toLowerCase().contains("improved") && line.toLowerCase().contains("proposal")) {
                capturing = true;
                continue;
            }
            if (capturing && (line.toLowerCase().contains("suggestion") || line.toLowerCase().contains("missing"))) {
                break;
            }
            if (capturing && !line.trim().isEmpty()) {
                improved.append(line).append("\n");
            }
        }
        
        return improved.length() > 0 ? improved.toString().trim() : aiResponse;
    }
    
    private List<String> extractListFromText(String text, String keyword) {
        List<String> items = new ArrayList<>();
        String[] lines = text.split("\n");
        boolean capturing = false;
        
        for (String line : lines) {
            if (line.toLowerCase().contains(keyword)) {
                capturing = true;
                continue;
            }
            if (capturing && line.trim().startsWith("-") || line.trim().startsWith("•") || line.trim().matches("^\\d+\\..*")) {
                items.add(line.trim().replaceAll("^[-•\\d\\.\\s]+", ""));
            } else if (capturing && !line.trim().isEmpty() && !line.trim().startsWith("-")) {
                break;
            }
        }
        
        return items.isEmpty() ? Arrays.asList("No specific " + keyword + "s identified") : items;
    }
    
    private AnalyzeProposalResponse createFallbackAnalysisResponse() {
        return AnalyzeProposalResponse.builder()
                .overallScore(7)
                .strengths(Arrays.asList("Proposal shows effort", "Clear communication"))
                .weaknesses(Arrays.asList("Could be more specific", "Analysis incomplete"))
                .missingElements(Arrays.asList("Unable to analyze completely"))
                .recommendations(Arrays.asList("Consider revising and resubmitting"))
                .detailedScores(AnalyzeProposalResponse.DetailedScores.builder()
                        .clarityScore(7)
                        .relevanceScore(7)
                        .professionalismScore(7)
                        .completenessScore(6)
                        .build())
                .build();
    }
    
    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
}