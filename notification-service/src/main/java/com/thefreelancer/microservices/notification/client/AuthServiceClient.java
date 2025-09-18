package com.thefreelancer.microservices.notification.client;

import com.thefreelancer.microservices.notification.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${auth-service.base-url}")
    private String authServiceBaseUrl;

    private WebClient getWebClient() {
        return webClientBuilder
                .baseUrl(authServiceBaseUrl)
                .build();
    }

    /**
     * Get user information by user ID from auth-service
     */
    public UserResponse getUserById(Long userId) {
        log.info("Fetching user information for userId: {}", userId);
        
        try {
            return getWebClient()
                    .get()
                    .uri("/api/auth/public/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(user -> log.info("Successfully fetched user: {} for ID: {}", user.getName(), userId))
                    .doOnError(error -> log.error("Error fetching user with ID {}: {}", userId, error.getMessage()))
                    .onErrorReturn(UserResponse.builder()
                            .id(userId)
                            .name("Unknown User")
                            .email("unknown@example.com")
                            .handle("unknown")
                            .role("freelancer")
                            .isActive(false)
                            .build())
                    .block();
        } catch (Exception e) {
            log.error("Exception occurred while fetching user with ID {}: {}", userId, e.getMessage());
            return UserResponse.builder()
                    .id(userId)
                    .name("Unknown User")
                    .email("unknown@example.com")
                    .handle("unknown")
                    .role("freelancer")
                    .isActive(false)
                    .build();
        }
    }

    /**
     * Get user information by email from auth-service
     */
    public UserResponse getUserByEmail(String email) {
        log.info("Fetching user information for email: {}", email);
        
        try {
            return getWebClient()
                    .get()
                    .uri("/api/auth/users/email/{email}", email)
                    .retrieve()
                    .bodyToMono(UserResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(user -> log.info("Successfully fetched user: {} for email: {}", user.getName(), email))
                    .doOnError(error -> log.error("Error fetching user with email {}: {}", email, error.getMessage()))
                    .onErrorReturn(UserResponse.builder()
                            .email(email)
                            .name("Unknown User")
                            .handle("unknown")
                            .role("freelancer")
                            .isActive(false)
                            .build())
                    .block();
        } catch (Exception e) {
            log.error("Exception occurred while fetching user with email {}: {}", email, e.getMessage());
            return UserResponse.builder()
                    .email(email)
                    .name("Unknown User")
                    .handle("unknown")
                    .role("freelancer")
                    .isActive(false)
                    .build();
        }
    }

    /**
     * Get multiple users by their IDs (batch request for performance)
     */
    public Map<Long, UserResponse> getUsersByIds(List<Long> userIds) {
        log.info("Fetching {} users by IDs: {}", userIds.size(), userIds);
        
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            // Convert list to comma-separated string for query parameter
            String userIdsParam = userIds.stream()
                    .map(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

            List<UserResponse> userList = getWebClient()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/auth/users")
                            .queryParam("ids", userIdsParam)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<UserResponse>>() {})
                    .timeout(Duration.ofSeconds(15))
                    .doOnSuccess(users -> log.info("Successfully fetched {} users", users.size()))
                    .doOnError(error -> log.error("Error fetching users by IDs {}: {}", userIds, error.getMessage()))
                    .onErrorReturn(Collections.emptyList())
                    .block();

            // Convert list to map for easier lookup
            return userList != null ? 
                userList.stream().collect(
                    java.util.stream.Collectors.toMap(user -> user.getId(), user -> user)
                ) : Collections.emptyMap();
                
        } catch (Exception e) {
            log.error("Exception occurred while fetching users by IDs {}: {}", userIds, e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Check if user exists by ID
     */
    public boolean userExists(Long userId) {
        log.debug("Checking if user exists for userId: {}", userId);
        
        try {
            UserResponse user = getUserById(userId);
            return user != null && user.getId() != null && !user.getName().equals("Unknown User");
        } catch (Exception e) {
            log.error("Exception occurred while checking user existence for ID {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Get user's basic info (just email and name) for notifications
     */
    public UserResponse getUserBasicInfo(Long userId) {
        log.debug("Fetching basic user info for userId: {}", userId);
        
        try {
            UserResponse user = getUserById(userId);
            if (user != null && user.getEmail() != null && !user.getEmail().equals("unknown@example.com")) {
                return UserResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .handle(user.getHandle())
                        .isActive(user.getIsActive())
                        .build();
            }
            return null;
        } catch (Exception e) {
            log.error("Exception occurred while fetching basic user info for ID {}: {}", userId, e.getMessage());
            return null;
        }
    }
}