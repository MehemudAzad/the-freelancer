package com.thefreelancer.microservices.workspace_service.dto.file;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMultipartUploadDto {
    
    @NotNull(message = "File is required")
    private MultipartFile file;
}
