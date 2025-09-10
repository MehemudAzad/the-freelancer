package com.thefreelancer.microservices.workspace_service.service;

import com.thefreelancer.microservices.workspace_service.dto.file.FileResponseDto;
import com.thefreelancer.microservices.workspace_service.dto.file.FileUpdateDto;
import com.thefreelancer.microservices.workspace_service.dto.file.FileUploadDto;
import com.thefreelancer.microservices.workspace_service.exception.ResourceNotFoundException;
import com.thefreelancer.microservices.workspace_service.mapper.FileMapper;
import com.thefreelancer.microservices.workspace_service.model.File;
import com.thefreelancer.microservices.workspace_service.model.Room;
import com.thefreelancer.microservices.workspace_service.repository.FileRepository;
import com.thefreelancer.microservices.workspace_service.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {
    
    private final FileRepository fileRepository;
    private final RoomRepository roomRepository;
    private final FileMapper fileMapper;
    
    @Transactional
    public FileResponseDto uploadFile(Long roomId, Long uploaderId, FileUploadDto uploadDto) {
        log.info("Uploading file {} to room {} by user {}", uploadDto.getFilename(), roomId, uploaderId);
        
        // Verify room exists
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));
        
        // Map DTO to entity
        File file = fileMapper.toEntity(uploadDto);
        file.setRoom(room);
        file.setUploaderId(uploaderId);
        
        // Save file
        File savedFile = fileRepository.save(file);
        log.info("File uploaded successfully with ID: {}", savedFile.getId());
        
        return fileMapper.toResponseDto(savedFile);
    }
    
    public Page<FileResponseDto> getRoomFiles(Long roomId, File.FileCategory category, String search, 
                                            int page, int size) {
        log.debug("Getting files for room {}, category: {}, search: {}, page: {}, size: {}", 
                 roomId, category, search, page, size);
        
        // Verify room exists
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room", "id", roomId);
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<File> files;
        
        if (search != null && !search.trim().isEmpty()) {
            files = fileRepository.findByRoomIdAndSearchOrderByCreatedAtDesc(roomId, search.trim(), pageable);
        } else if (category != null) {
            files = fileRepository.findByRoomIdAndCategoryOrderByCreatedAtDesc(roomId, category, pageable);
        } else {
            files = fileRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable);
        }
        
        return files.map(fileMapper::toResponseDto);
    }
    
    @Transactional
    public FileResponseDto updateFile(Long roomId, Long fileId, Long userId, FileUpdateDto updateDto) {
        log.info("Updating file {} in room {} by user {}", fileId, roomId, userId);
        
        // Find file by ID and room ID
        File file = fileRepository.findByIdAndRoomId(fileId, roomId)
            .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));
        
        // Check if user has permission (uploader or room member)
        // For now, we'll allow all updates. In production, add proper authorization
        
        // Update file metadata
        fileMapper.updateFileFromDto(updateDto, file);
        
        File updatedFile = fileRepository.save(file);
        log.info("File updated successfully: {}", fileId);
        
        return fileMapper.toResponseDto(updatedFile);
    }
    
    @Transactional
    public void deleteFile(Long roomId, Long fileId, Long userId) {
        log.info("Deleting file {} from room {} by user {}", fileId, roomId, userId);
        
        // Find file by ID and room ID
        File file = fileRepository.findByIdAndRoomId(fileId, roomId)
            .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));
        
        // Check if user has permission (uploader or room admin)
        // For now, we'll allow all deletions. In production, add proper authorization
        
        fileRepository.delete(file);
        log.info("File deleted successfully: {}", fileId);
    }
    
    public List<FileResponseDto> getUserFiles(Long userId) {
        log.debug("Getting files uploaded by user: {}", userId);
        List<File> files = fileRepository.findByUploaderId(userId);
        return files.stream()
                   .map(fileMapper::toResponseDto)
                   .toList();
    }
    
    public long getRoomFileCount(Long roomId) {
        return fileRepository.countByRoomId(roomId);
    }
    
    public Long getRoomTotalFileSize(Long roomId) {
        return fileRepository.getTotalFileSizeByRoomId(roomId);
    }
}
