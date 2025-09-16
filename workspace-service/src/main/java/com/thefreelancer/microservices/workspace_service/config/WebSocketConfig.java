package com.thefreelancer.microservices.workspace_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time chat functionality
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker to carry messages back to the client
        // on destinations prefixed with "/topic" or "/queue"
        config.enableSimpleBroker("/topic", "/queue");
        
        // Define prefix that is used to filter destinations handled by methods
        // annotated with @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        
        // Define prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Register the "/ws" endpoint, enabling SockJS fallback options
        // so that alternative transports can be used if WebSocket is not available
        registry.addEndpoint("/ws")
                .addInterceptors(new WebSocketAuthInterceptor())
                .setAllowedOriginPatterns("*") // Allow all origins for development
                .withSockJS();
                
        // Also add a plain WebSocket endpoint without SockJS for better compatibility
        registry.addEndpoint("/ws")
                .addInterceptors(new WebSocketAuthInterceptor())
                .setAllowedOriginPatterns("*");
    }
}
