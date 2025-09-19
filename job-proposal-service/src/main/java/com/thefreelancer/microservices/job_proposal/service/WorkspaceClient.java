package com.thefreelancer.microservices.job_proposal.service;

import com.thefreelancer.microservices.job_proposal.dto.workspace.RoomCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.workspace.RoomResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Service for integrating with workspace service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceClient {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${workspace.service.url}")
    private String workspaceServiceUrl;
    
    /**
     * Create a room in workspace service for a contract
     */
    public RoomResponseDto createRoom(RoomCreateDto roomCreateDto) {
        try {
            String url = workspaceServiceUrl + "/api/workspaces/rooms";
            
            log.info("Creating room in workspace service for contract: {} at URL: {}", 
                    roomCreateDto.getContractId(), url);
            
            WebClient webClient = webClientBuilder.baseUrl(workspaceServiceUrl).build();
            
            RoomResponseDto response = webClient.post()
                    .uri("/api/workspaces/rooms")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", "system")
                    .header("X-User-Role", "ADMIN")
                    .header("X-User-Email", "system@thefreelancer.com")
                    .bodyValue(roomCreateDto)
                    .retrieve()
                    .bodyToMono(RoomResponseDto.class)
                    .block();
            
            if (response != null) {
                log.info("Successfully created room with ID: {} for contract: {}", 
                        response.getId(), roomCreateDto.getContractId());
                return response;
            } else {
                log.error("Failed to create room - received null response");
                throw new RuntimeException("Failed to create room in workspace service");
            }
            
        } catch (WebClientResponseException e) {
            log.error("Error communicating with workspace service: {} - Status: {}, Body: {}", 
                    e.getMessage(), e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to communicate with workspace service", e);
        } catch (Exception e) {
            log.error("Error creating room: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create room in workspace service", e);
        }
    }

    /**
     * Update room status (e.g., to ARCHIVED when contract is completed)
     */
    public void updateRoomStatus(Long contractId, String status) {
        try {
            String endpoint = "/api/workspaces/rooms/contract/" + contractId + "/status";
            
            log.info("Updating room status to {} for contract: {} at endpoint: {}", 
                    status, contractId, endpoint);
            
            WebClient webClient = webClientBuilder.baseUrl(workspaceServiceUrl).build();
            
            // Simple status update payload
            String requestBody = "{\"status\":\"" + status + "\"}";
            
            webClient.put()
                    .uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", "system")
                    .header("X-User-Role", "ADMIN")
                    .header("X-User-Email", "system@thefreelancer.com")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            
            log.info("Successfully updated room status to {} for contract: {}", status, contractId);
            
        } catch (WebClientResponseException e) {
            log.error("Error updating room status for contract {}: {} - Status: {}, Body: {}", 
                    contractId, e.getMessage(), e.getStatusCode(), e.getResponseBodyAsString());
            // Don't throw exception to avoid failing the main contract acceptance
        } catch (Exception e) {
            log.error("Error updating room status for contract {}: {}", contractId, e.getMessage(), e);
            // Don't throw exception to avoid failing the main contract acceptance
        }
    }
}
