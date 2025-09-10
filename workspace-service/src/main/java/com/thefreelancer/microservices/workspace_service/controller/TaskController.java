package com.thefreelancer.microservices.workspace_service.controller;

import com.thefreelancer.microservices.workspace_service.dto.*;
import com.thefreelancer.microservices.workspace_service.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workspaces/rooms/{roomId}/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    
    private final TaskService taskService;
    
    /**
     * Create task in room
     */
    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(
            @PathVariable String roomId,
            @Valid @RequestBody TaskCreateDto createDto,
            HttpServletRequest request) {
        
        log.info("Creating task in room: {}", roomId);
        
        // Extract user ID from request (from gateway/JWT)
        String userId = extractUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        TaskResponseDto response = taskService.createTask(roomId, userId, createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get room tasks with filters
     */
    @GetMapping
    public ResponseEntity<TaskListResponseDto> getRoomTasks(
            @PathVariable String roomId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String assigneeId,
            HttpServletRequest request) {
        
        log.info("Getting tasks for room: {} with filters - status: {}, assignee: {}", 
                roomId, status, assigneeId);
        
        // Extract user ID from request
        String userId = extractUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        TaskListResponseDto response = taskService.getRoomTasks(roomId, userId, status, assigneeId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update task status/details
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable String roomId,
            @PathVariable String taskId,
            @Valid @RequestBody TaskUpdateDto updateDto,
            HttpServletRequest request) {
        
        log.info("Updating task: {} in room: {}", taskId, roomId);
        
        // Extract user ID from request
        String userId = extractUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        TaskResponseDto response = taskService.updateTask(roomId, taskId, userId, updateDto);
        return ResponseEntity.ok(response);
    }
    
    private String extractUserIdFromRequest(HttpServletRequest request) {
        // Try to get user ID from X-User-Id header first (from gateway)
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            return userIdHeader;
        }
        
        // TODO: Add JWT token extraction logic if needed
        log.warn("No user ID found in request headers");
        return null;
    }
}
