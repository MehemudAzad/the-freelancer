package com.thefreelancer.microservices.workspace_service.service;

import com.thefreelancer.microservices.workspace_service.dto.*;
import com.thefreelancer.microservices.workspace_service.exception.ResourceNotFoundException;
import com.thefreelancer.microservices.workspace_service.mapper.WorkspaceTaskMapper;
import com.thefreelancer.microservices.workspace_service.model.Room;
import com.thefreelancer.microservices.workspace_service.model.WorkspaceTask;
import com.thefreelancer.microservices.workspace_service.repository.RoomRepository;
import com.thefreelancer.microservices.workspace_service.repository.WorkspaceTaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceTaskService {
    
    private final WorkspaceTaskRepository taskRepository;
    private final RoomRepository roomRepository;
    private final WorkspaceTaskMapper taskMapper;
    
    @Transactional
    public TaskResponseDto createTask(String roomId, String userId, TaskCreateDto createDto) {
        log.info("Creating WorkspaceTask in room: {} by user: {}", roomId, userId);
        
        // Validate room exists and user has access
        Long roomIdLong = Long.parseLong(roomId);
        Room room = roomRepository.findById(roomIdLong)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));
        
        validateRoomAccess(room, userId);
        
        // Set order index if not provided
        if (createDto.getOrderIndex() == null) {
            Integer maxOrder = taskRepository.findMaxOrderIndexByRoomId(roomIdLong);
            createDto.setOrderIndex(maxOrder != null ? maxOrder + 1 : 1);
        }
        
        // Create WorkspaceTask entity
    WorkspaceTask workspaceTask = taskMapper.toEntity(createDto, roomIdLong, userId);
    // Mapper ignores room and createdById; set them here
    workspaceTask.setRoom(room);
    workspaceTask.setCreatedById(userId);
        
        WorkspaceTask savedTask = taskRepository.save(workspaceTask);
        
        // TODO: Publish WebSocket event for real-time updates
        // TODO: Send notification to assignee if different from creator
        
        log.info("WorkspaceTask created successfully with ID: {}", savedTask.getId());
        return taskMapper.toResponseDto(savedTask);
    }
    
    @Transactional(readOnly = true)
    public TaskListResponseDto getRoomTasks(String roomId, String userId, String status, String assigneeId) {
        log.info("Getting tasks for room: {} by user: {} with filters - status: {}, assignee: {}", 
                roomId, userId, status, assigneeId);
        
        // Validate room access
        Long roomIdLong = Long.parseLong(roomId);
        Room room = roomRepository.findById(roomIdLong)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));
        validateRoomAccess(room, userId);
        
        // Fetch tasks with filters
        List<WorkspaceTask> tasks;
        if (status != null && assigneeId != null) {
            WorkspaceTask.TaskStatus taskStatus = WorkspaceTask.TaskStatus.valueOf(status.toUpperCase());
            tasks = taskRepository.findByRoomIdAndStatusAndAssigneeIdOrderByOrderIndexAscCreatedAtDesc(
                roomIdLong, taskStatus, assigneeId);
        } else if (status != null) {
            WorkspaceTask.TaskStatus taskStatus = WorkspaceTask.TaskStatus.valueOf(status.toUpperCase());
            tasks = taskRepository.findByRoomIdAndStatusOrderByOrderIndexAscCreatedAtDesc(roomIdLong, taskStatus);
        } else if (assigneeId != null) {
            tasks = taskRepository.findByRoomIdAndAssigneeIdOrderByOrderIndexAscCreatedAtDesc(roomIdLong, assigneeId);
        } else {
            tasks = taskRepository.findByRoomIdOrderByOrderIndexAscCreatedAtDesc(roomIdLong);
        }
        
        List<TaskResponseDto> taskDtos = taskMapper.toResponseDtoList(tasks);
        
        // Calculate WorkspaceTask statistics
        TaskListResponseDto.TaskStats stats = calculateTaskStats(roomIdLong);
        
        log.info("Found {} tasks for room: {}", tasks.size(), roomId);
        
        return TaskListResponseDto.builder()
            .tasks(taskDtos)
            .totalTasks(tasks.size())
            .stats(stats)
            .build();
    }
    
    @Transactional
    public TaskResponseDto updateTask(String roomId, String taskId, String userId, TaskUpdateDto updateDto) {
        log.info("Updating WorkspaceTask: {} in room: {} by user: {}", taskId, roomId, userId);
        
        // Validate room access
        Long roomIdLong = Long.parseLong(roomId);
        Room room = roomRepository.findById(roomIdLong)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));
        validateRoomAccess(room, userId);
        
        // Find and validate WorkspaceTask
        Long taskIdLong = Long.parseLong(taskId);
        WorkspaceTask workspaceTask = taskRepository.findById(taskIdLong)
            .orElseThrow(() -> new ResourceNotFoundException("WorkspaceTask not found: " + taskId));

        if (workspaceTask.getRoom() == null || !workspaceTask.getRoom().getId().equals(roomIdLong)) {
            throw new IllegalArgumentException("WorkspaceTask does not belong to this room");
        }

        // Update WorkspaceTask
        taskMapper.updateTaskFromDto(updateDto, workspaceTask);

        WorkspaceTask savedTask = taskRepository.save(workspaceTask);
        
        // TODO: Publish WebSocket event for real-time updates
        // TODO: Send notification on status change or assignment change
        
        log.info("WorkspaceTask updated successfully: {}", taskId);
        return taskMapper.toResponseDto(savedTask);
    }

    @Transactional
    public TaskResponseDto updateTaskStatus(String roomId, String taskId, String userId, String status) {
        log.info("Updating WorkspaceTask status: {} in room: {} by user: {} to status: {}", taskId, roomId, userId, status);
        
        // Validate room access
        Long roomIdLong = Long.parseLong(roomId);
        Room room = roomRepository.findById(roomIdLong)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));
        validateRoomAccess(room, userId);
        
        // Find and validate WorkspaceTask
        Long taskIdLong = Long.parseLong(taskId);
        WorkspaceTask workspaceTask = taskRepository.findById(taskIdLong)
            .orElseThrow(() -> new ResourceNotFoundException("WorkspaceTask not found: " + taskId));

        if (workspaceTask.getRoom() == null || !workspaceTask.getRoom().getId().equals(roomIdLong)) {
            throw new IllegalArgumentException("WorkspaceTask does not belong to this room");
        }

        // Validate and update status
        try {
            WorkspaceTask.TaskStatus newStatus = WorkspaceTask.TaskStatus.valueOf(status.toUpperCase());
            workspaceTask.setStatus(newStatus);
            
            // Set completed timestamp if status is COMPLETED
            if (newStatus == WorkspaceTask.TaskStatus.COMPLETED) {
                workspaceTask.setCompletedAt(java.time.LocalDateTime.now());
            } else if (workspaceTask.getCompletedAt() != null) {
                // Clear completed timestamp if moving away from COMPLETED status
                workspaceTask.setCompletedAt(null);
            }
            
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Valid statuses are: TODO, IN_PROGRESS, REVIEW, COMPLETED, BLOCKED");
        }

        WorkspaceTask savedTask = taskRepository.save(workspaceTask);
        
        // TODO: Publish WebSocket event for real-time updates
        // TODO: Send notification on status change
        
        log.info("WorkspaceTask status updated successfully: {} to status: {}", taskId, status);
        return taskMapper.toResponseDto(savedTask);
    }

    @Transactional
    public void deleteTask(String roomId, String taskId, String userId) {
        log.info("Deleting WorkspaceTask: {} in room: {} by user: {}", taskId, roomId, userId);
        
        // Validate room access
        Long roomIdLong = Long.parseLong(roomId);
        Room room = roomRepository.findById(roomIdLong)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));
        validateRoomAccess(room, userId);
        
        // Find and validate WorkspaceTask
        Long taskIdLong = Long.parseLong(taskId);
        WorkspaceTask workspaceTask = taskRepository.findById(taskIdLong)
            .orElseThrow(() -> new ResourceNotFoundException("WorkspaceTask not found: " + taskId));

        if (workspaceTask.getRoom() == null || !workspaceTask.getRoom().getId().equals(roomIdLong)) {
            throw new IllegalArgumentException("WorkspaceTask does not belong to this room");
        }

        // Delete the task
        taskRepository.delete(workspaceTask);
        
        // TODO: Publish WebSocket event for real-time updates
        // TODO: Send notification about task deletion
        
        log.info("WorkspaceTask deleted successfully: {}", taskId);
    }
    
    private TaskListResponseDto.TaskStats calculateTaskStats(Long roomId) {
        LocalDate today = LocalDate.now();
        
        return TaskListResponseDto.TaskStats.builder()
            .todoCount(taskRepository.countByRoomIdAndStatus(roomId, WorkspaceTask.TaskStatus.TODO))
            .inProgressCount(taskRepository.countByRoomIdAndStatus(roomId, WorkspaceTask.TaskStatus.IN_PROGRESS))
            .reviewCount(taskRepository.countByRoomIdAndStatus(roomId, WorkspaceTask.TaskStatus.REVIEW))
            .doneCount(taskRepository.countByRoomIdAndStatus(roomId, WorkspaceTask.TaskStatus.COMPLETED))
            .overdueTasks(taskRepository.countOverdueTasks(roomId, today))
            .build();
    }
    
    private void validateRoomAccess(Room room, String userId) {
        Long userIdLong = Long.parseLong(userId);
        if (!room.getClientId().equals(userIdLong) && !room.getFreelancerId().equals(userIdLong)) {
            throw new IllegalArgumentException("User does not have access to this room");
        }
        
        if (room.getStatus() == Room.RoomStatus.LOCKED) {
            throw new IllegalArgumentException("Room is locked");
        }
    }
}
