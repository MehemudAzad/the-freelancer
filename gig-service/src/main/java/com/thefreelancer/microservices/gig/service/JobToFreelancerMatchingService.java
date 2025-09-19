package com.thefreelancer.microservices.gig.service;

import com.thefreelancer.microservices.gig.dto.JobMatchingRequestDto;
import com.thefreelancer.microservices.gig.dto.JobMatchingResponseDto;
import com.thefreelancer.microservices.gig.model.Profile;
import com.thefreelancer.microservices.gig.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobToFreelancerMatchingService {
    
    private final ProfileEmbeddingService profileEmbeddingService;
    private final ProfileRepository profileRepository;
    
    public JobMatchingResponseDto findMatchingFreelancers(JobMatchingRequestDto request) {
        long startTime = System.currentTimeMillis();
        log.info("Finding matching freelancers for job: {} with {} required skills", 
                request.getJobId(), request.getRequiredSkills() != null ? request.getRequiredSkills().size() : 0);
        
        try {
            // Create search query from job requirements
            String searchQuery = createJobSearchQuery(request);
            log.debug("Created search query: {}", searchQuery);
            
            // Find similar profiles using semantic search
            List<Document> similarProfiles = profileEmbeddingService.findSimilarProfiles(
                searchQuery, 
                request.getLimit() * 2, // Get more to allow for filtering
                request.getMinSimilarityScore()
            );
            
            log.info("Found {} similar profiles from vector search", similarProfiles.size());
            
            // Get detailed profile information and apply filters
            List<JobMatchingResponseDto.MatchedFreelancerDto> matchedFreelancers = similarProfiles.stream()
                .map(doc -> {
                    try {
                        return createMatchedFreelancerFromDocument(doc, request);
                    } catch (Exception e) {
                        log.warn("Failed to process profile document: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(freelancer -> applyFilters(freelancer, request))
                .limit(request.getLimit())
                .collect(Collectors.toList());
            
            // Calculate statistics
            double averageScore = matchedFreelancers.stream()
                .mapToDouble(JobMatchingResponseDto.MatchedFreelancerDto::getMatchScore)
                .average()
                .orElse(0.0);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Create filters summary
            JobMatchingResponseDto.FiltersSummaryDto filtersSummary = createFiltersSummary(request, similarProfiles.size(), matchedFreelancers.size());
            
            log.info("Successfully matched {} freelancers for job {} in {}ms", 
                    matchedFreelancers.size(), request.getJobId(), processingTime);
            
            return JobMatchingResponseDto.builder()
                .jobId(request.getJobId())
                .matchedFreelancers(matchedFreelancers)
                .totalMatches(matchedFreelancers.size())
                .averageMatchScore(averageScore)
                .searchQuery(searchQuery)
                .processingTimeMs(processingTime)
                .appliedFilters(filtersSummary)
                .build();
                
        } catch (Exception e) {
            log.error("Error finding matching freelancers for job {}: {}", request.getJobId(), e.getMessage(), e);
            throw new RuntimeException("Failed to find matching freelancers", e);
        }
    }
    
    private String createJobSearchQuery(JobMatchingRequestDto request) {
        StringBuilder query = new StringBuilder();
        
        // Add job title and description
        if (request.getTitle() != null) {
            query.append(request.getTitle()).append(" ");
        }
        
        if (request.getDescription() != null) {
            // Extract key technical terms from description
            String cleanDescription = extractKeyTermsFromDescription(request.getDescription());
            query.append(cleanDescription).append(" ");
        }
        
        // Add required skills with higher weight
        if (request.getRequiredSkills() != null && !request.getRequiredSkills().isEmpty()) {
            String requiredSkillsText = String.join(" ", request.getRequiredSkills());
            query.append("Required skills: ").append(requiredSkillsText).append(" ");
            // Add skills again for emphasis
            query.append(requiredSkillsText).append(" ");
        }
        
        // Add preferred skills
        if (request.getPreferredSkills() != null && !request.getPreferredSkills().isEmpty()) {
            query.append("Preferred: ").append(String.join(" ", request.getPreferredSkills())).append(" ");
        }
        
        // Add category
        if (request.getCategory() != null) {
            query.append(request.getCategory()).append(" ");
        }
        
        return query.toString().trim();
    }
    
    private String extractKeyTermsFromDescription(String description) {
        // Simple keyword extraction - can be enhanced with NLP
        String[] techKeywords = {
            "React", "Vue", "Angular", "JavaScript", "TypeScript", "Node.js", "Python", "Java", 
            "Spring", "Django", "Flask", "PostgreSQL", "MySQL", "MongoDB", "Redis", "Docker", 
            "Kubernetes", "AWS", "Azure", "GCP", "API", "REST", "GraphQL", "microservices",
            "machine learning", "AI", "data science", "frontend", "backend", "full stack",
            "mobile", "iOS", "Android", "React Native", "Flutter", "DevOps", "CI/CD"
        };
        
        Set<String> foundKeywords = new HashSet<>();
        String lowerDescription = description.toLowerCase();
        
        for (String keyword : techKeywords) {
            if (lowerDescription.contains(keyword.toLowerCase())) {
                foundKeywords.add(keyword);
            }
        }
        
        return String.join(" ", foundKeywords);
    }
    
    private JobMatchingResponseDto.MatchedFreelancerDto createMatchedFreelancerFromDocument(
            Document doc, JobMatchingRequestDto request) {
        
        Long userId = Long.parseLong(doc.getMetadata().get("userId").toString());
        
        // Get full profile information from database
        Optional<Profile> profileOpt = profileRepository.findByUserId(userId);
        if (profileOpt.isEmpty()) {
            log.warn("Profile not found in database for userId: {}", userId);
            return null;
        }
        
        Profile profile = profileOpt.get();
        
        // Calculate match score (similarity score from vector search)
        double matchScore = 1.0 - Double.parseDouble(doc.getMetadata().getOrDefault("distance", "0").toString());
        
        // Analyze skill matches
        List<JobMatchingResponseDto.SkillMatchDto> skillMatches = analyzeSkillMatches(
            profile.getSkills() != null ? Arrays.asList(profile.getSkills()) : Collections.emptyList(),
            request.getRequiredSkills(),
            request.getPreferredSkills()
        );
        
        // Generate match reason
        String matchReason = generateMatchReason(profile, request, skillMatches, matchScore);
        
        return JobMatchingResponseDto.MatchedFreelancerDto.builder()
            .userId(profile.getUserId())
            .headline(profile.getHeadline())
            .bio(profile.getBio())
            .skills(profile.getSkills() != null ? Arrays.asList(profile.getSkills()) : Collections.emptyList())
            .matchScore(matchScore)
            .hourlyRateCents(profile.getHourlyRateCents())
            .currency(profile.getCurrency())
            .locationText(profile.getLocationText())
            .availability(profile.getAvailability() != null ? profile.getAvailability().name() : null)
            .reviewAvg(profile.getReviewAvg() != null ? profile.getReviewAvg().doubleValue() : null)
            .reviewsCount(profile.getReviewsCount())
            .deliveryScore(profile.getDeliveryScore() != null ? profile.getDeliveryScore().doubleValue() : null)
            .profilePictureUrl(profile.getProfilePictureUrl())
            .websiteUrl(profile.getWebsiteUrl())
            .linkedinUrl(profile.getLinkedinUrl())
            .githubUsername(profile.getGithubUsername())
            .skillMatches(skillMatches)
            .matchReason(matchReason)
            .build();
    }
    
    private List<JobMatchingResponseDto.SkillMatchDto> analyzeSkillMatches(
            List<String> freelancerSkills, List<String> requiredSkills, List<String> preferredSkills) {
        
        List<JobMatchingResponseDto.SkillMatchDto> matches = new ArrayList<>();
        Set<String> freelancerSkillsLower = freelancerSkills.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
        
        // Check required skills
        if (requiredSkills != null) {
            for (String requiredSkill : requiredSkills) {
                double confidence = calculateSkillMatchConfidence(requiredSkill.toLowerCase(), freelancerSkillsLower);
                if (confidence > 0.3) { // Only include reasonably confident matches
                    matches.add(JobMatchingResponseDto.SkillMatchDto.builder()
                        .skill(requiredSkill)
                        .required(true)
                        .confidence(confidence)
                        .proficiencyLevel(determineProficiencyLevel(confidence))
                        .build());
                }
            }
        }
        
        // Check preferred skills
        if (preferredSkills != null) {
            for (String preferredSkill : preferredSkills) {
                double confidence = calculateSkillMatchConfidence(preferredSkill.toLowerCase(), freelancerSkillsLower);
                if (confidence > 0.3) {
                    matches.add(JobMatchingResponseDto.SkillMatchDto.builder()
                        .skill(preferredSkill)
                        .required(false)
                        .confidence(confidence)
                        .proficiencyLevel(determineProficiencyLevel(confidence))
                        .build());
                }
            }
        }
        
        return matches;
    }
    
    private double calculateSkillMatchConfidence(String requiredSkill, Set<String> freelancerSkills) {
        // Exact match
        if (freelancerSkills.contains(requiredSkill)) {
            return 1.0;
        }
        
        // Partial matches and related skills
        for (String freelancerSkill : freelancerSkills) {
            // Contains match
            if (freelancerSkill.contains(requiredSkill) || requiredSkill.contains(freelancerSkill)) {
                return 0.8;
            }
            
            // Related technology matches
            double relatedness = calculateTechnologyRelatedness(requiredSkill, freelancerSkill);
            if (relatedness > 0.3) {
                return relatedness;
            }
        }
        
        return 0.0;
    }
    
    private double calculateTechnologyRelatedness(String skill1, String skill2) {
        // Define technology groups and relationships
        Map<String, Set<String>> relatedTechnologies = Map.of(
            "javascript", Set.of("typescript", "node.js", "react", "vue.js", "angular", "express"),
            "react", Set.of("javascript", "typescript", "next.js", "redux", "jsx"),
            "python", Set.of("django", "flask", "fastapi", "pandas", "numpy", "tensorflow"),
            "java", Set.of("spring", "spring boot", "hibernate", "maven", "gradle"),
            "aws", Set.of("ec2", "s3", "lambda", "cloudformation", "docker", "kubernetes"),
            "docker", Set.of("kubernetes", "containerization", "devops", "aws", "microservices")
        );
        
        skill1 = skill1.toLowerCase();
        skill2 = skill2.toLowerCase();
        
        for (Map.Entry<String, Set<String>> entry : relatedTechnologies.entrySet()) {
            String mainTech = entry.getKey();
            Set<String> related = entry.getValue();
            
            if ((skill1.contains(mainTech) && related.stream().anyMatch(skill2::contains)) ||
                (skill2.contains(mainTech) && related.stream().anyMatch(skill1::contains))) {
                return 0.6;
            }
        }
        
        return 0.0;
    }
    
    private String determineProficiencyLevel(double confidence) {
        if (confidence >= 0.9) return "Expert";
        if (confidence >= 0.7) return "Advanced";
        if (confidence >= 0.5) return "Intermediate";
        return "Beginner";
    }
    
    private boolean applyFilters(JobMatchingResponseDto.MatchedFreelancerDto freelancer, JobMatchingRequestDto request) {
        // Minimum rating filter
        if (request.getMinRating() != null && 
            (freelancer.getReviewAvg() == null || freelancer.getReviewAvg() < request.getMinRating())) {
            return false;
        }
        
        // Maximum hourly rate filter
        if (request.getMaxHourlyRateCents() != null && 
            freelancer.getHourlyRateCents() != null && 
            freelancer.getHourlyRateCents() > request.getMaxHourlyRateCents()) {
            return false;
        }
        
        // Availability filter
        if (request.getAvailability() != null && 
            !request.getAvailability().equalsIgnoreCase(freelancer.getAvailability())) {
            return false;
        }
        
        // Minimum delivery score filter
        if (request.getMinDeliveryScore() != null && 
            (freelancer.getDeliveryScore() == null || freelancer.getDeliveryScore() < request.getMinDeliveryScore())) {
            return false;
        }
        
        // Location filter (if preferred locations are specified)
        if (request.getPreferredLocations() != null && !request.getPreferredLocations().isEmpty() &&
            freelancer.getLocationText() != null) {
            boolean locationMatch = request.getPreferredLocations().stream()
                .anyMatch(location -> freelancer.getLocationText().toLowerCase().contains(location.toLowerCase()));
            if (!locationMatch) {
                return false;
            }
        }
        
        return true;
    }
    
    private String generateMatchReason(Profile profile, JobMatchingRequestDto request, 
                                     List<JobMatchingResponseDto.SkillMatchDto> skillMatches, double matchScore) {
        StringBuilder reason = new StringBuilder();
        
        // Highlight skill matches
        long requiredSkillMatches = skillMatches.stream()
            .mapToLong(match -> match.getRequired() ? 1 : 0)
            .sum();
        
        if (requiredSkillMatches > 0) {
            reason.append(String.format("Matches %d/%d required skills. ", 
                requiredSkillMatches, 
                request.getRequiredSkills() != null ? request.getRequiredSkills().size() : 0));
        }
        
        // Highlight experience level
        if (profile.getReviewAvg() != null && profile.getReviewAvg().compareTo(BigDecimal.valueOf(4.5)) >= 0) {
            reason.append("Highly rated freelancer. ");
        }
        
        if (profile.getDeliveryScore() != null && profile.getDeliveryScore().compareTo(BigDecimal.valueOf(90)) >= 0) {
            reason.append("Excellent delivery track record. ");
        }
        
        // Mention semantic match
        if (matchScore >= 0.8) {
            reason.append("Strong semantic match with job requirements.");
        } else if (matchScore >= 0.6) {
            reason.append("Good alignment with job requirements.");
        }
        
        return reason.toString().trim();
    }
    
    private JobMatchingResponseDto.FiltersSummaryDto createFiltersSummary(
            JobMatchingRequestDto request, int totalFound, int afterFiltering) {
        
        return JobMatchingResponseDto.FiltersSummaryDto.builder()
            .minSimilarityScore(request.getMinSimilarityScore())
            .minRating(request.getMinRating())
            .maxHourlyRateCents(request.getMaxHourlyRateCents())
            .availability(request.getAvailability())
            .filteredOutCount(totalFound - afterFiltering)
            .totalFreelancersInDatabase((int) profileRepository.count()) // Total freelancers count
            .build();
    }
}