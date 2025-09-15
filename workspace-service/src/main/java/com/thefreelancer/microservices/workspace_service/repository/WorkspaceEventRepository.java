package com.thefreelancer.microservices.workspace_service.repository;

import com.thefreelancer.microservices.workspace_service.model.WorkspaceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceEventRepository extends JpaRepository<WorkspaceEvent, Long> {
    // You can add custom query methods if needed
}
