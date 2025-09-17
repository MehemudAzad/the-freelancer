package com.thefreelancer.microservices.workspace_service.repository;

import com.thefreelancer.microservices.workspace_service.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    
    // Find messages by room with pagination
    Page<Message> findByRoomIdOrderByCreatedAtDesc(Long roomId, Pageable pageable);
    
    // Find messages before a specific message (for cursor pagination)
    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId AND m.createdAt < " +
           "(SELECT msg.createdAt FROM Message msg WHERE msg.id = :beforeMessageId) " +
           "ORDER BY m.createdAt DESC")
    Page<Message> findByRoomIdBeforeMessageOrderByCreatedAtDesc(
        @Param("roomId") Long roomId, 
        @Param("beforeMessageId") String beforeMessageId, 
        Pageable pageable);
    
    // Find messages after a specific message (for real-time updates)
    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId AND m.createdAt > " +
           "(SELECT msg.createdAt FROM Message msg WHERE msg.id = :afterMessageId) " +
           "ORDER BY m.createdAt ASC")
    List<Message> findByRoomIdAfterMessageOrderByCreatedAtAsc(
        @Param("roomId") Long roomId, 
        @Param("afterMessageId") String afterMessageId);
    
    // Search messages by content
    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId AND " +
           "(LOWER(m.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY m.createdAt DESC")
    Page<Message> searchByRoomIdAndContent(
        @Param("roomId") Long roomId, 
        @Param("searchTerm") String searchTerm, 
        Pageable pageable);
    
    // Search messages by type and content
    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId AND " +
           "m.messageType = :messageType AND " +
           "(LOWER(m.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY m.createdAt DESC")
    Page<Message> searchByRoomIdAndTypeAndContent(
        @Param("roomId") Long roomId,
        @Param("messageType") Message.MessageType messageType,
        @Param("searchTerm") String searchTerm, 
        Pageable pageable);
    
    // Find message with reply context
    @Query("SELECT m FROM Message m LEFT JOIN FETCH m.replyToMessage WHERE m.id = :messageId")
    Optional<Message> findByIdWithReplyContext(@Param("messageId") Long messageId);
    
    // Count messages in room
    long countByRoomId(Long roomId);
    
    // Find latest message in room
    Optional<Message> findFirstByRoomIdOrderByCreatedAtDesc(Long roomId);
}
