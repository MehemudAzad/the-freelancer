package com.thefreelancer.microservices.ai_service.service;
import com.thefreelancer.microservices.ai_service.dto.EnhanceTitleRequest;
import com.thefreelancer.microservices.ai_service.dto.EnhanceTitleResponse;
import com.thefreelancer.microservices.ai_service.dto.SuggestSkillsRequest;
import com.thefreelancer.microservices.ai_service.dto.SuggestSkillsResponse;
import com.thefreelancer.microservices.ai_service.dto.EnhanceDescriptionRequest;
import com.thefreelancer.microservices.ai_service.dto.EnhanceDescriptionResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thefreelancer.microservices.ai_service.dto.JobEnhancementRequest;
import com.thefreelancer.microservices.ai_service.dto.JobEnhancementResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Service for enhancing job descriptions using Spring AI and OpenAI
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobEnhancementService {

    /**
     * Suggest an improved job title based on the description
     */
    public EnhanceTitleResponse enhanceTitle(EnhanceTitleRequest request) {
        log.info("Enhancing job title based on description");
        try {
            String prompt = buildTitlePrompt(request.getDescription());
            String aiResponse = chatClient.prompt()
                    .system("You are an expert at writing concise, clear, and attractive job titles for freelance projects. Given the job description, suggest a professional title. Respond ONLY with the title, no extra text or formatting.")
                    .user(prompt)
                    .call()
                    .content();
            log.debug("AI suggested title: {}", aiResponse);
            return EnhanceTitleResponse.builder()
                    .suggestedTitle(aiResponse.trim().replaceAll("^\"|\"$", ""))
                    .build();
        } catch (Exception e) {
            log.error("Error enhancing title: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to enhance title: " + e.getMessage(), e);
        }
    }

    private String buildTitlePrompt(String description) {
        return "Job Description: " + description + "\n\nSuggest a concise, professional job title for this project.";
    }

    /**
     * Suggest relevant skills based on the job description
     */
    public SuggestSkillsResponse suggestSkills(SuggestSkillsRequest request) {
        log.info("Suggesting skills based on description");
        try {
            String prompt = buildSkillsPrompt(request.getDescription());
            String aiResponse = chatClient.prompt()
                    .system("You are an expert at analyzing any type of job description and identifying the key technologies, tools, software, or technical skills needed. Return only the technology names separated by commas. Examples: 'React, Node.js, MongoDB' or 'Photoshop, Illustrator, InDesign' or 'AutoCAD, SolidWorks, 3D Modeling'")
                    .user(prompt)
                    .call()
                    .content();
            
            log.debug("Raw AI response: {}", aiResponse);
            
            // Parse the comma-separated response
            List<String> skills = parseSkillsFromResponse(aiResponse);
            
            log.info("Extracted {} skills: {}", skills.size(), skills);
            return SuggestSkillsResponse.builder().suggestedSkills(skills).build();
            
        } catch (Exception e) {
            log.error("Error suggesting skills: {}", e.getMessage(), e);
            // Return basic fallback
            return SuggestSkillsResponse.builder()
                    .suggestedSkills(List.of("Technical Skills Required"))
                    .build();
        }
    }
    
    private List<String> parseSkillsFromResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return List.of("Technical Skills Required");
        }
        
        // Clean the response and split by commas
        String cleaned = aiResponse.trim()
                .replaceAll("^[\\[\"'`]", "")
                .replaceAll("[\\]\"'`]$", "")
                .replaceAll("```", "")
                .trim();
        
        List<String> skills = new ArrayList<>();
        String[] parts = cleaned.split("[,;\\n]");
        
        for (String part : parts) {
            String skill = part.trim()
                    .replaceAll("^[\\-\\*\\d\\.\\s]+", "") // Remove bullets/numbers
                    .replaceAll("[\"']", "") // Remove quotes
                    .trim();
            
            if (!skill.isEmpty() && skill.length() > 1) {
                skills.add(skill);
            }
        }
        
        // If no skills found, return a generic response
        if (skills.isEmpty()) {
            skills.add("Technical Skills Required");
        }
        
        return skills.stream().limit(10).collect(java.util.stream.Collectors.toList()); // Limit to 10 skills
    }
    private String buildSkillsPrompt(String description) {
        return "What are the main technologies, tools, software, or technical skills needed for this job?\n\n" + description + 
               "\n\nList the key technical requirements separated by commas (e.g., React, Photoshop, AutoCAD, etc.):";
    }

    /**
     * Enhance only the job description using AI
     */
    public EnhanceDescriptionResponse enhanceDescription(EnhanceDescriptionRequest request) {
        log.info("Enhancing job description only");
        try {
            String prompt = buildDescriptionOnlyPrompt(request.getDescription());
            String aiResponse = chatClient.prompt()
                    .system("You are an expert job description writer. Improve the following job description for clarity, professionalism, and attractiveness. Respond ONLY with the improved description, no extra text or formatting.")
                    .user(prompt)
                    .call()
                    .content();
            log.debug("AI enhanced description: {}", aiResponse);
            return EnhanceDescriptionResponse.builder()
                    .originalDescription(request.getDescription())
                    .enhancedDescription(aiResponse != null ? aiResponse.trim() : request.getDescription())
                    .build();
        } catch (Exception e) {
            log.error("Error enhancing description: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to enhance description: " + e.getMessage(), e);
        }
    }

    private String buildDescriptionOnlyPrompt(String description) {
        return "Original Description: " + description + "\n\nPlease rewrite this to be more clear, professional, and attractive to freelancers.";
    }
    
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Enhance a job description using AI
     */
    public JobEnhancementResponse enhanceJobDescription(JobEnhancementRequest request) {
        log.info("Enhancing job description for title: {}", request.getTitle());
        
        try {
            String prompt = buildEnhancementPrompt(request);
            
            log.debug("Sending request to OpenAI via Spring AI");
            String aiResponse = chatClient.prompt()
                    .system(getSystemPrompt())
                    .user(prompt)
                    .call()
                    .content();
            
            log.debug("Received response from OpenAI: {}", aiResponse);
            
            return parseAIResponse(request, aiResponse, 0); // Token count not available in this simple version
            
        } catch (Exception e) {
            log.error("Error enhancing job description: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to enhance job description: " + e.getMessage(), e);
        }
    }
    
    private String getSystemPrompt() {
        return """
            You are an expert job description writer for a freelance platform. Your task is to enhance job descriptions to make them clear, professional, and attractive to freelancers.
            
            Guidelines:
            1. Make the description clear and specific
            2. Include all necessary technical requirements
            3. Add professional structure and formatting
            4. Suggest appropriate skills if missing
            5. Estimate complexity and budget range
            6. Identify missing elements
            7. Provide quality scores
            
            IMPORTANT: Always respond with valid JSON in exactly this format:
            {
              "enhancedTitle": "improved title",
              "enhancedDescription": "improved description with better structure",
              "suggestedSkills": ["skill1", "skill2"],
              "estimatedComplexity": "beginner|intermediate|expert",
              "suggestedBudgetRange": "$1000-$5000",
              "estimatedTimeframe": "2-4 weeks",
              "improvementSuggestions": ["suggestion1", "suggestion2"],
              "missingElements": ["element1", "element2"],
              "confidenceScore": 0.85,
              "qualityMetrics": {
                "clarityScore": 8,
                "completenessScore": 7,
                "professionalismScore": 9,
                "overallScore": 8,
                "strengths": ["clear requirements", "good structure"],
                "areasForImprovement": ["add timeline", "specify deliverables"]
              }
            }
            """;
    }
    
    private String buildEnhancementPrompt(JobEnhancementRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Please enhance this job posting:\n\n");
        
        prompt.append("**Original Title:** ").append(request.getTitle()).append("\n");
        prompt.append("**Original Description:** ").append(request.getDescription()).append("\n");
        
        if (request.getBudgetRange() != null) {
            prompt.append("**Budget Range:** ").append(request.getBudgetRange()).append("\n");
        }
        
        if (request.getRequiredSkills() != null && !request.getRequiredSkills().isEmpty()) {
            prompt.append("**Required Skills:** ").append(String.join(", ", request.getRequiredSkills())).append("\n");
        }
        
        if (request.getCategory() != null) {
            prompt.append("**Category:** ").append(request.getCategory()).append("\n");
        }
        
        if (request.getExperienceLevel() != null) {
            prompt.append("**Experience Level:** ").append(request.getExperienceLevel()).append("\n");
        }
        
        if (request.getProjectDuration() != null) {
            prompt.append("**Project Duration:** ").append(request.getProjectDuration()).append("\n");
        }
        
        if (request.getRemoteWork() != null) {
            prompt.append("**Remote Work:** ").append(request.getRemoteWork() ? "Yes" : "No").append("\n");
        }
        
        if (request.getAdditionalContext() != null) {
            prompt.append("**Additional Context:** ").append(request.getAdditionalContext()).append("\n");
        }
        
        prompt.append("\nPlease provide an enhanced version that is more professional, clear, and attractive to freelancers.");
        
        return prompt.toString();
    }
    
    private JobEnhancementResponse parseAIResponse(JobEnhancementRequest request, String aiResponse, Integer tokensUsed) {
        try {
            // Clean the response - remove any markdown formatting
            String cleanedResponse = aiResponse.trim();
            if (cleanedResponse.startsWith("```json")) {
                cleanedResponse = cleanedResponse.substring(7);
            }
            if (cleanedResponse.endsWith("```")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
            }
            cleanedResponse = cleanedResponse.trim();
            
            log.debug("Parsing AI response: {}", cleanedResponse);
            
            JsonNode jsonNode = objectMapper.readTree(cleanedResponse);
            
            JobEnhancementResponse.QualityMetrics qualityMetrics = null;
            if (jsonNode.has("qualityMetrics")) {
                JsonNode metricsNode = jsonNode.get("qualityMetrics");
                qualityMetrics = JobEnhancementResponse.QualityMetrics.builder()
                        .clarityScore(metricsNode.get("clarityScore").asInt())
                        .completenessScore(metricsNode.get("completenessScore").asInt())
                        .professionalismScore(metricsNode.get("professionalismScore").asInt())
                        .overallScore(metricsNode.get("overallScore").asInt())
                        .strengths(jsonArrayToList(metricsNode.get("strengths")))
                        .areasForImprovement(jsonArrayToList(metricsNode.get("areasForImprovement")))
                        .build();
            }
            
            return JobEnhancementResponse.builder()
                    .originalTitle(request.getTitle())
                    .enhancedTitle(jsonNode.get("enhancedTitle").asText())
                    .originalDescription(request.getDescription())
                    .enhancedDescription(jsonNode.get("enhancedDescription").asText())
                    .suggestedSkills(jsonArrayToList(jsonNode.get("suggestedSkills")))
                    .estimatedComplexity(jsonNode.get("estimatedComplexity").asText())
                    .suggestedBudgetRange(jsonNode.get("suggestedBudgetRange").asText())
                    .estimatedTimeframe(jsonNode.get("estimatedTimeframe").asText())
                    .improvementSuggestions(jsonArrayToList(jsonNode.get("improvementSuggestions")))
                    .missingElements(jsonArrayToList(jsonNode.get("missingElements")))
                    .tokensUsed(tokensUsed)
                    .model("gpt-4o-mini")
                    .processedAt(LocalDateTime.now())
                    .confidenceScore(jsonNode.get("confidenceScore").asDouble())
                    .qualityMetrics(qualityMetrics)
                    .build();
                    
        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response as JSON: {}", aiResponse, e);
            
            // Fallback: create a basic response
            return createFallbackResponse(request, aiResponse, tokensUsed);
        }
    }
    
    private List<String> jsonArrayToList(JsonNode arrayNode) {
        List<String> list = new ArrayList<>();
        if (arrayNode != null && arrayNode.isArray()) {
            arrayNode.forEach(node -> list.add(node.asText()));
        }
        return list;
    }
    
    private JobEnhancementResponse createFallbackResponse(JobEnhancementRequest request, String aiResponse, Integer tokensUsed) {
        log.warn("Creating fallback response due to JSON parsing failure");
        
        return JobEnhancementResponse.builder()
                .originalTitle(request.getTitle())
                .enhancedTitle(request.getTitle()) // Keep original
                .originalDescription(request.getDescription())
                .enhancedDescription(aiResponse) // Use raw AI response
                .suggestedSkills(request.getRequiredSkills() != null ? request.getRequiredSkills() : List.of())
                .estimatedComplexity("intermediate")
                .suggestedBudgetRange(request.getBudgetRange() != null ? request.getBudgetRange() : "$1000-$5000")
                .estimatedTimeframe("2-4 weeks")
                .improvementSuggestions(List.of("AI response could not be parsed properly"))
                .missingElements(List.of())
                .tokensUsed(tokensUsed)
                .model("gpt-4o-mini")
                .processedAt(LocalDateTime.now())
                .confidenceScore(0.5)
                .qualityMetrics(JobEnhancementResponse.QualityMetrics.builder()
                        .clarityScore(5)
                        .completenessScore(5)
                        .professionalismScore(5)
                        .overallScore(5)
                        .strengths(List.of("Basic response provided"))
                        .areasForImprovement(List.of("AI response parsing failed"))
                        .build())
                .build();
    }
}