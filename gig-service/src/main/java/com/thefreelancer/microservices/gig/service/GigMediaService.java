package com.thefreelancer.microservices.gig.service;

// import com.thefreelancer.microservices.gig.model.GigMedia;
// import com.thefreelancer.microservices.gigservice.dto.gigmedia.GigMediaResponseDto;
// import com.thefreelancer.microservices.gigservice.mapper.GigMediaMapper;
// import com.thefreelancer.microservices.gigservice.repository.GigMediaRepository;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.thefreelancer.microservices.gig.model.GigMedia;
import com.thefreelancer.microservices.gig.dto.GigMediaResponseDto;
import com.thefreelancer.microservices.gig.mapper.GigMediaMapper;
import com.thefreelancer.microservices.gig.repository.GigMediaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GigMediaService {
    
    private final GigMediaRepository gigMediaRepository;
    private final CloudinaryService cloudinaryService;
    private final GigMediaMapper gigMediaMapper;
    
    public GigMediaResponseDto uploadMedia(Long gigId, MultipartFile file) throws IOException {
        log.info("Uploading media for gig ID: {}, file: {}", gigId, file.getOriginalFilename());

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Upload to Cloudinary (folder organized by gig)
        String folder = "gigs/" + gigId + "/files";
        CloudinaryService.CloudinaryUploadResult uploadResult = cloudinaryService.uploadFile(file, folder);
        
        // Get next order index
        Integer nextOrderIndex = gigMediaRepository.findMaxOrderIndexByGigId(gigId) + 1;
        
        // Map Cloudinary resource type to GigMedia.Kind
        GigMedia.Kind inferredKind = mapResourceTypeToKind(uploadResult.getResourceType());

        // Save to database
        GigMedia gigMedia = GigMedia.builder()
            .gigId(gigId)
            .url(uploadResult.getSecureUrl())
            .contentType(uploadResult.getFormat() != null ? uploadResult.getFormat() : file.getContentType())
            .kind(inferredKind)
            .orderIndex(nextOrderIndex)
            .build();
            
        GigMedia savedMedia = gigMediaRepository.save(gigMedia);
        
        log.info("Media uploaded successfully with ID: {}", savedMedia.getId());
        return gigMediaMapper.toResponseDto(savedMedia);
    }
    
    @Transactional(readOnly = true)
    public List<GigMediaResponseDto> getGigMedia(Long gigId) {
        log.info("Fetching media for gig ID: {}", gigId);
        List<GigMedia> mediaList = gigMediaRepository.findByGigIdOrderByOrderIndexAsc(gigId);
        return gigMediaMapper.toResponseDtoList(mediaList);
    }
    
    @Transactional(readOnly = true)
    public GigMediaResponseDto getMediaById(Long id, Long gigId) {
        log.info("Fetching media with ID: {} for gig ID: {}", id, gigId);
        GigMedia media = gigMediaRepository.findByIdAndGigId(id, gigId)
            .orElseThrow(() -> new IllegalArgumentException("Media not found with ID: " + id + " for gig: " + gigId));
        return gigMediaMapper.toResponseDto(media);
    }
    
    public void deleteMedia(Long id, Long gigId) throws IOException {
        log.info("Deleting media with ID: {} for gig ID: {}", id, gigId);
        
        GigMedia media = gigMediaRepository.findByIdAndGigId(id, gigId)
            .orElseThrow(() -> new IllegalArgumentException("Media not found with ID: " + id + " for gig: " + gigId));
        
        // Extract public ID from URL for Cloudinary deletion
        String publicId = extractPublicIdFromUrl(media.getUrl());
        String resourceType = determineCloudinaryResourceType(media.getKind());
        
        try {
            // Delete from Cloudinary
            cloudinaryService.deleteFile(publicId, resourceType);
        } catch (Exception e) {
            log.warn("Failed to delete file from Cloudinary, continuing with database deletion: {}", e.getMessage());
        }
        
        // Delete from database
        gigMediaRepository.deleteByIdAndGigId(id, gigId);
        
        log.info("Media deleted successfully with ID: {}", id);
    }
    
    // validateFileType removed: kind is now inferred from Cloudinary's resource type
    
    private GigMedia.Kind mapResourceTypeToKind(String resourceType) {
        if (resourceType == null) return GigMedia.Kind.DOCUMENT;
        return switch (resourceType) {
            case "image" -> GigMedia.Kind.IMAGE;
            case "video" -> GigMedia.Kind.VIDEO;
            default -> GigMedia.Kind.DOCUMENT;
        };
    }
    
    private String extractPublicIdFromUrl(String url) {
        // Extract public ID from Cloudinary URL
        // URL format: https://res.cloudinary.com/{cloud_name}/{resource_type}/{type}/v{version}/{public_id}.{format}
        int lastSlashIndex = url.lastIndexOf('/');
        int lastDotIndex = url.lastIndexOf('.');
        
        if (lastSlashIndex != -1 && lastDotIndex != -1 && lastDotIndex > lastSlashIndex) {
            String publicIdWithFolder = url.substring(url.indexOf("/gigs/"), lastDotIndex);
            return publicIdWithFolder;
        }
        
        throw new IllegalArgumentException("Cannot extract public ID from URL: " + url);
    }
    
    private String determineCloudinaryResourceType(GigMedia.Kind kind) {
        return switch (kind) {
            case IMAGE -> "image";
            case VIDEO -> "video";
            case DOCUMENT -> "raw";
        };
    }
}
