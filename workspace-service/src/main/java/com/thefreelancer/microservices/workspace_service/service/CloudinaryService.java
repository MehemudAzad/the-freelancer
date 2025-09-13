package com.thefreelancer.microservices.workspace_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload file to Cloudinary
     */
    public CloudinaryUploadResult uploadFile(MultipartFile file, String folderPath, String resourceType) throws IOException {
        log.info("Uploading file {} to Cloudinary folder: {}", file.getOriginalFilename(), folderPath);

        // Generate unique public ID
        String publicId = generateUniquePublicId(file.getOriginalFilename());
        
        Map<String, Object> uploadParams = new HashMap<>();
        uploadParams.put("folder", folderPath);
        uploadParams.put("public_id", publicId);
        uploadParams.put("resource_type", resourceType);
        uploadParams.put("use_filename", true);
        uploadParams.put("unique_filename", false);
        uploadParams.put("overwrite", false);

        // Add specific parameters based on resource type
        if ("image".equals(resourceType)) {
            uploadParams.put("quality", "auto");
            uploadParams.put("fetch_format", "auto");
        } else if ("video".equals(resourceType)) {
            uploadParams.put("quality", "auto");
        } else {
            uploadParams.put("resource_type", "raw"); // For documents and other files
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(), uploadParams);
        
        CloudinaryUploadResult uploadResult = CloudinaryUploadResult.builder()
            .publicId((String) result.get("public_id"))
            .url((String) result.get("secure_url"))
            .originalUrl((String) result.get("url"))
            .format((String) result.get("format"))
            .resourceType((String) result.get("resource_type"))
            .bytes(((Number) result.get("bytes")).longValue())
            .width(result.get("width") != null ? ((Number) result.get("width")).intValue() : null)
            .height(result.get("height") != null ? ((Number) result.get("height")).intValue() : null)
            .createdAt((String) result.get("created_at"))
            .build();

        log.info("File uploaded successfully to Cloudinary: {}", uploadResult.getUrl());
        return uploadResult;
    }

    /**
     * Delete file from Cloudinary
     */
    public boolean deleteFile(String publicId, String resourceType) {
        try {
            log.info("Deleting file from Cloudinary: {}", publicId);
            
            Map<String, Object> deleteParams = new HashMap<>();
            deleteParams.put("resource_type", resourceType);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) cloudinary.uploader().destroy(publicId, deleteParams);
            String deleteResult = (String) result.get("result");
            
            boolean success = "ok".equals(deleteResult);
            log.info("File deletion result: {} for publicId: {}", deleteResult, publicId);
            
            return success;
        } catch (Exception e) {
            log.error("Error deleting file from Cloudinary: {}", publicId, e);
            return false;
        }
    }

    /**
     * Generate transformation URL for images
     */
    public String getTransformedImageUrl(String publicId, int width, int height, String crop) {
        try {
            return cloudinary.url()
                .transformation(new Transformation<>()
                    .width(width)
                    .height(height)
                    .crop(crop)
                    .quality("auto")
                    .fetchFormat("auto"))
                .generate(publicId);
        } catch (Exception e) {
            log.error("Error generating transformed URL for publicId: {}", publicId, e);
            return null;
        }
    }

    /**
     * Get thumbnail URL for images
     */
    public String getThumbnailUrl(String publicId) {
        return getTransformedImageUrl(publicId, 300, 300, "fill");
    }

    /**
     * Determine resource type based on file content type
     */
    public String determineResourceType(String contentType) {
        if (contentType == null) {
            return "raw";
        }
        
        if (contentType.startsWith("image/")) {
            return "image";
        } else if (contentType.startsWith("video/")) {
            return "video";
        } else {
            return "raw";
        }
    }

    /**
     * Generate unique public ID for file
     */
    private String generateUniquePublicId(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        if (originalFilename != null) {
            // Remove extension and special characters
            String baseName = originalFilename.replaceFirst("[.][^.]+$", "")
                .replaceAll("[^a-zA-Z0-9]", "_");
            return baseName + "_" + timestamp + "_" + uuid;
        }
        
        return "file_" + timestamp + "_" + uuid;
    }

    /**
     * Result class for Cloudinary upload
     */
    @lombok.Data
    @lombok.Builder
    public static class CloudinaryUploadResult {
        private String publicId;
        private String url;
        private String originalUrl;
        private String format;
        private String resourceType;
        private Long bytes;
        private Integer width;
        private Integer height;
        private String createdAt;
    }
}
