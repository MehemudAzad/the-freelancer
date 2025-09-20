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
    private final JobEmbeddingService jobEmbeddingService;
    private final ProfileRepository profileRepository;
    
    public JobMatchingResponseDto findMatchingFreelancers(JobMatchingRequestDto request) {
        log.info("Finding matching freelancers for job: {} using vector-to-vector similarity", request.getJobId());
        
        try {
            // Method 1: If we have job embedding, use vector-to-vector similarity
            Optional<JobMatchingResponseDto> vectorResult = findMatchingFreelancersUsingJobVector(request);
            if (vectorResult.isPresent()) {
                log.info("Found matches using job vector embedding");
                return vectorResult.get();
            }
            
            // Method 2: Fallback to text-based search if no job vector exists
            log.info("No job vector found, falling back to text-based search");
            return findMatchingFreelancersUsingTextSearch(request);
            
        } catch (Exception e) {
            log.error("Error finding matching freelancers for job {}: {}", request.getJobId(), e.getMessage(), e);
            throw new RuntimeException("Failed to find matching freelancers", e);
        }
    }
    
    /**
     * Find matching freelancers using job vector embedding (preferred method)
     */
    private Optional<JobMatchingResponseDto> findMatchingFreelancersUsingJobVector(JobMatchingRequestDto request) {
        try {
            // Find the job's vector embedding first
            List<Document> jobDocuments = jobEmbeddingService.findAllJobDocuments(request.getJobId());
            
            if (jobDocuments.isEmpty()) {
                log.debug("No job embedding found for job {}", request.getJobId());
                return Optional.empty();
            }
            
            Document jobDoc = jobDocuments.get(0);
            
            // Get job details from metadata to create search query
            String projectName = jobDoc.getMetadata().getOrDefault("projectName", "").toString();
            String description = jobDoc.getMetadata().getOrDefault("description", "").toString();
            String skills = jobDoc.getMetadata().getOrDefault("skills", "").toString();
            
            // Create search query from job metadata
            String jobSearchQuery = createJobSearchQueryFromMetadata(projectName, description, skills);
            
            log.info("Found job embedding for job {}, searching for similar freelancer profiles using: {}", 
                request.getJobId(), jobSearchQuery);
            
            // Use the job's metadata to find similar profile vectors
            List<Document> similarProfiles = profileEmbeddingService.findSimilarProfiles(
                jobSearchQuery, 
                request.getLimit() * 2, // Get more to allow for filtering
                request.getMinSimilarityScore()
            );
            
            log.info("Found {} similar profiles using job vector for job {}", similarProfiles.size(), request.getJobId());
            
            // Process results and create response
            JobMatchingResponseDto response = processProfileMatches(similarProfiles, request, "vector-to-vector", System.currentTimeMillis());
            return Optional.of(response);
            
        } catch (Exception e) {
            log.error("Error in vector-based matching for job {}: {}", request.getJobId(), e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Fallback method using text-based search when no job vector exists
     */
    private JobMatchingResponseDto findMatchingFreelancersUsingTextSearch(JobMatchingRequestDto request) {
        long startTime = System.currentTimeMillis();
        
        // Create search query from job requirements
        String searchQuery = createJobSearchQuery(request);
        log.debug("Created search query for text-based search: {}", searchQuery);
        
        // Find similar profiles using semantic search
        List<Document> similarProfiles = profileEmbeddingService.findSimilarProfiles(
            searchQuery, 
            request.getLimit() * 2, // Get more to allow for filtering
            request.getMinSimilarityScore()
        );
        
        log.info("Found {} similar profiles using text-based search", similarProfiles.size());
        
        return processProfileMatches(similarProfiles, request, "text-to-vector", startTime);
    }
    
    /**
     * Smart matching with hybrid scoring (80% semantic + 20% business metrics)
     * Normalizes semantic scores and combines with business performance indicators
     */
    public JobMatchingResponseDto findMatchingFreelancersWithSmartScoring(JobMatchingRequestDto request) {
        long startTime = System.currentTimeMillis();
        log.info("Finding smart matching freelancers for job: {} with hybrid scoring", request.getJobId());
        
        try {
            // Get initial matches using existing logic
            JobMatchingResponseDto initialResponse = findMatchingFreelancers(request);
            
            if (initialResponse.getMatchedFreelancers() == null || initialResponse.getMatchedFreelancers().isEmpty()) {
                log.info("No initial matches found for smart scoring");
                return initialResponse;
            }
            
            // Apply smart scoring algorithm
            List<JobMatchingResponseDto.MatchedFreelancerDto> smartScoredFreelancers = 
                applySmartScoring(initialResponse.getMatchedFreelancers());
            
            // Sort by new smart score (descending)
            smartScoredFreelancers = smartScoredFreelancers.stream()
                .sorted((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()))
                .collect(Collectors.toList());
            
            double averageScore = smartScoredFreelancers.stream()
                .mapToDouble(JobMatchingResponseDto.MatchedFreelancerDto::getMatchScore)
                .average()
                .orElse(0.0);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            log.info("Applied smart scoring to {} freelancers, new average score: {:.2f}", 
                    smartScoredFreelancers.size(), averageScore);
            
            return JobMatchingResponseDto.builder()
                .jobId(request.getJobId())
                .matchedFreelancers(smartScoredFreelancers)
                .totalMatches(smartScoredFreelancers.size())
                .averageMatchScore(averageScore)
                .searchQuery("smart hybrid scoring (80% semantic + 20% business)")
                .processingTimeMs(processingTime)
                .appliedFilters(initialResponse.getAppliedFilters())
                .build();
                
        } catch (Exception e) {
            log.error("Error in smart matching for job {}: {}", request.getJobId(), e.getMessage(), e);
            throw new RuntimeException("Failed to find smart matching freelancers", e);
        }
    }
    
    /**
     * Apply smart scoring algorithm: 80% normalized semantic + 20% business metrics
     */
    private List<JobMatchingResponseDto.MatchedFreelancerDto> applySmartScoring(
            List<JobMatchingResponseDto.MatchedFreelancerDto> freelancers) {
        
        if (freelancers.isEmpty()) {
            return freelancers;
        }
        
        // Find min and max semantic scores for normalization
        double minSemanticScore = freelancers.stream()
            .mapToDouble(JobMatchingResponseDto.MatchedFreelancerDto::getMatchScore)
            .min()
            .orElse(0.0);
        
        double maxSemanticScore = freelancers.stream()
            .mapToDouble(JobMatchingResponseDto.MatchedFreelancerDto::getMatchScore)
            .max()
            .orElse(1.0);
        
        log.debug("Semantic score range: {:.3f} to {:.3f}", minSemanticScore, maxSemanticScore);
        
        // Apply smart scoring to each freelancer
        return freelancers.stream()
            .map(freelancer -> applySmartScoringToFreelancer(freelancer, minSemanticScore, maxSemanticScore))
            .collect(Collectors.toList());
    }
    
    /**
     * Apply smart scoring to individual freelancer
     */
    private JobMatchingResponseDto.MatchedFreelancerDto applySmartScoringToFreelancer(
            JobMatchingResponseDto.MatchedFreelancerDto freelancer, double minScore, double maxScore) {
        
        // 1. Normalize semantic score to 0-100%
        double range = maxScore - minScore;
        double normalizedSemantic = range > 0 ? ((freelancer.getMatchScore() - minScore) / range) * 100.0 : 100.0;
        
        // 2. Calculate business metrics (0-100 scale)
        double businessScore = calculateBusinessMetricsScore(freelancer);
        
        // 3. Apply hybrid formula: 80% semantic + 20% business
        double smartScore = (normalizedSemantic * 0.8) + (businessScore * 0.2);
        
        log.debug("Freelancer {}: semantic={:.1f}% -> {:.1f}%, business={:.1f}%, final={:.1f}%",
                freelancer.getUserId(), freelancer.getMatchScore() * 100, normalizedSemantic, businessScore, smartScore);
        
        // Return new freelancer with updated smart score
        return JobMatchingResponseDto.MatchedFreelancerDto.builder()
            .userId(freelancer.getUserId())
            .headline(freelancer.getHeadline())
            .bio(freelancer.getBio())
            .skills(freelancer.getSkills())
            .matchScore(smartScore / 100.0) // Convert back to 0-1 scale for consistency
            .hourlyRateCents(freelancer.getHourlyRateCents())
            .currency(freelancer.getCurrency())
            .locationText(freelancer.getLocationText())
            .availability(freelancer.getAvailability())
            .reviewAvg(freelancer.getReviewAvg())
            .reviewsCount(freelancer.getReviewsCount())
            .deliveryScore(freelancer.getDeliveryScore())
            .profilePictureUrl(freelancer.getProfilePictureUrl())
            .websiteUrl(freelancer.getWebsiteUrl())
            .linkedinUrl(freelancer.getLinkedinUrl())
            .githubUsername(freelancer.getGithubUsername())
            .skillMatches(freelancer.getSkillMatches())
            .matchReason(freelancer.getMatchReason())
            .build();
    }
    
    /**
     * Calculate business metrics score (0-100)
     * Combines availability, reviewAvg, reviewsCount, and deliveryScore
     */
    private double calculateBusinessMetricsScore(JobMatchingResponseDto.MatchedFreelancerDto freelancer) {
        double availabilityScore = calculateAvailabilityScore(freelancer.getAvailability());
        double reviewScore = calculateReviewScore(freelancer.getReviewAvg(), freelancer.getReviewsCount());
        double deliveryScore = normalizeDeliveryScore(freelancer.getDeliveryScore());
        
        // Weight the business metrics: 40% availability + 35% reviews + 25% delivery
        return (availabilityScore * 0.4) + (reviewScore * 0.35) + (deliveryScore * 0.25);
    }
    
    /**
     * Calculate availability score (0-100)
     */
    private double calculateAvailabilityScore(String availability) {
        if (availability == null) return 50.0;
        
        return switch (availability.toUpperCase()) {
            case "FULL_TIME" -> 100.0;
            case "PART_TIME" -> 75.0;
            case "OCCASIONAL" -> 50.0;
            case "UNAVAILABLE" -> 0.0;
            default -> 50.0;
        };
    }
    
    /**
     * Calculate review score with confidence weighting (0-100)
     * Formula: (reviewAvg/5.0 * 100) weighted by review count confidence
     */
    private double calculateReviewScore(Double reviewAvg, Integer reviewsCount) {
        if (reviewAvg == null || reviewsCount == null || reviewsCount == 0) {
            return 50.0; // Neutral score for new freelancers
        }
        
        // Normalize rating to 0-100
        double ratingScore = (reviewAvg / 5.0) * 100.0;
        
        // Apply confidence factor based on review count
        double confidenceFactor = Math.min(1.0, Math.log(reviewsCount + 1) / Math.log(21)); // Logarithmic confidence
        double neutralScore = 50.0;
        
        return neutralScore + ((ratingScore - neutralScore) * confidenceFactor);
    }
    
    /**
     * Normalize delivery score (0-100)
     */
    private double normalizeDeliveryScore(Double deliveryScore) {
        if (deliveryScore == null) {
            return 50.0; // Neutral score for new freelancers
        }
        return Math.min(100.0, Math.max(0.0, deliveryScore));
    }
    
    /**
     * Common method to process profile matches and create response
     */
    private JobMatchingResponseDto processProfileMatches(List<Document> similarProfiles, 
                                                        JobMatchingRequestDto request, 
                                                        String searchMethod,
                                                        long startTime) {
        
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
        
        log.info("Successfully matched {} freelancers for job {} in {}ms using {}", 
                matchedFreelancers.size(), request.getJobId(), processingTime, searchMethod);
        
        return JobMatchingResponseDto.builder()
            .jobId(request.getJobId())
            .matchedFreelancers(matchedFreelancers)
            .totalMatches(matchedFreelancers.size())
            .averageMatchScore(averageScore)
            .searchQuery(searchMethod + " similarity search")
            .processingTimeMs(processingTime)
            .appliedFilters(filtersSummary)
            .build();
    }
    
    /**
     * Create search query from job metadata (for vector-to-vector matching)
     */
    private String createJobSearchQueryFromMetadata(String projectName, String description, String skills) {
        StringBuilder query = new StringBuilder();
        
        if (projectName != null && !projectName.trim().isEmpty()) {
            query.append(projectName.trim()).append(" ");
        }
        
        if (description != null && !description.trim().isEmpty()) {
            query.append(description.trim()).append(" ");
        }
        
        if (skills != null && !skills.trim().isEmpty()) {
            query.append("Skills: ").append(skills.trim()).append(" ");
        }
        
        String finalQuery = query.toString().trim();
        
        // Fallback: if query is empty, use a default search
        if (finalQuery.isEmpty()) {
            finalQuery = "general freelancer software development";
            log.warn("Empty job metadata, using fallback query: '{}'", finalQuery);
        }
        
        return finalQuery;
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
        
        String finalQuery = query.toString().trim();
        
        // Fallback: if query is empty, use a default search
        if (finalQuery.isEmpty()) {
            finalQuery = "general freelancer software development";
            log.warn("Empty search query for job {}, using fallback: '{}'", request.getJobId(), finalQuery);
        }
        
        return finalQuery;
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