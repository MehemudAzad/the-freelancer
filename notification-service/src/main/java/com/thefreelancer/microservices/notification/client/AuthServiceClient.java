package com.thefreelancer.microservices.notification.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
public class AuthServiceClient {
    
    private final WebClient authServiceWebClient;
    
    public AuthServiceClient(WebClient authServiceWebClient) {
        this.authServiceWebClient = authServiceWebClient;
    }
    
    public UserResponse getUserById(Long userId) {
        try {
            return authServiceWebClient
                    .get()
                    .uri("/api/auth/public/users/{userId}", userId)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> {
                                log.error("Failed to fetch user details for user ID: {}, status: {}", userId, response.statusCode());
                                return Mono.error(new RuntimeException("Failed to fetch user details"));
                            })
                    .bodyToMono(UserResponse.class)
                    .doOnError(error -> log.error("Error fetching user details for user ID {}: {}", userId, error.getMessage()))
                    .onErrorReturn(null) // Return null on error for backward compatibility
                    .block(Duration.ofSeconds(5)); // Convert to blocking call with timeout
        } catch (Exception e) {
            log.error("Error fetching user {}: {}", userId, e.getMessage());
            return null;
        }
    }
    
    public UserResponse getUserByEmail(String email) {
        try {
            return authServiceWebClient
                    .get()
                    .uri("/api/auth/users/email/{email}", email)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> {
                                log.error("Failed to fetch user details for email: {}, status: {}", email, response.statusCode());
                                return Mono.error(new RuntimeException("Failed to fetch user details"));
                            })
                    .bodyToMono(UserResponse.class)
                    .doOnError(error -> log.error("Error fetching user details for email {}: {}", email, error.getMessage()))
                    .onErrorReturn(null) // Return null on error for backward compatibility
                    .block(Duration.ofSeconds(5)); // Convert to blocking call with timeout
        } catch (Exception e) {
            log.error("Error fetching user by email {}: {}", email, e.getMessage());
            return null;
        }
    }
}