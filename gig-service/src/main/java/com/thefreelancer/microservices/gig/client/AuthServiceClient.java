package com.thefreelancer.microservices.gig.client;

import com.thefreelancer.microservices.gig.dto.UserResponseDto;
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
    public UserResponseDto getUserById(Long userId) {
        log.info("Fetching user information for userId: {}", userId);
        
        try {
            return getWebClient()
                    .get()
                    .uri("/api/auth/public/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserResponseDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(user -> log.info("Successfully fetched user: {} for ID: {}", user.getName(), userId))
                    .doOnError(error -> log.error("Error fetching user with ID {}: {}", userId, error.getMessage()))
                    .onErrorReturn(UserResponseDto.builder()
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
            return UserResponseDto.builder()
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
     * Get multiple users by their IDs (batch request for performance)
     */
    public Map<Long, UserResponseDto> getUsersByIds(List<Long> userIds) {
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

            List<UserResponseDto> userList = getWebClient()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/auth/users")
                            .queryParam("ids", userIdsParam)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<UserResponseDto>>() {})
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
}
