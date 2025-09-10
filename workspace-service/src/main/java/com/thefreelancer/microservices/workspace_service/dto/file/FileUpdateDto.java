package com.thefreelancer.microservices.workspace_service.dto.file;

import com.thefreelancer.microservices.workspace_service.model.File;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUpdateDto {
    private String filename;
    private File.FileCategory category;
    private String description;
}
