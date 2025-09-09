package com.thefreelancer.microservices.workspace_service.mapper;

import com.thefreelancer.microservices.workspace_service.dto.RoomCreateDto;
import com.thefreelancer.microservices.workspace_service.dto.RoomResponseDto;
import com.thefreelancer.microservices.workspace_service.model.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {
    
    public Room toEntity(RoomCreateDto createDto) {
        Room room = new Room();
        room.setContractId(createDto.getContractId());
        room.setJobTitle(createDto.getJobTitle());
        room.setClientId(createDto.getClientId());
        room.setFreelancerId(createDto.getFreelancerId());
        room.setStatus(Room.RoomStatus.ACTIVE);
        return room;
    }
    
    public RoomResponseDto toResponseDto(Room room) {
        return RoomResponseDto.builder()
            .id(room.getId())
            .contractId(room.getContractId())
            .jobTitle(room.getJobTitle())
            .clientId(room.getClientId())
            .freelancerId(room.getFreelancerId())
            .status(room.getStatus())
            .createdAt(room.getCreatedAt())
            .updatedAt(room.getUpdatedAt())
            .build();
    }
    
    public RoomResponseDto toResponseDtoWithCounts(Room room, Long messageCount, Long fileCount, Long taskCount, Long eventCount) {
        return RoomResponseDto.builder()
            .id(room.getId())
            .contractId(room.getContractId())
            .jobTitle(room.getJobTitle())
            .clientId(room.getClientId())
            .freelancerId(room.getFreelancerId())
            .status(room.getStatus())
            .createdAt(room.getCreatedAt())
            .updatedAt(room.getUpdatedAt())
            .messageCount(messageCount)
            .fileCount(fileCount)
            .taskCount(taskCount)
            .eventCount(eventCount)
            .build();
    }
}
