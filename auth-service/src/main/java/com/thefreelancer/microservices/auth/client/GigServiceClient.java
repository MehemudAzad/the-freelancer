package com.thefreelancer.microservices.auth.client;

import com.thefreelancer.microservices.auth.dto.ProfileResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GigServiceClient {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${app.gig-service.url:http://localhost:8082}")
    private String gigServiceUrl;
    
    public Optional<ProfileResponseDto> getProfileByUserId(Long userId) {
        try {
            log.debug("Calling Gig Service to get profile for user: {}", userId);
            
            ProfileResponseDto profile = webClientBuilder.build()
                .get()
                .uri(gigServiceUrl + "/api/profiles/{userId}", userId)
                .retrieve()
                .bodyToMono(ProfileResponseDto.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(WebClientResponseException.NotFound.class, 
                    ex -> {
                        log.debug("Profile not found for user: {}", userId);
                        return Mono.empty();
                    })
                .onErrorResume(Exception.class, 
                    ex -> {
                        log.error("Error calling Gig Service for user: {}", userId, ex);
                        return Mono.empty();
                    })
                .block();
            
            if (profile != null) {
                log.debug("Successfully retrieved profile from Gig Service for user: {}", userId);
                return Optional.of(profile);
            } else {
                log.debug("No profile found for user: {}", userId);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            log.error("Failed to get profile from Gig Service for user: {}", userId, e);
            return Optional.empty();
        }
    }
}
