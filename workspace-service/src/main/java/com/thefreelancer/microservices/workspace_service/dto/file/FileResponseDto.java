package com.thefreelancer.microservices.workspace_service.dto.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponseDto {
    private Long id;
    private Long roomId;
    private Long uploaderId;
    private String filename;
    private String originalFilename;
    private String url;
    private String thumbnailUrl;
    private String cloudinaryPublicId;
    private String cloudinaryResourceType;
    private String contentType;
    private Long fileSize;
    private Long milestoneId;
    private String checksum;
    private LocalDateTime createdAt;
}
