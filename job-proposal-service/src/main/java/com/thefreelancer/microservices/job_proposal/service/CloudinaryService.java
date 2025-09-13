package com.thefreelancer.microservices.job_proposal.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryUploadResult uploadFile(MultipartFile file, String folder) throws IOException {
        log.info("Uploading file {} to folder {}", file.getOriginalFilename(), folder);

        String resourceType = determineResourceType(file.getContentType());

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", resourceType,
                "use_filename", true,
                "unique_filename", true,
                "overwrite", false
        );

        if ("image".equals(resourceType)) {
            uploadParams.put("quality", "auto:best");
            uploadParams.put("fetch_format", "auto");
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            return CloudinaryUploadResult.builder()
                    .publicId((String) result.get("public_id"))
                    .url((String) result.get("url"))
                    .secureUrl((String) result.get("secure_url"))
                    .resourceType(resourceType)
                    .format((String) result.get("format"))
                    .bytes(result.get("bytes") instanceof Integer ? (Integer) result.get("bytes") : result.get("bytes") instanceof Long ? ((Long) result.get("bytes")).intValue() : null)
                    .width(result.get("width") instanceof Integer ? (Integer) result.get("width") : null)
                    .height(result.get("height") instanceof Integer ? (Integer) result.get("height") : null)
                    .build();

        } catch (Exception e) {
            log.error("Error uploading file to Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to Cloudinary", e);
        }
    }

    public void deleteFile(String publicId, String resourceType) throws IOException {
        log.info("Deleting file with public ID: {} of type: {}", publicId, resourceType);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> params = ObjectUtils.asMap("resource_type", resourceType);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, params);

            String deleteResult = (String) result.get("result");
            if (!"ok".equals(deleteResult)) {
                log.warn("File deletion may have failed. Result: {}", deleteResult);
            } else {
                log.info("File deleted successfully: {}", publicId);
            }

        } catch (Exception e) {
            log.error("Error deleting file from Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Failed to delete file from Cloudinary", e);
        }
    }

    private String determineResourceType(String contentType) {
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

    @Builder
    @Data
    public static class CloudinaryUploadResult {
        private String publicId;
        private String url;
        private String secureUrl;
        private String resourceType;
        private String format;
        private Integer bytes;
        private Integer width;
        private Integer height;
    }
}
