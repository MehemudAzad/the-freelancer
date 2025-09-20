package com.thefreelancer.microservices.ai_service.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * AI configuration using Spring AI with RAG capabilities
 */
@Configuration
@Slf4j
public class AIConfig {
    
    /**
     * ChatClient for OpenAI interactions using Spring AI
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        log.info("Initializing Spring AI ChatClient");
        return ChatClient.builder(chatModel).build();
    }
}