package com.thefreelancer.microservices.workspace_service.controller;

import com.thefreelancer.microservices.workspace_service.dto.RoomCreateDto;
import com.thefreelancer.microservices.workspace_service.dto.RoomResponseDto;
import com.thefreelancer.microservices.workspace_service.dto.RoomSettingsUpdateDto;
import com.thefreelancer.microservices.workspace_service.dto.RoomStatusUpdateDto;
import com.thefreelancer.microservices.workspace_service.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@Slf4j
public class RoomController {
    
    private final RoomService roomService;
    
    // 1. Get workspace for contract
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<RoomResponseDto> getWorkspaceByContract(
            @PathVariable Long contractId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/workspaces/contract/{} - Getting workspace for contract", contractId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for accessing workspace");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only clients and freelancers can access workspace
        if (!"CLIENT".equalsIgnoreCase(userRole) && !"FREELANCER".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only clients and freelancers can access workspace. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            String authenticatedUserId = userIdHeader;
            log.info("Getting workspace for contract: {} by user: {}", contractId, authenticatedUserId);
            
            RoomResponseDto room = roomService.getRoomByContractId(contractId, authenticatedUserId);
            log.info("Successfully retrieved workspace for contract: {}", contractId);
            
            return ResponseEntity.ok(room);
        } catch (IllegalArgumentException e) {
            log.error("Room not found for contract {}: {}", contractId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error retrieving workspace for contract {}: {}", contractId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 2. Create workspace room (triggered by contract creation)
    @PostMapping("/rooms")
    public ResponseEntity<RoomResponseDto> createWorkspaceRoom(
            @Valid @RequestBody RoomCreateDto createDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("POST /api/workspaces/rooms - Creating workspace room for contract: {}", createDto.getContractId());
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for creating workspace room");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // For room creation, we'll allow ADMIN role as well (for system-triggered creation)
        if (!"CLIENT".equalsIgnoreCase(userRole) && !"FREELANCER".equalsIgnoreCase(userRole) && !"ADMIN".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only authorized users can create workspace rooms. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            String authenticatedUserId = userIdHeader;
            log.info("Creating workspace room for contract: {} by user: {}", createDto.getContractId(), authenticatedUserId);
            
            RoomResponseDto room = roomService.createRoom(createDto);
            log.info("Successfully created workspace room: {} for contract: {}", room.getId(), createDto.getContractId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(room);
        } catch (IllegalStateException e) {
            log.error("Room already exists for contract {}: {}", createDto.getContractId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error creating workspace room for contract {}: {}", createDto.getContractId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 3. Update room settings
    @PutMapping("/rooms/{roomId}/settings")
    public ResponseEntity<RoomResponseDto> updateRoomSettings(
            @PathVariable Long roomId,
            @Valid @RequestBody RoomSettingsUpdateDto settingsDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("PUT /api/workspaces/rooms/{}/settings - Updating room settings", roomId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for updating room settings");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only clients and freelancers can update settings
        if (!"CLIENT".equalsIgnoreCase(userRole) && !"FREELANCER".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only clients and freelancers can update room settings. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            String authenticatedUserId = userIdHeader;
            log.info("Updating settings for room: {} by user: {}", roomId, authenticatedUserId);
            
            RoomResponseDto room = roomService.updateRoomSettings(roomId, settingsDto, authenticatedUserId);
            log.info("Successfully updated settings for room: {}", roomId);
            
            return ResponseEntity.ok(room);
        } catch (IllegalArgumentException e) {
            log.error("Room not found or access denied for room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error updating settings for room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 4. Archive/close workspace
    @PutMapping("/rooms/{roomId}/status")
    public ResponseEntity<RoomResponseDto> updateRoomStatus(
            @PathVariable Long roomId,
            @Valid @RequestBody RoomStatusUpdateDto statusDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("PUT /api/workspaces/rooms/{}/status - Updating room status to: {}", roomId, statusDto.getStatus());
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for updating room status");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only clients and freelancers can update status
        if (!"CLIENT".equalsIgnoreCase(userRole) && !"FREELANCER".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only clients and freelancers can update room status. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            String authenticatedUserId = userIdHeader;
            log.info("Updating status for room: {} to: {} by user: {}", roomId, statusDto.getStatus(), authenticatedUserId);
            
            RoomResponseDto room = roomService.updateRoomStatus(roomId, statusDto, authenticatedUserId);
            log.info("Successfully updated status for room: {} to: {}", roomId, statusDto.getStatus());
            
            return ResponseEntity.ok(room);
        } catch (IllegalArgumentException e) {
            log.error("Room not found or access denied for room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            log.error("Invalid status transition for room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error updating status for room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
