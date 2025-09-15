package com.thefreelancer.microservices.workspace_service.controller;

import com.thefreelancer.microservices.workspace_service.dto.WorkspaceEventRequestDto;
import com.thefreelancer.microservices.workspace_service.dto.WorkspaceEventResponseDto;
import com.thefreelancer.microservices.workspace_service.service.WorkspaceEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/events")
@RequiredArgsConstructor
public class WorkspaceEventController {
    private final WorkspaceEventService workspaceEventService;

    @GetMapping
    public ResponseEntity<List<WorkspaceEventResponseDto>> getAllEvents() {
        return ResponseEntity.ok(workspaceEventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceEventResponseDto> getEventById(@PathVariable Long id) {
        return workspaceEventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WorkspaceEventResponseDto> createEvent(@RequestBody WorkspaceEventRequestDto requestDto) {
        WorkspaceEventResponseDto created = workspaceEventService.createEvent(requestDto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkspaceEventResponseDto> updateEvent(@PathVariable Long id, @RequestBody WorkspaceEventRequestDto requestDto) {
        WorkspaceEventResponseDto updated = workspaceEventService.updateEvent(id, requestDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        workspaceEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
