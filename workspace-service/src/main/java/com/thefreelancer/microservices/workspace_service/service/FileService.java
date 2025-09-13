package com.thefreelancer.microservices.workspace_service.service;

import com.thefreelancer.microservices.workspace_service.dto.file.FileMultipartUploadDto;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {
    
    private final FileRepository fileRepository;
    private final RoomRepository roomRepository;
    private final FileMapper fileMapper;
    private final CloudinaryService cloudinaryService;
    
    // @Transactional
    // public FileResponseDto uploadFile(Long roomId, Long uploaderId, FileUploadDto uploadDto) {
    //     log.info("Uploading file {} to room {} by user {}", uploadDto.getFilename(), roomId, uploaderId);
        
    //     // Verify room exists
    //     Room room = roomRepository.findById(roomId)
    //         .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));
        
    //     // Map DTO to entity
    //     File file = fileMapper.toEntity(uploadDto);
    //     file.setRoom(room);
    //     file.setUploaderId(uploaderId);
        
    //     // Save file
    //     File savedFile = fileRepository.save(file);
    //     log.info("File uploaded successfully with ID: {}", savedFile.getId());
        
    //     return fileMapper.toResponseDto(savedFile);
    // }
    
    @Transactional
    public FileResponseDto uploadFileToCloudinary(Long roomId, Long uploaderId, FileMultipartUploadDto uploadDto) throws IOException {
        MultipartFile file = uploadDto.getFile();
        log.info("Uploading file {} to Cloudinary for room {} by user {}", 
                file.getOriginalFilename(), roomId, uploaderId);
        
        // Verify room exists
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));
        
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        // Determine resource type for Cloudinary
        String resourceType = cloudinaryService.determineResourceType(file.getContentType());
        String folderPath = "workspace/" + roomId + "/files";
        
        // Upload to Cloudinary
        CloudinaryService.CloudinaryUploadResult uploadResult = cloudinaryService.uploadFile(
            file, folderPath, resourceType
        );
        
        // Calculate checksum
        String checksum = calculateChecksum(file.getBytes());
        
        // Create file entity
        File fileEntity = File.builder()
            .room(room)
            .uploaderId(uploaderId)
            .filename(uploadResult.getPublicId())
            .originalFilename(file.getOriginalFilename())
            .url(uploadResult.getUrl())
            .thumbnailUrl(resourceType.equals("image") ? 
                cloudinaryService.getThumbnailUrl(uploadResult.getPublicId()) : null)
            .cloudinaryPublicId(uploadResult.getPublicId())
            .cloudinaryResourceType(uploadResult.getResourceType())
            .contentType(file.getContentType())
            .fileSize(uploadResult.getBytes())
            .checksum(checksum)
            .build();
        
        // Save file
        File savedFile = fileRepository.save(fileEntity);
        log.info("File uploaded successfully to Cloudinary with ID: {}", savedFile.getId());
        
        return fileMapper.toResponseDto(savedFile);
    }

    public Page<FileResponseDto> getRoomFiles(Long roomId, String search,
                                            int page, int size) {
        log.debug("Getting files for room {}, search: {}, page: {}, size: {}", 
                 roomId, search, page, size);
        
        // Verify room exists
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room", "id", roomId);
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<File> files;
        
        if (search != null && !search.trim().isEmpty()) {
            files = fileRepository.findByRoomIdAndSearchOrderByCreatedAtDesc(roomId, search.trim(), pageable);
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
        
        // Delete from Cloudinary if it's a Cloudinary file
        if (file.getCloudinaryPublicId() != null && file.getCloudinaryResourceType() != null) {
            boolean cloudinaryDeleted = cloudinaryService.deleteFile(
                file.getCloudinaryPublicId(), 
                file.getCloudinaryResourceType()
            );
            
            if (!cloudinaryDeleted) {
                log.warn("Failed to delete file from Cloudinary: {}", file.getCloudinaryPublicId());
            }
        }
        
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
    
    /**
     * Calculate SHA-256 checksum for file integrity
     */
    private String calculateChecksum(byte[] fileBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileBytes);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.warn("SHA-256 algorithm not available, using simple hash", e);
            return String.valueOf(java.util.Arrays.hashCode(fileBytes));
        }
    }
}
