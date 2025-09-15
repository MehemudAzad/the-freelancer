package com.thefreelancer.microservices.workspace_service.service;

import com.thefreelancer.microservices.workspace_service.model.WorkspaceEvent;
import com.thefreelancer.microservices.workspace_service.model.Room;
import com.thefreelancer.microservices.workspace_service.repository.WorkspaceEventRepository;
import com.thefreelancer.microservices.workspace_service.repository.RoomRepository;
import com.thefreelancer.microservices.workspace_service.dto.WorkspaceEventRequestDto;
import com.thefreelancer.microservices.workspace_service.dto.WorkspaceEventResponseDto;
import com.thefreelancer.microservices.workspace_service.mapper.WorkspaceEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkspaceEventService {
    private final WorkspaceEventRepository workspaceEventRepository;
    private final RoomRepository roomRepository;
    private final WorkspaceEventMapper workspaceEventMapper;

    public List<WorkspaceEventResponseDto> getAllEvents() {
        return workspaceEventRepository.findAll().stream()
                .map(workspaceEventMapper::toResponseDto)
                .toList();
    }

    public Optional<WorkspaceEventResponseDto> getEventById(Long id) {
        return workspaceEventRepository.findById(id)
                .map(workspaceEventMapper::toResponseDto);
    }

    public WorkspaceEventResponseDto createEvent(WorkspaceEventRequestDto dto) {
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        WorkspaceEvent event = workspaceEventMapper.toEntity(dto, room);
        WorkspaceEvent saved = workspaceEventRepository.save(event);
        return workspaceEventMapper.toResponseDto(saved);
    }

    public WorkspaceEventResponseDto updateEvent(Long id, WorkspaceEventRequestDto dto) {
        WorkspaceEvent event = workspaceEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkspaceEvent not found"));
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        workspaceEventMapper.updateEntity(event, dto, room);
        WorkspaceEvent saved = workspaceEventRepository.save(event);
        return workspaceEventMapper.toResponseDto(saved);
    }

    public void deleteEvent(Long id) {
        workspaceEventRepository.deleteById(id);
    }
}
