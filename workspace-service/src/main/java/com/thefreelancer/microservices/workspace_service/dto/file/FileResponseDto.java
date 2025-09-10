package com.thefreelancer.microservices.workspace_service.dto.file;

import com.thefreelancer.microservices.workspace_service.model.File;
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
    private String contentType;
    private Long fileSize;
    private File.FileCategory category;
    private String description;
    private String checksum;
    private LocalDateTime createdAt;
}
