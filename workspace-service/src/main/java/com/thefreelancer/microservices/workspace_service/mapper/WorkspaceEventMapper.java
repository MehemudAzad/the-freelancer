package com.thefreelancer.microservices.workspace_service.mapper;

import com.thefreelancer.microservices.workspace_service.dto.WorkspaceEventRequestDto;
import com.thefreelancer.microservices.workspace_service.dto.WorkspaceEventResponseDto;
import com.thefreelancer.microservices.workspace_service.model.Room;
import com.thefreelancer.microservices.workspace_service.model.WorkspaceEvent;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceEventMapper {
    public WorkspaceEventResponseDto toResponseDto(WorkspaceEvent event) {
        return WorkspaceEventResponseDto.builder()
                .id(event.getId())
                .roomId(event.getRoom() != null ? event.getRoom().getId() : null)
                .title(event.getTitle())
                .description(event.getDescription())
                .eventType(event.getEventType())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .build();
    }

    public WorkspaceEvent toEntity(WorkspaceEventRequestDto dto, Room room) {
        return WorkspaceEvent.builder()
                .room(room)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .eventType(dto.getEventType())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }

    public void updateEntity(WorkspaceEvent event, WorkspaceEventRequestDto dto, Room room) {
        event.setRoom(room);
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setEventType(dto.getEventType());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
    }
}
