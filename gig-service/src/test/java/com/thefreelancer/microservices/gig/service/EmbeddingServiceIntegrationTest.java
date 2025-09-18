package com.thefreelancer.microservices.gig.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Spring AI Vector Store functionality
 * Tests the ProfileEmbeddingService and JobEmbeddingService
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.ai.openai.api-key=test-key",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class EmbeddingServiceIntegrationTest {

    @Autowired
    private ProfileEmbeddingService profileEmbeddingService;

    @Autowired
    private JobEmbeddingService jobEmbeddingService;

    @Autowired
    private MatchingService matchingService;

    @Test
    void testProfileEmbeddingServiceExists() {
        assertNotNull(profileEmbeddingService);
    }

    @Test
    void testJobEmbeddingServiceExists() {
        assertNotNull(jobEmbeddingService);
    }

    @Test
    void testMatchingServiceExists() {
        assertNotNull(matchingService);
    }

    @Test
    void testStoreProfileEmbedding() {
        // Given
        Long userId = 1L;
        String headline = "Full Stack Developer";
        String bio = "Experienced developer with expertise in Java and React";
        List<String> skills = Arrays.asList("Java", "Spring Boot", "React", "PostgreSQL");

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> {
            profileEmbeddingService.storeProfileEmbedding(userId, headline, bio, skills);
        });
    }

    @Test
    void testStoreJobEmbedding() {
        // Given
        Long jobId = 1L;
        String projectName = "E-commerce Platform";
        String description = "Build a modern e-commerce platform with React and Spring Boot";
        List<String> skills = Arrays.asList("Java", "Spring Boot", "React", "MySQL");
        String budgetType = "fixed";
        Long budgetMin = 5000L;
        Long budgetMax = 10000L;

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> {
            jobEmbeddingService.storeJobEmbedding(jobId, projectName, description, skills, budgetType, budgetMin, budgetMax);
        });
    }

    @Test
    void testFindSimilarProfiles() {
        // Given
        String query = "Java Spring Boot developer";
        int topK = 5;
        double threshold = 0.7;

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> {
            List<org.springframework.ai.document.Document> results = profileEmbeddingService.findSimilarProfiles(query, topK, threshold);
            assertNotNull(results);
        });
    }

    @Test
    void testFindSimilarJobs() {
        // Given
        String query = "React frontend development";
        int topK = 5;
        double threshold = 0.7;

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> {
            List<org.springframework.ai.document.Document> results = jobEmbeddingService.findSimilarJobs(query, topK, threshold);
            assertNotNull(results);
        });
    }

    @Test
    void testMatchingService() {
        // Given
        List<String> skills = Arrays.asList("Java", "Spring Boot");
        int topK = 5;

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> {
            List<MatchingService.MatchResult> results = matchingService.findFreelancersBySkills(skills, topK);
            assertNotNull(results);
        });
    }
}