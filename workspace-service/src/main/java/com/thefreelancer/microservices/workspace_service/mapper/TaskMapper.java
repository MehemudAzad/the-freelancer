package com.thefreelancer.microservices.workspace_service.mapper;

import com.thefreelancer.microservices.workspace_service.dto.TaskCreateDto;
import com.thefreelancer.microservices.workspace_service.dto.TaskResponseDto;
import com.thefreelancer.microservices.workspace_service.dto.TaskUpdateDto;
import com.thefreelancer.microservices.workspace_service.model.Task;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TaskMapper {
    
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "priorityToString")
    TaskResponseDto toResponseDto(Task task);
    
    List<TaskResponseDto> toResponseDtoList(List<Task> tasks);
    
    @Mapping(target = "priority", source = "priority", qualifiedByName = "stringToPriority")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "roomId", source = "roomId")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "TODO")
    Task toEntity(TaskCreateDto createDto, @Param("roomId") Long roomId, @Param("createdBy") String createdBy);
    
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "stringToPriority")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "roomId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateTaskFromDto(TaskUpdateDto updateDto, @MappingTarget Task task);
    
    @Named("statusToString")
    default String statusToString(Task.TaskStatus status) {
        return status != null ? status.toString() : null;
    }
    
    @Named("stringToStatus")
    default Task.TaskStatus stringToStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        try {
            return Task.TaskStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // Invalid value, ignore
        }
    }
    
    @Named("priorityToString")
    default String priorityToString(Task.TaskPriority priority) {
        return priority != null ? priority.toString() : null;
    }
    
    @Named("stringToPriority")
    default Task.TaskPriority stringToPriority(String priority) {
        if (priority == null || priority.trim().isEmpty()) {
            return Task.TaskPriority.MEDIUM; // Default
        }
        try {
            return Task.TaskPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Task.TaskPriority.MEDIUM; // Invalid value, use default
        }
    }
    
    @AfterMapping
    default void setCompletedAt(@MappingTarget Task task, TaskUpdateDto updateDto) {
        if (updateDto.getStatus() != null && 
            "DONE".equalsIgnoreCase(updateDto.getStatus()) && 
            task.getCompletedAt() == null) {
            task.setCompletedAt(LocalDateTime.now());
        } else if (updateDto.getStatus() != null && 
                   !"DONE".equalsIgnoreCase(updateDto.getStatus())) {
            task.setCompletedAt(null);
        }
    }
}
