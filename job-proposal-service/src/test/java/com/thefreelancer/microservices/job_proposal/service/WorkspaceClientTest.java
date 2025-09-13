package com.thefreelancer.microservices.job_proposal.service;

import com.thefreelancer.microservices.job_proposal.dto.workspace.RoomCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.workspace.RoomResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Test for WorkspaceClient integration
 */
@ExtendWith(MockitoExtension.class)
class WorkspaceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WorkspaceClient workspaceClient;

    @Test
    void testCreateRoom_Success() {
        // Given
        ReflectionTestUtils.setField(workspaceClient, "workspaceServiceUrl", "http://localhost:8084");
        
        RoomCreateDto roomCreateDto = RoomCreateDto.builder()
            .contractId(123L)
            .jobTitle("Test Job")
            .clientId("456")
            .freelancerId("789")
            .build();

        RoomResponseDto expectedResponse = RoomResponseDto.builder()
            .id(1L)
            .contractId(123L)
            .jobTitle("Test Job")
            .clientId("456")
            .freelancerId("789")
            .status("ACTIVE")
            .build();

        ResponseEntity<RoomResponseDto> responseEntity = 
            new ResponseEntity<>(expectedResponse, HttpStatus.CREATED);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(RoomResponseDto.class)))
            .thenReturn(responseEntity);

        // When
        RoomResponseDto result = workspaceClient.createRoom(roomCreateDto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(123L, result.getContractId());
        assertEquals("Test Job", result.getJobTitle());
        assertEquals("456", result.getClientId());
        assertEquals("789", result.getFreelancerId());
        assertEquals("ACTIVE", result.getStatus());
    }

    @Test
    void testCreateRoom_Failure() {
        // Given
        ReflectionTestUtils.setField(workspaceClient, "workspaceServiceUrl", "http://localhost:8084");
        
        RoomCreateDto roomCreateDto = RoomCreateDto.builder()
            .contractId(123L)
            .jobTitle("Test Job")
            .clientId("456")
            .freelancerId("789")
            .build();

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(RoomResponseDto.class)))
            .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then
        assertThrows(RuntimeException.class, () -> workspaceClient.createRoom(roomCreateDto));
    }
}
