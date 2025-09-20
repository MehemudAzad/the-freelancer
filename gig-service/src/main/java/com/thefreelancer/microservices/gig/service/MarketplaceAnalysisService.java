package com.thefreelancer.microservices.gig.service;

import com.thefreelancer.microservices.gig.dto.MarketAnalysisRequestDto;
import com.thefreelancer.microservices.gig.dto.MarketAnalysisResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Marketplace Intelligence Service
 * Analyzes supply/demand patterns using vector embeddings
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MarketplaceAnalysisService {
    
    private final VectorStore vectorStore;
    private final QueryEnhancementService queryEnhancementService;
    
    /**
     * Comprehensive market analysis for a skill/category
     */
    public MarketAnalysisResponseDto analyzeMarket(MarketAnalysisRequestDto request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Analyzing marketplace for: {} with sample size: {}", 
                    request.getSkillOrCategory(), request.getSampleSize());
            
            // Enhance the search query
            String enhancedQuery = queryEnhancementService.enhanceGigQuery(request.getSkillOrCategory());
            
            // Analyze demand (jobs)
            MarketAnalysisResponseDto.DemandAnalysis demandAnalysis = analyzeDemand(enhancedQuery, request);
            
            // Analyze supply (gigs + profiles)
            MarketAnalysisResponseDto.SupplyAnalysis supplyAnalysis = analyzeSupply(enhancedQuery, request);
            
            // Calculate market metrics
            MarketAnalysisResponseDto.MarketMetrics marketMetrics = calculateMarketMetrics(
                demandAnalysis, supplyAnalysis, request
            );
            
            // Generate recommendations
            MarketAnalysisResponseDto.Recommendations recommendations = generateRecommendations(
                demandAnalysis, supplyAnalysis, marketMetrics, request
            );
            
            // Competitive landscape
            MarketAnalysisResponseDto.CompetitiveLandscape competitive = analyzeCompetition(enhancedQuery, request);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            log.info("Market analysis completed for '{}' in {}ms - Jobs: {}, Gigs: {}, Freelancers: {}", 
                    request.getSkillOrCategory(), processingTime,
                    demandAnalysis.getTotalJobsFound(),
                    supplyAnalysis.getTotalGigsFound(),
                    supplyAnalysis.getTotalFreelancersFound());
            
            return MarketAnalysisResponseDto.builder()
                .query(request.getSkillOrCategory())
                .enhancedQuery(enhancedQuery)
                .demandAnalysis(demandAnalysis)
                .supplyAnalysis(supplyAnalysis)
                .marketMetrics(marketMetrics)
                .recommendations(recommendations)
                .competitiveLandscape(competitive)
                .analysisDate(LocalDateTime.now())
                .processingTimeMs(processingTime)
                .build();
                
        } catch (Exception e) {
            log.error("Error analyzing marketplace for {}: {}", request.getSkillOrCategory(), e.getMessage(), e);
            throw new RuntimeException("Failed to analyze marketplace", e);
        }
    }
    
    /**
     * Analyze demand patterns from job embeddings
     */
    private MarketAnalysisResponseDto.DemandAnalysis analyzeDemand(String query, MarketAnalysisRequestDto request) {
        log.debug("Analyzing demand patterns for query: {}", query);
        
        // Search job embeddings
        SearchRequest jobSearchRequest = SearchRequest.builder()
            .query(query)
            .topK(request.getSampleSize())
            .similarityThreshold(request.getMinSimilarityThreshold())
            .filterExpression(new FilterExpressionBuilder()
                .eq("documentType", "job")
                .build())
            .build();
        
        List<Document> jobDocuments = vectorStore.similaritySearch(jobSearchRequest);
        
        // Extract job metadata and analyze patterns
        List<Map<String, Object>> jobData = jobDocuments.stream()
            .map(doc -> doc.getMetadata())
            .collect(Collectors.toList());
        
        // Calculate demand metrics
        int totalJobs = jobData.size();
        double avgBudget = calculateAverageBudget(jobData);
        Map<String, Long> skillDemand = analyzeSkillDemand(jobData);
        Map<String, Long> categoryDistribution = analyzeCategoryDistribution(jobData);
        Map<String, Integer> urgencyLevels = analyzeUrgencyLevels(jobData);
        Map<String, Integer> demandTrends = analyzeDemandTrends(jobData);
        
        log.debug("Demand analysis - Total jobs: {}, Avg budget: ${:.2f}", 
                totalJobs, avgBudget / 100.0);
        
        return MarketAnalysisResponseDto.DemandAnalysis.builder()
            .totalJobsFound(totalJobs)
            .averageBudget(avgBudget)
            .skillDemandBreakdown(skillDemand)
            .categoryDistribution(categoryDistribution)
            .urgencyLevels(urgencyLevels)
            .demandTrends(demandTrends)
            .topRequiredSkills(getTopSkills(skillDemand, 10))
            .budgetRanges(analyzeBudgetRanges(jobData))
            .build();
    }
    
    /**
     * Analyze supply patterns from gig and profile embeddings
     */
    private MarketAnalysisResponseDto.SupplyAnalysis analyzeSupply(String query, MarketAnalysisRequestDto request) {
        log.debug("Analyzing supply patterns for query: {}", query);
        
        // Search gig embeddings
        SearchRequest gigSearchRequest = SearchRequest.builder()
            .query(query)
            .topK(request.getSampleSize())
            .similarityThreshold(request.getMinSimilarityThreshold())
            .filterExpression(new FilterExpressionBuilder()
                .eq("documentType", "gig")
                .build())
            .build();
        
        List<Document> gigDocuments = vectorStore.similaritySearch(gigSearchRequest);
        
        // Search profile embeddings
        SearchRequest profileSearchRequest = SearchRequest.builder()
            .query(query)
            .topK(request.getSampleSize())
            .similarityThreshold(request.getMinSimilarityThreshold())
            .filterExpression(new FilterExpressionBuilder()
                .eq("documentType", "profile")
                .build())
            .build();
        
        List<Document> profileDocuments = vectorStore.similaritySearch(profileSearchRequest);
        
        // Analyze supply data
        List<Map<String, Object>> gigData = gigDocuments.stream()
            .map(doc -> doc.getMetadata())
            .collect(Collectors.toList());
        
        List<Map<String, Object>> profileData = profileDocuments.stream()
            .map(doc -> doc.getMetadata())
            .collect(Collectors.toList());
        
        // Calculate supply metrics
        int totalGigs = gigData.size();
        int totalFreelancers = profileData.size();
        double avgGigPrice = calculateAverageGigPrice(gigData);
        double avgFreelancerRate = calculateAverageFreelancerRate(profileData);
        
        Map<String, Long> skillSupply = analyzeSkillSupply(gigData, profileData);
        Map<String, Double> experienceLevels = analyzeExperienceLevels(profileData);
        Map<String, Integer> availabilityDistribution = analyzeAvailability(profileData);
        
        log.debug("Supply analysis - Gigs: {}, Freelancers: {}, Avg gig price: ${:.2f}", 
                totalGigs, totalFreelancers, avgGigPrice / 100.0);
        
        return MarketAnalysisResponseDto.SupplyAnalysis.builder()
            .totalGigsFound(totalGigs)
            .totalFreelancersFound(totalFreelancers)
            .averageGigPrice(avgGigPrice)
            .averageFreelancerHourlyRate(avgFreelancerRate)
            .skillSupplyBreakdown(skillSupply)
            .experienceLevels(experienceLevels)
            .availabilityDistribution(availabilityDistribution)
            .topOfferedSkills(getTopSkills(skillSupply, 10))
            .priceRanges(analyzeGigPriceRanges(gigData))
            .qualityMetrics(analyzeQualityMetrics(gigData, profileData))
            .build();
    }
    
    /**
     * Calculate key market metrics and ratios
     */
    private MarketAnalysisResponseDto.MarketMetrics calculateMarketMetrics(
            MarketAnalysisResponseDto.DemandAnalysis demand,
            MarketAnalysisResponseDto.SupplyAnalysis supply,
            MarketAnalysisRequestDto request) {
        
        log.debug("Calculating market metrics");
        
        // Supply/Demand Ratio
        double supplyDemandRatio = demand.getTotalJobsFound() > 0 ? 
            (double) supply.getTotalFreelancersFound() / demand.getTotalJobsFound() : 0.0;
        
        // Market Health Score (0-100)
        double marketHealthScore = calculateMarketHealthScore(demand, supply, supplyDemandRatio);
        
        // Competition Index (0-100) - higher means more competitive
        double competitionIndex = calculateCompetitionIndex(supply, demand);
        
        // Opportunity Score (0-100) - higher means better opportunities
        double opportunityScore = calculateOpportunityScore(demand, supply, supplyDemandRatio);
        
        // Price Gap Analysis
        double priceGap = demand.getAverageBudget() - supply.getAverageGigPrice();
        double priceGapPercentage = supply.getAverageGigPrice() > 0 ? 
            (priceGap / supply.getAverageGigPrice()) * 100 : 0.0;
        
        return MarketAnalysisResponseDto.MarketMetrics.builder()
            .supplyDemandRatio(round(supplyDemandRatio, 2))
            .marketHealthScore(round(marketHealthScore, 1))
            .competitionIndex(round(competitionIndex, 1))
            .opportunityScore(round(opportunityScore, 1))
            .priceGap(round(priceGap, 2))
            .priceGapPercentage(round(priceGapPercentage, 1))
            .marketMaturity(determineMarketMaturity(demand, supply))
            .growthPotential(calculateGrowthPotential(demand, supply))
            .build();
    }
    
    /**
     * Generate actionable recommendations
     */
    private MarketAnalysisResponseDto.Recommendations generateRecommendations(
            MarketAnalysisResponseDto.DemandAnalysis demand,
            MarketAnalysisResponseDto.SupplyAnalysis supply,
            MarketAnalysisResponseDto.MarketMetrics metrics,
            MarketAnalysisRequestDto request) {
        
        log.debug("Generating marketplace recommendations");
        
        List<String> forFreelancers = new ArrayList<>();
        List<String> forClients = new ArrayList<>();
        List<String> forPlatform = new ArrayList<>();
        
        // Freelancer recommendations
        if (metrics.getSupplyDemandRatio() < 0.5) {
            forFreelancers.add("🚀 HIGH OPPORTUNITY: Low competition, high demand. Great time to enter this market!");
        } else if (metrics.getSupplyDemandRatio() > 2.0) {
            forFreelancers.add("⚠️ SATURATED MARKET: High competition. Consider specializing or niche positioning.");
        }
        
        if (metrics.getPriceGap() > 0) {
            forFreelancers.add(String.format("💰 PRICING OPPORTUNITY: Clients budget $%.2f more than average gig price. Consider premium positioning.", 
                    metrics.getPriceGap() / 100.0));
        }
        
        if (metrics.getOpportunityScore() > 70) {
            forFreelancers.add("📈 MARKET TIMING: High opportunity score indicates favorable conditions for growth.");
        }
        
        // Client recommendations
        if (supply.getTotalFreelancersFound() > demand.getTotalJobsFound()) {
            forClients.add("👥 BUYER'S MARKET: Many qualified freelancers available. You have good negotiating power.");
        }
        
        if (metrics.getCompetitionIndex() > 70) {
            forClients.add("💎 QUALITY OPTIONS: High competition means many quality freelancers to choose from.");
        }
        
        if (metrics.getPriceGap() < 0) {
            forClients.add(String.format("💡 BUDGET INSIGHT: Consider increasing budget by $%.2f to access more freelancers.", 
                    Math.abs(metrics.getPriceGap()) / 100.0));
        }
        
        // Platform recommendations
        if (metrics.getMarketHealthScore() < 50) {
            forPlatform.add("📈 GROWTH NEEDED: Consider incentives to attract more " + 
                (metrics.getSupplyDemandRatio() < 1 ? "freelancers" : "clients"));
        }
        
        if (metrics.getSupplyDemandRatio() > 3.0) {
            forPlatform.add("🎯 DEMAND GENERATION: High freelancer supply. Focus on attracting more client projects.");
        } else if (metrics.getSupplyDemandRatio() < 0.3) {
            forPlatform.add("👨‍💻 SUPPLY EXPANSION: High client demand. Focus on recruiting more freelancers.");
        }
        
        return MarketAnalysisResponseDto.Recommendations.builder()
            .forFreelancers(forFreelancers)
            .forClients(forClients)
            .forPlatform(forPlatform)
            .optimalPricing(calculateOptimalPricing(demand, supply))
            .marketEntryStrategy(generateMarketEntryStrategy(metrics))
            .build();
    }
    
    /**
     * Analyze competitive landscape
     */
    private MarketAnalysisResponseDto.CompetitiveLandscape analyzeCompetition(String query, MarketAnalysisRequestDto request) {
        log.debug("Analyzing competitive landscape");
        
        // Get top profiles in this space
        SearchRequest competitorSearch = SearchRequest.builder()
            .query(query)
            .topK(50)
            .similarityThreshold(0.4)
            .filterExpression(new FilterExpressionBuilder()
                .eq("documentType", "profile")
                .build())
            .build();
        
        List<Document> competitors = vectorStore.similaritySearch(competitorSearch);
        
        // Analyze competitor data
        Map<String, Object> competitorAnalysis = analyzeCompetitors(competitors);
        
        @SuppressWarnings("unchecked")
        Map<String, Long> topSkills = (Map<String, Long>) competitorAnalysis.getOrDefault("topSkills", new HashMap<String, Long>());
        
        return MarketAnalysisResponseDto.CompetitiveLandscape.builder()
            .totalCompetitors(competitors.size())
            .topCompetitorSkills(topSkills)
            .averageCompetitorRating((Double) competitorAnalysis.getOrDefault("avgRating", 0.0))
            .competitionIntensity(calculateCompetitionIntensity(competitors))
            .marketLeaders(getMarketLeaders(competitors))
            .skillGaps(identifySkillGaps(competitors, query))
            .build();
    }
    
    // Helper methods for calculations
    private double calculateAverageBudget(List<Map<String, Object>> jobData) {
        return jobData.stream()
            .filter(job -> job.containsKey("budgetCents") && job.get("budgetCents") != null)
            .mapToDouble(job -> {
                Object budget = job.get("budgetCents");
                return ((Number) budget).doubleValue();
            })
            .average()
            .orElse(0.0);
    }
    
    private Map<String, Long> analyzeSkillDemand(List<Map<String, Object>> jobData) {
        return jobData.stream()
            .filter(job -> job.containsKey("skills") && job.get("skills") != null)
            .map(job -> (String) job.get("skills"))
            .filter(Objects::nonNull)
            .flatMap(skills -> Arrays.stream(skills.split(",")))
            .map(String::trim)
            .filter(skill -> !skill.isEmpty())
            .collect(Collectors.groupingBy(
                skill -> skill.toLowerCase(),
                Collectors.counting()
            ));
    }
    
    private Map<String, Long> analyzeCategoryDistribution(List<Map<String, Object>> jobData) {
        return jobData.stream()
            .filter(job -> job.containsKey("category") && job.get("category") != null)
            .map(job -> (String) job.get("category"))
            .collect(Collectors.groupingBy(
                category -> category,
                Collectors.counting()
            ));
    }
    
    private Map<String, Integer> analyzeUrgencyLevels(List<Map<String, Object>> jobData) {
        Map<String, Integer> urgency = new HashMap<>();
        urgency.put("Low", 0);
        urgency.put("Medium", 0);
        urgency.put("High", 0);
        urgency.put("Urgent", 0);
        
        // Placeholder logic - can be enhanced based on job metadata
        int total = jobData.size();
        urgency.put("Medium", (int) (total * 0.6));
        urgency.put("High", (int) (total * 0.3));
        urgency.put("Low", (int) (total * 0.1));
        
        return urgency;
    }
    
    private Map<String, Integer> analyzeDemandTrends(List<Map<String, Object>> jobData) {
        // Placeholder implementation - would analyze createdAt timestamps
        Map<String, Integer> trends = new HashMap<>();
        trends.put("Last 7 days", jobData.size() / 4);
        trends.put("Last 30 days", jobData.size());
        trends.put("Growth rate", 15); // 15% growth
        return trends;
    }
    
    private Map<String, Integer> analyzeBudgetRanges(List<Map<String, Object>> jobData) {
        Map<String, Integer> ranges = new HashMap<>();
        ranges.put("Under $500", 0);
        ranges.put("$500-$2000", 0);
        ranges.put("$2000-$5000", 0);
        ranges.put("$5000+", 0);
        
        jobData.stream()
            .filter(job -> job.containsKey("budgetCents") && job.get("budgetCents") != null)
            .mapToDouble(job -> ((Number) job.get("budgetCents")).doubleValue() / 100.0)
            .forEach(budget -> {
                if (budget < 500) ranges.merge("Under $500", 1, Integer::sum);
                else if (budget < 2000) ranges.merge("$500-$2000", 1, Integer::sum);
                else if (budget < 5000) ranges.merge("$2000-$5000", 1, Integer::sum);
                else ranges.merge("$5000+", 1, Integer::sum);
            });
        
        return ranges;
    }
    
    private double calculateAverageGigPrice(List<Map<String, Object>> gigData) {
        return gigData.stream()
            .filter(gig -> gig.containsKey("priceCents") && gig.get("priceCents") != null)
            .mapToDouble(gig -> ((Number) gig.get("priceCents")).doubleValue())
            .average()
            .orElse(0.0);
    }
    
    private double calculateAverageFreelancerRate(List<Map<String, Object>> profileData) {
        return profileData.stream()
            .filter(profile -> profile.containsKey("hourlyRateCents") && profile.get("hourlyRateCents") != null)
            .mapToDouble(profile -> ((Number) profile.get("hourlyRateCents")).doubleValue())
            .average()
            .orElse(0.0);
    }
    
    private Map<String, Long> analyzeSkillSupply(List<Map<String, Object>> gigData, List<Map<String, Object>> profileData) {
        Map<String, Long> skillSupply = new HashMap<>();
        
        // Analyze gig skills
        gigData.stream()
            .filter(gig -> gig.containsKey("tags") && gig.get("tags") != null)
            .map(gig -> (String) gig.get("tags"))
            .filter(Objects::nonNull)
            .flatMap(tags -> Arrays.stream(tags.split(",")))
            .map(String::trim)
            .filter(skill -> !skill.isEmpty())
            .forEach(skill -> skillSupply.merge(skill.toLowerCase(), 1L, Long::sum));
        
        // Analyze profile skills
        profileData.stream()
            .filter(profile -> profile.containsKey("skills") && profile.get("skills") != null)
            .map(profile -> (String) profile.get("skills"))
            .filter(Objects::nonNull)
            .flatMap(skills -> Arrays.stream(skills.split(",")))
            .map(String::trim)
            .filter(skill -> !skill.isEmpty())
            .forEach(skill -> skillSupply.merge(skill.toLowerCase(), 1L, Long::sum));
        
        return skillSupply;
    }
    
    private Map<String, Double> analyzeExperienceLevels(List<Map<String, Object>> profileData) {
        Map<String, Double> experience = new HashMap<>();
        experience.put("Junior", 0.3);
        experience.put("Mid-level", 0.5);
        experience.put("Senior", 0.2);
        return experience;
    }
    
    private Map<String, Integer> analyzeAvailability(List<Map<String, Object>> profileData) {
        Map<String, Integer> availability = new HashMap<>();
        availability.put("FULL_TIME", 0);
        availability.put("PART_TIME", 0);
        availability.put("OCCASIONAL", 0);
        availability.put("UNAVAILABLE", 0);
        
        profileData.stream()
            .filter(profile -> profile.containsKey("availability"))
            .map(profile -> (String) profile.get("availability"))
            .forEach(avail -> {
                if (avail != null) {
                    availability.merge(avail, 1, Integer::sum);
                }
            });
        
        return availability;
    }
    
    private List<String> getTopSkills(Map<String, Long> skills, int limit) {
        return skills.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    private Map<String, Integer> analyzeGigPriceRanges(List<Map<String, Object>> gigData) {
        Map<String, Integer> ranges = new HashMap<>();
        ranges.put("Under $100", 0);
        ranges.put("$100-$500", 0);
        ranges.put("$500-$2000", 0);
        ranges.put("$2000+", 0);
        
        gigData.stream()
            .filter(gig -> gig.containsKey("priceCents") && gig.get("priceCents") != null)
            .mapToDouble(gig -> ((Number) gig.get("priceCents")).doubleValue() / 100.0)
            .forEach(price -> {
                if (price < 100) ranges.merge("Under $100", 1, Integer::sum);
                else if (price < 500) ranges.merge("$100-$500", 1, Integer::sum);
                else if (price < 2000) ranges.merge("$500-$2000", 1, Integer::sum);
                else ranges.merge("$2000+", 1, Integer::sum);
            });
        
        return ranges;
    }
    
    private Map<String, Object> analyzeQualityMetrics(List<Map<String, Object>> gigData, List<Map<String, Object>> profileData) {
        Map<String, Object> quality = new HashMap<>();
        
        double avgGigRating = gigData.stream()
            .filter(gig -> gig.containsKey("reviewAvg") && gig.get("reviewAvg") != null)
            .mapToDouble(gig -> ((Number) gig.get("reviewAvg")).doubleValue())
            .average()
            .orElse(0.0);
        
        double avgProfileRating = profileData.stream()
            .filter(profile -> profile.containsKey("reviewAvg") && profile.get("reviewAvg") != null)
            .mapToDouble(profile -> ((Number) profile.get("reviewAvg")).doubleValue())
            .average()
            .orElse(0.0);
        
        quality.put("averageGigRating", round(avgGigRating, 2));
        quality.put("averageProfileRating", round(avgProfileRating, 2));
        quality.put("qualityIndex", round((avgGigRating + avgProfileRating) / 2.0 * 20, 1)); // Convert to 0-100 scale
        
        return quality;
    }
    
    private double calculateMarketHealthScore(
            MarketAnalysisResponseDto.DemandAnalysis demand,
            MarketAnalysisResponseDto.SupplyAnalysis supply,
            double supplyDemandRatio) {
        
        // Ideal ratio is around 1.0-1.5 (slightly more supply than demand)
        double ratioScore = 100.0 - Math.abs(supplyDemandRatio - 1.2) * 50;
        ratioScore = Math.max(0, Math.min(100, ratioScore));
        
        // Factor in volume (more activity = healthier market)
        double volumeScore = Math.min(100, (demand.getTotalJobsFound() + supply.getTotalGigsFound()) / 20.0 * 100);
        
        return (ratioScore * 0.7) + (volumeScore * 0.3);
    }
    
    private double calculateCompetitionIndex(
            MarketAnalysisResponseDto.SupplyAnalysis supply,
            MarketAnalysisResponseDto.DemandAnalysis demand) {
        
        if (demand.getTotalJobsFound() == 0) return 100.0;
        
        double freelancerToJobRatio = (double) supply.getTotalFreelancersFound() / demand.getTotalJobsFound();
        return Math.min(100.0, freelancerToJobRatio * 50);
    }
    
    private double calculateOpportunityScore(
            MarketAnalysisResponseDto.DemandAnalysis demand,
            MarketAnalysisResponseDto.SupplyAnalysis supply,
            double supplyDemandRatio) {
        
        // Base score on market balance
        double balanceScore = supplyDemandRatio < 1.5 ? 80.0 : Math.max(20.0, 80.0 - (supplyDemandRatio - 1.5) * 20);
        
        // Factor in market size
        double sizeScore = Math.min(20.0, (demand.getTotalJobsFound() + supply.getTotalGigsFound()) / 10.0);
        
        return balanceScore + sizeScore;
    }
    
    private String determineMarketMaturity(
            MarketAnalysisResponseDto.DemandAnalysis demand,
            MarketAnalysisResponseDto.SupplyAnalysis supply) {
        
        int totalActivity = demand.getTotalJobsFound() + supply.getTotalGigsFound() + supply.getTotalFreelancersFound();
        
        if (totalActivity < 20) return "Emerging";
        else if (totalActivity < 100) return "Growing";
        else if (totalActivity < 300) return "Established";
        else return "Mature";
    }
    
    private String calculateGrowthPotential(
            MarketAnalysisResponseDto.DemandAnalysis demand,
            MarketAnalysisResponseDto.SupplyAnalysis supply) {
        
        double demandSupplyBalance = demand.getTotalJobsFound() > 0 ? 
            (double) supply.getTotalFreelancersFound() / demand.getTotalJobsFound() : 0.0;
        
        if (demandSupplyBalance < 0.5) return "Very High";
        else if (demandSupplyBalance < 1.0) return "High";
        else if (demandSupplyBalance < 2.0) return "Moderate";
        else return "Limited";
    }
    
    private Map<String, Object> calculateOptimalPricing(
            MarketAnalysisResponseDto.DemandAnalysis demand,
            MarketAnalysisResponseDto.SupplyAnalysis supply) {
        
        Map<String, Object> pricing = new HashMap<>();
        
        double suggestedRate = (demand.getAverageBudget() + supply.getAverageGigPrice()) / 2.0;
        pricing.put("suggestedHourlyRate", round(suggestedRate / 100.0, 2));
        pricing.put("marketRange", Map.of(
            "min", round(supply.getAverageGigPrice() * 0.8 / 100.0, 2),
            "max", round(demand.getAverageBudget() * 1.2 / 100.0, 2)
        ));
        
        return pricing;
    }
    
    private List<String> generateMarketEntryStrategy(MarketAnalysisResponseDto.MarketMetrics metrics) {
        List<String> strategy = new ArrayList<>();
        
        if (metrics.getCompetitionIndex() > 70) {
            strategy.add("Focus on niche specialization to differentiate");
            strategy.add("Build strong portfolio showcasing unique value");
        } else {
            strategy.add("Capitalize on low competition with aggressive positioning");
            strategy.add("Build market presence quickly");
        }
        
        if (metrics.getOpportunityScore() > 75) {
            strategy.add("High opportunity market - consider premium pricing");
        } else {
            strategy.add("Competitive pricing recommended for market entry");
        }
        
        return strategy;
    }
    
    private Map<String, Object> analyzeCompetitors(List<Document> competitors) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Extract competitor skills
        Map<String, Long> competitorSkills = competitors.stream()
            .map(doc -> doc.getMetadata())
            .filter(meta -> meta.containsKey("skills") && meta.get("skills") != null)
            .map(meta -> (String) meta.get("skills"))
            .flatMap(skills -> Arrays.stream(skills.split(",")))
            .map(String::trim)
            .filter(skill -> !skill.isEmpty())
            .collect(Collectors.groupingBy(
                skill -> skill.toLowerCase(),
                Collectors.counting()
            ));
        
        // Calculate average rating
        double avgRating = competitors.stream()
            .map(doc -> doc.getMetadata())
            .filter(meta -> meta.containsKey("reviewAvg") && meta.get("reviewAvg") != null)
            .mapToDouble(meta -> ((Number) meta.get("reviewAvg")).doubleValue())
            .average()
            .orElse(0.0);
        
        analysis.put("topSkills", competitorSkills);
        analysis.put("avgRating", round(avgRating, 2));
        
        return analysis;
    }
    
    private double calculateCompetitionIntensity(List<Document> competitors) {
        // Base intensity on number of competitors and their quality metrics
        double baseIntensity = Math.min(100.0, competitors.size() * 2.0);
        
        // Adjust based on competitor quality (higher average ratings = more intense competition)
        double avgRating = competitors.stream()
            .map(doc -> doc.getMetadata())
            .filter(meta -> meta.containsKey("reviewAvg") && meta.get("reviewAvg") != null)
            .mapToDouble(meta -> ((Number) meta.get("reviewAvg")).doubleValue())
            .average()
            .orElse(3.0);
        
        double qualityMultiplier = avgRating / 5.0; // Convert to 0-1 scale
        
        return Math.min(100.0, baseIntensity * (0.7 + 0.3 * qualityMultiplier));
    }
    
    private List<Map<String, Object>> getMarketLeaders(List<Document> competitors) {
        return competitors.stream()
            .limit(5) // Top 5 competitors
            .map(doc -> {
                Map<String, Object> leader = new HashMap<>();
                Map<String, Object> meta = doc.getMetadata();
                
                leader.put("profileId", meta.get("profileId"));
                leader.put("rating", meta.getOrDefault("reviewAvg", 0.0));
                leader.put("reviewCount", meta.getOrDefault("reviewsCount", 0));
                Object distance = doc.getMetadata().getOrDefault("distance", 0.0);
                leader.put("similarityScore", round(distance instanceof Number ? ((Number) distance).doubleValue() : 0.0, 3));
                
                return leader;
            })
            .collect(Collectors.toList());
    }
    
    private List<String> identifySkillGaps(List<Document> competitors, String query) {
        // Placeholder implementation - would analyze missing skills in high-demand but low-supply areas
        List<String> gaps = new ArrayList<>();
        gaps.add("Advanced " + query.split(" ")[0] + " optimization");
        gaps.add("Enterprise-level " + query.split(" ")[0] + " solutions");
        gaps.add("Industry-specific " + query.split(" ")[0] + " expertise");
        return gaps;
    }
    
    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}