package com.thefreelancer.microservices.workspace_service.repository;

import com.thefreelancer.microservices.workspace_service.model.WorkspaceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceEventRepository extends JpaRepository<WorkspaceEvent, Long> {
    List<WorkspaceEvent> findByRoomId(Long roomId);
}
