package com.thefreelancer.microservices.workspace_service.mapper;

import com.thefreelancer.microservices.workspace_service.dto.TaskCreateDto;
import com.thefreelancer.microservices.workspace_service.dto.TaskResponseDto;
import com.thefreelancer.microservices.workspace_service.dto.TaskUpdateDto;
import com.thefreelancer.microservices.workspace_service.model.WorkspaceTask;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface WorkspaceTaskMapper {
    
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "priorityToString")
    @Mapping(target = "createdBy", source = "createdById")
    TaskResponseDto toResponseDto(WorkspaceTask task);
    
    List<TaskResponseDto> toResponseDtoList(List<WorkspaceTask> tasks);
    
    @Mapping(target = "priority", source = "createDto.priority", qualifiedByName = "stringToPriority")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "TODO")
    WorkspaceTask toEntity(TaskCreateDto createDto, Long roomId, String createdBy);
    
    @Mapping(target = "status", source = "updateDto.status", qualifiedByName = "stringToStatus")
    @Mapping(target = "priority", source = "updateDto.priority", qualifiedByName = "stringToPriority")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "createdById", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateTaskFromDto(TaskUpdateDto updateDto, @MappingTarget WorkspaceTask task);
    
    @Named("statusToString")
    default String statusToString(WorkspaceTask.TaskStatus status) {
        return status != null ? status.toString() : null;
    }
    
    @Named("stringToStatus")
    default WorkspaceTask.TaskStatus stringToStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        try {
            return WorkspaceTask.TaskStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // Invalid value, ignore
        }
    }
    
    @Named("priorityToString")
    default String priorityToString(WorkspaceTask.TaskPriority priority) {
        return priority != null ? priority.toString() : null;
    }
    
    @Named("stringToPriority")
    default WorkspaceTask.TaskPriority stringToPriority(String priority) {
        if (priority == null || priority.trim().isEmpty()) {
            return WorkspaceTask.TaskPriority.MEDIUM; // Default
        }
        try {
            return WorkspaceTask.TaskPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return WorkspaceTask.TaskPriority.MEDIUM; // Invalid value, use default
        }
    }
    
    @AfterMapping
    default void setCompletedAt(@MappingTarget WorkspaceTask task, TaskUpdateDto updateDto) {
        if (updateDto.getStatus() != null && 
            "COMPLETED".equalsIgnoreCase(updateDto.getStatus()) && 
            task.getCompletedAt() == null) {
            task.setCompletedAt(LocalDateTime.now());
        } else if (updateDto.getStatus() != null && 
                   !"COMPLETED".equalsIgnoreCase(updateDto.getStatus())) {
            task.setCompletedAt(null);
        }
    }
}
