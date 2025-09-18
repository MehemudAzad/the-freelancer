package com.thefreelancer.microservices.gig.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Pattern;

@Service
@Slf4j
public class QueryEnhancementService {
    
    private static final Map<String, String> SKILL_EXPANSIONS = Map.of(
        "react", "react reactjs next.js jsx frontend web development",
        "node", "node nodejs express javascript backend api",
        "python", "python django flask fastapi backend development",
        "design", "design ui ux graphic visual branding logo",
        "mobile", "mobile app ios android react-native flutter",
        "wordpress", "wordpress wp cms website blog customization",
        "ecommerce", "ecommerce e-commerce store shop online selling",
        "writing", "writing content copywriting blog articles",
        "marketing", "marketing digital seo social media advertising",
        "video", "video editing animation motion graphics"
    );
    
    private static final Map<String, String> CATEGORY_EXPANSIONS = Map.of(
        "programming", "programming development coding software web mobile",
        "design", "design graphic ui ux visual branding creative",
        "writing", "writing content copywriting blog articles translation",
        "marketing", "marketing digital advertising seo social media",
        "business", "business consulting strategy planning management"
    );
    
    /**
     * Enhance gig search query for better semantic matching
     */
    public String enhanceGigQuery(String originalQuery) {
        if (originalQuery == null || originalQuery.trim().isEmpty()) {
            return originalQuery;
        }
        
        String enhanced = originalQuery.toLowerCase().trim();
        
        // Apply skill expansions
        for (Map.Entry<String, String> entry : SKILL_EXPANSIONS.entrySet()) {
            String skill = entry.getKey();
            String expansion = entry.getValue();
            
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(skill) + "\\b");
            if (pattern.matcher(enhanced).find()) {
                enhanced = pattern.matcher(enhanced).replaceAll(expansion);
            }
        }
        
        // Apply category expansions
        for (Map.Entry<String, String> entry : CATEGORY_EXPANSIONS.entrySet()) {
            String category = entry.getKey();
            String expansion = entry.getValue();
            
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(category) + "\\b");
            if (pattern.matcher(enhanced).find()) {
                enhanced = pattern.matcher(enhanced).replaceAll(expansion);
            }
        }
        
        // Add service-oriented terms
        enhanced = addServiceTerms(enhanced);
        
        // Clean up extra spaces
        enhanced = enhanced.replaceAll("\\s+", " ").trim();
        
        log.debug("Enhanced query: '{}' -> '{}'", originalQuery, enhanced);
        return enhanced;
    }
    
    /**
     * Add service-oriented terms to query
     */
    private String addServiceTerms(String query) {
        StringBuilder enhanced = new StringBuilder(query);
        
        // Add service context terms
        if (query.contains("need") || query.contains("looking") || query.contains("find")) {
            enhanced.append(" service freelancer gig");
        }
        
        // Add project context
        if (query.contains("project") || query.contains("work")) {
            enhanced.append(" professional experience");
        }
        
        return enhanced.toString();
    }
}