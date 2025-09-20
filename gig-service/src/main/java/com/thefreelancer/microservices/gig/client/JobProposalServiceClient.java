package com.thefreelancer.microservices.gig.client;

import com.thefreelancer.microservices.gig.dto.JobDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.Optional;

@Component
@Slf4j
public class JobProposalServiceClient {
    
    private final WebClient webClient;
    
    public JobProposalServiceClient(WebClient.Builder webClientBuilder, 
                                   @Value("${services.job-proposal-service.url:http://localhost:8083}") String jobProposalServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(jobProposalServiceUrl).build();
    }
    
    /**
     * Check if client has completed at least one contract with freelancer
     */
    public boolean hasCompletedContractWithFreelancer(Long clientId, Long freelancerId) {
        log.debug("Checking if client {} has completed contracts with freelancer {}", clientId, freelancerId);
        
        try {
            Boolean hasCompleted = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/api/contracts/internal/has-completed-contract")
                    .queryParam("clientId", clientId)
                    .queryParam("freelancerId", freelancerId)
                    .build())
                .retrieve()
                .bodyToMono(Boolean.class)
                .block(); // Using block() for synchronous call - consider making this reactive
            
            log.debug("Client {} has completed contract with freelancer {}: {}", clientId, freelancerId, hasCompleted);
            return hasCompleted != null ? hasCompleted : false;
            
        } catch (WebClientException e) {
            log.error("Error checking completed contracts for client {} and freelancer {}: {}", 
                     clientId, freelancerId, e.getMessage());
            // In case of service unavailability, allow review creation (fail-open)
            // You might want to change this to fail-closed in production
            return true;
        }
    }
    
    /**
     * Get job details by ID for matching purposes
     */
    public Optional<JobDto> getJobById(Long jobId) {
        log.debug("Fetching job details for jobId: {}", jobId);
        
        try {
            JobDto job = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/api/jobs/{jobId}")
                    .build(jobId))
                .retrieve()
                .bodyToMono(JobDto.class)
                .block(); // Using block() for synchronous call
            
            log.debug("Successfully fetched job {} with title: {}", jobId, job != null ? job.getTitle() : null);
            return Optional.ofNullable(job);
            
        } catch (WebClientException e) {
            log.error("Error fetching job {}: {}", jobId, e.getMessage());
            return Optional.empty();
        }
    }
}