package com.thefreelancer.microservices.workspace_service.controller;

import com.thefreelancer.microservices.workspace_service.dto.file.FileResponseDto;
import com.thefreelancer.microservices.workspace_service.dto.file.FileUpdateDto;
import com.thefreelancer.microservices.workspace_service.dto.file.FileUploadDto;
import com.thefreelancer.microservices.workspace_service.model.File;
import com.thefreelancer.microservices.workspace_service.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/workspaces/rooms/{roomId}/files")
@RequiredArgsConstructor
@Tag(name = "File Collaboration", description = "File management within workspace rooms")
public class FileController {
    
    private final FileService fileService;
    
    @PostMapping
    @Operation(summary = "Upload file to room", description = "Upload a new file to a workspace room")
    @ApiResponse(responseCode = "201", description = "File uploaded successfully")
    @ApiResponse(responseCode = "404", description = "Room not found")
    public ResponseEntity<FileResponseDto> uploadFile(
            @Parameter(description = "Room ID") @PathVariable Long roomId,
            @Valid @RequestBody FileUploadDto uploadDto,
            HttpServletRequest request) {
        
        Long uploaderId = extractUserIdFromRequest(request);
        log.info("File upload request for room {} by user {}", roomId, uploaderId);
        
        FileResponseDto response = fileService.uploadFile(roomId, uploaderId, uploadDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "Get room files", description = "Get paginated list of files in a room with optional filtering")
    @ApiResponse(responseCode = "200", description = "Files retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Room not found")
    public ResponseEntity<Page<FileResponseDto>> getRoomFiles(
            @Parameter(description = "Room ID") @PathVariable Long roomId,
            @Parameter(description = "Filter by file category") @RequestParam(required = false) File.FileCategory category,
            @Parameter(description = "Search in filename and description") @RequestParam(required = false) String search,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting files for room {}, category: {}, search: {}", roomId, category, search);
        
        Page<FileResponseDto> files = fileService.getRoomFiles(roomId, category, search, page, size);
        return ResponseEntity.ok(files);
    }
    
    @PutMapping("/{fileId}")
    @Operation(summary = "Update file metadata", description = "Update file information like name, category, or description")
    @ApiResponse(responseCode = "200", description = "File updated successfully")
    @ApiResponse(responseCode = "404", description = "File or room not found")
    public ResponseEntity<FileResponseDto> updateFile(
            @Parameter(description = "Room ID") @PathVariable Long roomId,
            @Parameter(description = "File ID") @PathVariable Long fileId,
            @Valid @RequestBody FileUpdateDto updateDto,
            HttpServletRequest request) {
        
        Long userId = extractUserIdFromRequest(request);
        log.info("File update request for file {} in room {} by user {}", fileId, roomId, userId);
        
        FileResponseDto response = fileService.updateFile(roomId, fileId, userId, updateDto);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{fileId}")
    @Operation(summary = "Delete file", description = "Delete a file from the room")
    @ApiResponse(responseCode = "204", description = "File deleted successfully")
    @ApiResponse(responseCode = "404", description = "File or room not found")
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "Room ID") @PathVariable Long roomId,
            @Parameter(description = "File ID") @PathVariable Long fileId,
            HttpServletRequest request) {
        
        Long userId = extractUserIdFromRequest(request);
        log.info("File deletion request for file {} in room {} by user {}", fileId, roomId, userId);
        
        fileService.deleteFile(roomId, fileId, userId);
        return ResponseEntity.noContent().build();
    }
    
    private Long extractUserIdFromRequest(HttpServletRequest request) {
        // Extract user ID from X-User-Id header (set by gateway) or JWT token
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID in header: {}", userIdHeader);
            }
        }
        
        // Fallback: For now return a default user ID
        // In production, implement JWT token extraction
        log.warn("No user ID found in request, using default");
        return 1L; // Default user for testing
    }
}
