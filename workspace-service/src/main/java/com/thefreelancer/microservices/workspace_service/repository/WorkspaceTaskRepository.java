package com.thefreelancer.microservices.workspace_service.repository;

import com.thefreelancer.microservices.workspace_service.model.WorkspaceTask;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkspaceTaskRepository extends JpaRepository<WorkspaceTask, Long> {
    
    List<WorkspaceTask> findByRoomIdOrderByOrderIndexAscCreatedAtDesc(Long roomId);

    List<WorkspaceTask> findByRoomIdAndStatusOrderByOrderIndexAscCreatedAtDesc(Long roomId, WorkspaceTask.TaskStatus status);

    List<WorkspaceTask> findByRoomIdAndAssigneeIdOrderByOrderIndexAscCreatedAtDesc(Long roomId, String assigneeId);

    List<WorkspaceTask> findByRoomIdAndStatusAndAssigneeIdOrderByOrderIndexAscCreatedAtDesc(
        Long roomId, WorkspaceTask.TaskStatus status, String assigneeId);

    @Query("SELECT COUNT(t) FROM WorkspaceTask t WHERE t.room.id = :roomId AND t.status = :status")
    long countByRoomIdAndStatus(@Param("roomId") Long roomId, @Param("status") WorkspaceTask.TaskStatus status);

    @Query("SELECT COUNT(t) FROM WorkspaceTask t WHERE t.room.id = :roomId AND t.dueDate < :today AND t.status <> 'COMPLETED'")
    long countOverdueTasks(@Param("roomId") Long roomId, @Param("today") LocalDate today);

    @Query("SELECT MAX(t.orderIndex) FROM WorkspaceTask t WHERE t.room.id = :roomId")
    Integer findMaxOrderIndexByRoomId(@Param("roomId") Long roomId);
}
