package com.thefreelancer.microservices.job_proposal.service;

import com.thefreelancer.microservices.job_proposal.dto.workspace.RoomCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.workspace.RoomResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Service for integrating with workspace service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${workspace.service.url}")
    private String workspaceServiceUrl;
    
    /**
     * Create a room in workspace service for a contract
     */
    public RoomResponseDto createRoom(RoomCreateDto roomCreateDto) {
        try {
            String url = workspaceServiceUrl + "/api/workspaces/rooms";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Add system authentication headers for service-to-service communication
            headers.set("X-User-Id", "system");
            headers.set("X-User-Role", "ADMIN");
            headers.set("X-User-Email", "system@thefreelancer.com");
            
            HttpEntity<RoomCreateDto> requestEntity = new HttpEntity<>(roomCreateDto, headers);
            
            log.info("Creating room in workspace service for contract: {} at URL: {}", 
                    roomCreateDto.getContractId(), url);
            
            ResponseEntity<RoomResponseDto> response = restTemplate.postForEntity(
                    url, requestEntity, RoomResponseDto.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                RoomResponseDto roomResponse = response.getBody();
                log.info("Successfully created room with ID: {} for contract: {}", 
                        roomResponse.getId(), roomCreateDto.getContractId());
                return roomResponse;
            } else {
                log.error("Failed to create room - received status: {} with body: {}", 
                        response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to create room in workspace service");
            }
            
        } catch (RestClientException e) {
            log.error("Error communicating with workspace service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to communicate with workspace service", e);
        }
    }
}
