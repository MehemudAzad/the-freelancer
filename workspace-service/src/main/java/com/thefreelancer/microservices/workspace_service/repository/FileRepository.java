package com.thefreelancer.microservices.workspace_service.repository;

import com.thefreelancer.microservices.workspace_service.model.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    
    @Query("SELECT f FROM File f WHERE f.room.id = :roomId ORDER BY f.createdAt DESC")
    Page<File> findByRoomIdOrderByCreatedAtDesc(@Param("roomId") Long roomId, Pageable pageable);
    
    @Query("SELECT f FROM File f WHERE f.room.id = :roomId AND " +
           "(LOWER(f.filename) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(f.originalFilename) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY f.createdAt DESC")
    Page<File> findByRoomIdAndSearchOrderByCreatedAtDesc(
        @Param("roomId") Long roomId, 
        @Param("search") String search, 
        Pageable pageable
    );
    
    Optional<File> findByIdAndRoomId(Long id, Long roomId);
    
    List<File> findByUploaderId(Long uploaderId);
    
    @Query("SELECT COUNT(f) FROM File f WHERE f.room.id = :roomId")
    long countByRoomId(@Param("roomId") Long roomId);
    
    @Query("SELECT SUM(f.fileSize) FROM File f WHERE f.room.id = :roomId")
    Long getTotalFileSizeByRoomId(@Param("roomId") Long roomId);
}
