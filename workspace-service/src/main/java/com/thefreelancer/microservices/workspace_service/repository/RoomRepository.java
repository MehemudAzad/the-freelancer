package com.thefreelancer.microservices.workspace_service.repository;

import com.thefreelancer.microservices.workspace_service.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    Optional<Room> findByContractId(Long contractId);
    
    boolean existsByContractId(Long contractId);
    
    @Query("SELECT r FROM Room r WHERE r.contractId = :contractId AND (r.clientId = :userId OR r.freelancerId = :userId)")
    Optional<Room> findByContractIdAndUserId(@Param("contractId") Long contractId, @Param("userId") String userId);
    
    @Query("SELECT r FROM Room r WHERE r.id = :roomId AND (r.clientId = :userId OR r.freelancerId = :userId)")
    Optional<Room> findByIdAndUserId(@Param("roomId") Long roomId, @Param("userId") String userId);
    
    @Query("SELECT COUNT(m) FROM Room r LEFT JOIN r.messages m WHERE r.id = :roomId")
    Long countMessagesByRoomId(@Param("roomId") Long roomId);
    
    @Query("SELECT COUNT(f) FROM Room r LEFT JOIN r.files f WHERE r.id = :roomId")
    Long countFilesByRoomId(@Param("roomId") Long roomId);
    
    @Query("SELECT COUNT(t) FROM Room r LEFT JOIN r.tasks t WHERE r.id = :roomId")
    Long countTasksByRoomId(@Param("roomId") Long roomId);
    
    @Query("SELECT COUNT(e) FROM Room r LEFT JOIN r.events e WHERE r.id = :roomId")
    Long countEventsByRoomId(@Param("roomId") Long roomId);
}
