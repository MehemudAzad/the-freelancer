package com.thefreelancer.microservices.workspace_service.service;

import com.thefreelancer.microservices.workspace_service.dto.*;
import com.thefreelancer.microservices.workspace_service.exception.ResourceNotFoundException;
import com.thefreelancer.microservices.workspace_service.mapper.TaskMapper;
import com.thefreelancer.microservices.workspace_service.model.Room;
import com.thefreelancer.microservices.workspace_service.model.Task;
import com.thefreelancer.microservices.workspace_service.repository.RoomRepository;
import com.thefreelancer.microservices.workspace_service.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final RoomRepository roomRepository;
    private final TaskMapper taskMapper;
    
    @Transactional
    public TaskResponseDto createTask(String roomId, String userId, TaskCreateDto createDto) {
        log.info("Creating task in room: {} by user: {}", roomId, userId);
        
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
        
        // Create task entity
        Task task = taskMapper.toEntity(createDto, roomIdLong, userId);
        task.setRoom(room);
        
        Task savedTask = taskRepository.save(task);
        
        // TODO: Publish WebSocket event for real-time updates
        // TODO: Send notification to assignee if different from creator
        
        log.info("Task created successfully with ID: {}", savedTask.getId());
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
        List<Task> tasks;
        if (status != null && assigneeId != null) {
            Task.TaskStatus taskStatus = Task.TaskStatus.valueOf(status.toUpperCase());
            tasks = taskRepository.findByRoomIdAndStatusAndAssigneeIdOrderByOrderIndexAscCreatedAtDesc(
                roomIdLong, taskStatus, assigneeId);
        } else if (status != null) {
            Task.TaskStatus taskStatus = Task.TaskStatus.valueOf(status.toUpperCase());
            tasks = taskRepository.findByRoomIdAndStatusOrderByOrderIndexAscCreatedAtDesc(roomIdLong, taskStatus);
        } else if (assigneeId != null) {
            tasks = taskRepository.findByRoomIdAndAssigneeIdOrderByOrderIndexAscCreatedAtDesc(roomIdLong, assigneeId);
        } else {
            tasks = taskRepository.findByRoomIdOrderByOrderIndexAscCreatedAtDesc(roomIdLong);
        }
        
        List<TaskResponseDto> taskDtos = taskMapper.toResponseDtoList(tasks);
        
        // Calculate task statistics
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
        log.info("Updating task: {} in room: {} by user: {}", taskId, roomId, userId);
        
        // Validate room access
        Long roomIdLong = Long.parseLong(roomId);
        Room room = roomRepository.findById(roomIdLong)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));
        validateRoomAccess(room, userId);
        
        // Find and validate task
        Long taskIdLong = Long.parseLong(taskId);
        Task task = taskRepository.findById(taskIdLong)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        
        if (!task.getRoomId().equals(roomIdLong)) {
            throw new IllegalArgumentException("Task does not belong to this room");
        }
        
        // Update task
        taskMapper.updateTaskFromDto(updateDto, task);
        
        Task savedTask = taskRepository.save(task);
        
        // TODO: Publish WebSocket event for real-time updates
        // TODO: Send notification on status change or assignment change
        
        log.info("Task updated successfully: {}", taskId);
        return taskMapper.toResponseDto(savedTask);
    }
    
    private TaskListResponseDto.TaskStats calculateTaskStats(Long roomId) {
        LocalDateTime now = LocalDateTime.now();
        
        return TaskListResponseDto.TaskStats.builder()
            .todoCount(taskRepository.countByRoomIdAndStatus(roomId, Task.TaskStatus.TODO))
            .inProgressCount(taskRepository.countByRoomIdAndStatus(roomId, Task.TaskStatus.IN_PROGRESS))
            .reviewCount(taskRepository.countByRoomIdAndStatus(roomId, Task.TaskStatus.REVIEW))
            .doneCount(taskRepository.countByRoomIdAndStatus(roomId, Task.TaskStatus.DONE))
            .overdueTasks(taskRepository.countOverdueTasks(roomId, now))
            .build();
    }
    
    private void validateRoomAccess(Room room, String userId) {
        if (!room.getClientId().equals(userId) && !room.getFreelancerId().equals(userId)) {
            throw new IllegalArgumentException("User does not have access to this room");
        }
        
        if (room.getStatus() == Room.RoomStatus.LOCKED) {
            throw new IllegalArgumentException("Room is locked");
        }
    }
}
