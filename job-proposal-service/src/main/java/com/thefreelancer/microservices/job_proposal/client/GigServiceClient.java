package com.thefreelancer.microservices.job_proposal.client;

import com.thefreelancer.microservices.job_proposal.dto.JobDataForEmbeddingDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GigServiceClient {

    private final WebClient webClient;

    @Value("${services.gig-service.url}")
    private String gigServiceUrl;

    public GigServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<Void> triggerJobEmbeddingGeneration(JobDataForEmbeddingDto jobData) {
        String url = gigServiceUrl + "/api/internal/embeddings/jobs";
        log.info("Sending job data to gig-service for embedding generation. URL: {}", url);
        return webClient.post()
                .uri(url)
                .bodyValue(jobData)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(aVoid -> log.info("Successfully triggered embedding generation for job ID: {}", jobData.getJobId()))
                .doOnError(error -> log.error("Failed to trigger embedding generation for job ID: {}. Error: {}", jobData.getJobId(), error.getMessage()));
    }
}
