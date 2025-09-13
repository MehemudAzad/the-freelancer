package com.thefreelancer.microservices.gig.controller;

import com.thefreelancer.microservices.gig.model.GigMedia;
import com.thefreelancer.microservices.gig.service.GigMediaService;
import com.thefreelancer.microservices.gig.dto.GigMediaResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/gigs/{gigId}/media")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gig Media", description = "Gig media management APIs")
public class GigMediaController {
    
    private final GigMediaService gigMediaService;
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload media for a gig", description = "Upload image, video, or document for a gig")
    public ResponseEntity<GigMediaResponseDto> uploadMedia(
            @PathVariable @NotNull Long gigId,
            @RequestParam @NotNull @Parameter(description = "Media file to upload") MultipartFile file) {
        
        try {
            log.info("Uploading media for gig ID: {}", gigId);
            GigMediaResponseDto response = gigMediaService.uploadMedia(gigId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for media upload: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (IOException e) {
            log.error("Failed to upload media: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            
        } catch (Exception e) {
            log.error("Unexpected error during media upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping
    @Operation(summary = "Get all media for a gig", description = "Retrieve all media files for a specific gig")
    public ResponseEntity<List<GigMediaResponseDto>> getGigMedia(@PathVariable @NotNull Long gigId) {
        try {
            log.info("Fetching media for gig ID: {}", gigId);
            List<GigMediaResponseDto> mediaList = gigMediaService.getGigMedia(gigId);
            return ResponseEntity.ok(mediaList);
            
        } catch (Exception e) {
            log.error("Failed to fetch media for gig {}: {}", gigId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{mediaId}")
    @Operation(summary = "Get specific media by ID", description = "Retrieve a specific media file by its ID")
    public ResponseEntity<GigMediaResponseDto> getMediaById(
            @PathVariable @NotNull Long gigId,
            @PathVariable @NotNull Long mediaId) {
        
        try {
            log.info("Fetching media ID: {} for gig ID: {}", mediaId, gigId);
            GigMediaResponseDto media = gigMediaService.getMediaById(mediaId, gigId);
            return ResponseEntity.ok(media);
            
        } catch (IllegalArgumentException e) {
            log.error("Media not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Failed to fetch media {}: {}", mediaId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{mediaId}")
    @Operation(summary = "Delete media", description = "Delete a specific media file")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable @NotNull Long gigId,
            @PathVariable @NotNull Long mediaId) {
        
        try {
            log.info("Deleting media ID: {} for gig ID: {}", mediaId, gigId);
            gigMediaService.deleteMedia(mediaId, gigId);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            log.error("Media not found for deletion: {}", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (IOException e) {
            log.error("Failed to delete media from cloud storage: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            
        } catch (Exception e) {
            log.error("Failed to delete media {}: {}", mediaId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
