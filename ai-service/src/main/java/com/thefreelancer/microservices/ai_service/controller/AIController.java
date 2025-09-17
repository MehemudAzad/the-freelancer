package com.thefreelancer.microservices.ai_service.controller;
import com.thefreelancer.microservices.ai_service.dto.EnhanceDescriptionRequest;
import com.thefreelancer.microservices.ai_service.dto.EnhanceDescriptionResponse;
import com.thefreelancer.microservices.ai_service.dto.EnhanceTitleRequest;
import com.thefreelancer.microservices.ai_service.dto.EnhanceTitleResponse;
import com.thefreelancer.microservices.ai_service.dto.SuggestSkillsRequest;
import com.thefreelancer.microservices.ai_service.dto.SuggestSkillsResponse;

import com.thefreelancer.microservices.ai_service.dto.JobEnhancementRequest;
import com.thefreelancer.microservices.ai_service.dto.JobEnhancementResponse;

// Proposal Assistant DTOs
import com.thefreelancer.microservices.ai_service.dto.ImproveProposalRequest;
import com.thefreelancer.microservices.ai_service.dto.ImproveProposalResponse;
import com.thefreelancer.microservices.ai_service.dto.AnalyzeProposalRequest;
import com.thefreelancer.microservices.ai_service.dto.AnalyzeProposalResponse;
import com.thefreelancer.microservices.ai_service.dto.GenerateCoverLetterRequest;
import com.thefreelancer.microservices.ai_service.dto.GenerateCoverLetterResponse;

import com.thefreelancer.microservices.ai_service.service.JobEnhancementService;
import com.thefreelancer.microservices.ai_service.service.ProposalAssistantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AIController {
    
    private final JobEnhancementService jobEnhancementService;
    private final ProposalAssistantService proposalAssistantService;

    @PostMapping("/enhance-description")
    public ResponseEntity<EnhanceDescriptionResponse> enhanceDescription(@Valid @RequestBody EnhanceDescriptionRequest request) {
        return ResponseEntity.ok(jobEnhancementService.enhanceDescription(request));
    }

    @PostMapping("/enhance-title")
    public ResponseEntity<EnhanceTitleResponse> enhanceTitle(@Valid @RequestBody EnhanceTitleRequest request) {
        return ResponseEntity.ok(jobEnhancementService.enhanceTitle(request));
    }

    @PostMapping("/suggest-skills")
    public ResponseEntity<SuggestSkillsResponse> suggestSkills(@Valid @RequestBody SuggestSkillsRequest request) {
        return ResponseEntity.ok(jobEnhancementService.suggestSkills(request));
    }
    
    // Proposal Assistant Endpoints
    @PostMapping("/improve-proposal")
    public ResponseEntity<ImproveProposalResponse> improveProposal(@Valid @RequestBody ImproveProposalRequest request) {
        log.info("Received proposal improvement request");
        return ResponseEntity.ok(proposalAssistantService.improveProposal(request));
    }
    
    @PostMapping("/analyze-proposal")
    public ResponseEntity<AnalyzeProposalResponse> analyzeProposal(@Valid @RequestBody AnalyzeProposalRequest request) {
        log.info("Received proposal analysis request");
        return ResponseEntity.ok(proposalAssistantService.analyzeProposal(request));
    }
    
    @PostMapping("/generate-cover-letter")
    public ResponseEntity<GenerateCoverLetterResponse> generateCoverLetter(@Valid @RequestBody GenerateCoverLetterRequest request) {
        log.info("Received cover letter generation request for job: {}", request.getJobTitle());
        return ResponseEntity.ok(proposalAssistantService.generateCoverLetter(request));
    }
    
    @PostMapping("/enhance-job")
    public ResponseEntity<JobEnhancementResponse> enhanceJobDescription(@Valid @RequestBody JobEnhancementRequest request) {
        log.info("Received job enhancement request for title: {}", request.getTitle());
        
        try {
            JobEnhancementResponse response = jobEnhancementService.enhanceJobDescription(request);
            log.info("Successfully enhanced job description. Tokens used: {}", response.getTokensUsed());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error enhancing job description: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/info")
    public ResponseEntity<?> getServiceInfo() {
        return ResponseEntity.ok(new Object() {
            public final String service = "AI Service";
            public final String version = "1.0.0";
            public final String description = "AI-powered job description enhancement";
        });
    }
    
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(new Object() {
            public final String status = "UP";
            public final String service = "ai-service";
        });
    }
}