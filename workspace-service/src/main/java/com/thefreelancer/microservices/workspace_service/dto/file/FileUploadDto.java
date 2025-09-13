package com.thefreelancer.microservices.workspace_service.dto.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadDto {
    @NotBlank(message = "Filename is required")
    private String filename;

    @NotBlank(message = "Original filename is required")
    private String originalFilename;

    @NotBlank(message = "URL is required")
    private String url;

    private String contentType;

    @Positive(message = "File size must be positive")
    private Long fileSize;

    private String checksum;
}
