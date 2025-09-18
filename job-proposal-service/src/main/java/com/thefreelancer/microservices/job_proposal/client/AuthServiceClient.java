package com.thefreelancer.microservices.job_proposal.client;

import com.thefreelancer.microservices.job_proposal.dto.UserResponseDto;
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
    
    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;
    
    /**
     * Get a single user by ID from Auth Service
     */
    public Optional<UserResponseDto> getUserById(Long userId) {
        try {
            log.debug("Calling Auth Service to get user: {}", userId);
            
            UserResponseDto user = webClientBuilder
                .baseUrl(authServiceUrl)
                .build()
                .get()
                .uri("/api/auth/public/users/{userId}", userId)
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
    
    /**
     * Get multiple users by IDs from Auth Service (batch operation)
     * Since auth-service only supports single user lookup, we'll call it individually for each ID
     */
    public List<UserResponseDto> getUsersByIds(List<Long> userIds) {
        try {
            if (userIds == null || userIds.isEmpty()) {
                log.debug("No user IDs provided for batch lookup");
                return List.of();
            }
            
            log.debug("Calling Auth Service to get {} users individually", userIds.size());
            
            // Call getUserById for each ID and collect the results
            List<UserResponseDto> users = userIds.stream()
                .map(this::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
            
            log.debug("Successfully retrieved {} users from Auth Service", users.size());
            return users;
            
        } catch (Exception e) {
            log.error("Failed to get users from Auth Service: {}", userIds, e);
            return List.of();
        }
    }
}
