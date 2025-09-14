package com.thefreelancer.microservices.workspace_service.client;

import com.thefreelancer.microservices.workspace_service.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${app.auth-service.url:http://localhost:8081/api/auth}")
    private String authServiceUrl;
    
    public Optional<UserResponseDto> getUserById(Long userId) {
        try {
            log.debug("Calling Auth Service to get user: {}", userId);
            
            UserResponseDto user = webClientBuilder
                .baseUrl(authServiceUrl)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path("/users/{userId}")
                    .build(userId))
                .retrieve()
                .bodyToMono(UserResponseDto.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(WebClientResponseException.NotFound.class,
                    ex -> {
                        log.debug("User not found: {}", userId);
                        return Mono.empty();
                    })
                .onErrorResume(Exception.class,
                    ex -> {
                        log.error("Error calling Auth Service for user: {}", userId, ex);
                        return Mono.empty();
                    })
                .block();
            
            if (user != null) {
                log.debug("Successfully retrieved user from Auth Service: {}", userId);
                return Optional.of(user);
            } else {
                log.debug("No user found: {}", userId);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            log.error("Failed to get user from Auth Service: {}", userId, e);
            return Optional.empty();
        }
    }
    
    public List<UserResponseDto> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        
        try {
            log.debug("Calling Auth Service to get {} users", userIds.size());
            
            // Convert userIds to comma-separated string
            String userIdsParam = userIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
            
            List<UserResponseDto> users = webClientBuilder
                .baseUrl(authServiceUrl)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path("/users")
                    .queryParam("ids", userIdsParam)
                    .build())
                .retrieve()
                .bodyToFlux(UserResponseDto.class)
                .collectList()
                .timeout(Duration.ofSeconds(20))
                .onErrorResume(Exception.class,
                    ex -> {
                        log.error("Error calling Auth Service for users: {}", userIds, ex);
                        return Mono.just(List.of());
                    })
                .block();
            
            log.debug("Successfully retrieved {} users from Auth Service", 
                users != null ? users.size() : 0);
            return users != null ? users : List.of();
            
        } catch (Exception e) {
            log.error("Failed to get users from Auth Service: {}", userIds, e);
            return List.of();
        }
    }
}
