package com.thefreelancer.microservices.workspace_service.repository;

import com.thefreelancer.microservices.workspace_service.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByRoomIdOrderByOrderIndexAscCreatedAtDesc(Long roomId);
    
    List<Task> findByRoomIdAndStatusOrderByOrderIndexAscCreatedAtDesc(Long roomId, Task.TaskStatus status);
    
    List<Task> findByRoomIdAndAssigneeIdOrderByOrderIndexAscCreatedAtDesc(Long roomId, String assigneeId);
    
    List<Task> findByRoomIdAndStatusAndAssigneeIdOrderByOrderIndexAscCreatedAtDesc(
        Long roomId, Task.TaskStatus status, String assigneeId);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.roomId = :roomId AND t.status = :status")
    long countByRoomIdAndStatus(@Param("roomId") Long roomId, @Param("status") Task.TaskStatus status);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.roomId = :roomId AND t.dueDate < :now AND t.status != 'DONE'")
    long countOverdueTasks(@Param("roomId") Long roomId, @Param("now") LocalDateTime now);
    
    @Query("SELECT MAX(t.orderIndex) FROM Task t WHERE t.roomId = :roomId")
    Integer findMaxOrderIndexByRoomId(@Param("roomId") Long roomId);
}
