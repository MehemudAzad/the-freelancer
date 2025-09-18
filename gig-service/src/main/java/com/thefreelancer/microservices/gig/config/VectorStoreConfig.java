package com.thefreelancer.microservices.gig.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring AI Vector Store using PGVector
 * This configuration relies on Spring AI auto-configuration
 * 
 * The following properties should be set in application.yml:
 * 
 * spring:
 *   ai:
 *     openai:
 *       api-key: ${OPENAI_API_KEY}
 *     vectorstore:
 *       pgvector:
 *         index-type: HNSW
 *         distance-type: COSINE_DISTANCE
 *         dimensions: 1536
 *   datasource:
 *     url: jdbc:postgresql://localhost:5432/gig_db
 *     username: ${DB_USERNAME:gig_user}
 *     password: ${DB_PASSWORD:gig_password}
 */
@Configuration
public class VectorStoreConfig {
    
    // Spring AI will auto-configure:
    // 1. OpenAI EmbeddingModel using the API key
    // 2. PgVectorStore using the datasource configuration
    // 3. Vector table creation and management
    
    // No additional beans needed when using auto-configuration
}