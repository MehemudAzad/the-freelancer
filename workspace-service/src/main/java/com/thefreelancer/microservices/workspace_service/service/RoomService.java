package com.thefreelancer.microservices.workspace_service.service;

import com.thefreelancer.microservices.workspace_service.dto.RoomCreateDto;
import com.thefreelancer.microservices.workspace_service.dto.RoomResponseDto;
import com.thefreelancer.microservices.workspace_service.dto.RoomSettingsUpdateDto;
import com.thefreelancer.microservices.workspace_service.dto.RoomStatusUpdateDto;
import com.thefreelancer.microservices.workspace_service.mapper.RoomMapper;
import com.thefreelancer.microservices.workspace_service.model.Room;
import com.thefreelancer.microservices.workspace_service.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoomService {
    
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    
    @Transactional
    public RoomResponseDto createRoom(RoomCreateDto createDto) {
        log.info("Creating workspace room for contract: {}", createDto.getContractId());
        
        // Check if room already exists for this contract
        if (roomRepository.existsByContractId(createDto.getContractId())) {
            throw new IllegalStateException("Room already exists for contract: " + createDto.getContractId());
        }
        
        Room room = roomMapper.toEntity(createDto);
        Room savedRoom = roomRepository.save(room);
        
        log.info("Created workspace room: {} for contract: {}", savedRoom.getId(), createDto.getContractId());
        return roomMapper.toResponseDto(savedRoom);
    }
    
    @Transactional(readOnly = true)
    public RoomResponseDto getRoomByContractId(Long contractId, String userId) {
        log.info("Getting workspace room for contract: {} by user: {}", contractId, userId);
        
        Room room = roomRepository.findByContractIdAndUserId(contractId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Room not found for contract: " + contractId + " or access denied"));
        
        // Get counts for statistics
        Long messageCount = roomRepository.countMessagesByRoomId(room.getId());
        Long fileCount = roomRepository.countFilesByRoomId(room.getId());
        Long taskCount = roomRepository.countTasksByRoomId(room.getId());
        Long eventCount = roomRepository.countEventsByRoomId(room.getId());
        
        return roomMapper.toResponseDtoWithCounts(room, messageCount, fileCount, taskCount, eventCount);
    }
    
    @Transactional
    public RoomResponseDto updateRoomSettings(Long roomId, RoomSettingsUpdateDto settingsDto, String userId) {
        log.info("Updating room settings for room: {} by user: {}", roomId, userId);
        
        Room room = roomRepository.findByIdAndUserId(roomId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId + " or access denied"));
        
        // For now, we'll just log the settings update as we don't have these fields in our Room entity yet
        // In a real implementation, you might want to add these fields to the Room entity
        log.info("Room settings update requested: {}", settingsDto);
        
        // TODO: Update room settings when we add the fields to Room entity
        
        return roomMapper.toResponseDto(room);
    }
    
    @Transactional
    public RoomResponseDto updateRoomStatus(Long roomId, RoomStatusUpdateDto statusDto, String userId) {
        log.info("Updating room status for room: {} to status: {} by user: {}", roomId, statusDto.getStatus(), userId);
        
        Room room = roomRepository.findByIdAndUserId(roomId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId + " or access denied"));
        
        // Validate status transition
        if (room.getStatus() == Room.RoomStatus.ARCHIVED && statusDto.getStatus() == Room.RoomStatus.ACTIVE) {
            throw new IllegalStateException("Cannot reactivate an archived room");
        }
        
        room.setStatus(statusDto.getStatus());
        Room savedRoom = roomRepository.save(room);
        
        log.info("Updated room status for room: {} to: {}", roomId, statusDto.getStatus());
        return roomMapper.toResponseDto(savedRoom);
    }
    
    @Transactional(readOnly = true)
    public boolean hasAccess(Long roomId, String userId) {
        return roomRepository.findByIdAndUserId(roomId, userId).isPresent();
    }
}
