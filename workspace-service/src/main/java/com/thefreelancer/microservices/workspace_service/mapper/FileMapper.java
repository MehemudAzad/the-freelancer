package com.thefreelancer.microservices.workspace_service.mapper;

import com.thefreelancer.microservices.workspace_service.dto.file.FileResponseDto;
import com.thefreelancer.microservices.workspace_service.dto.file.FileUpdateDto;
// FileUploadDto removed; multipart upload path constructs File entities directly
import com.thefreelancer.microservices.workspace_service.model.File;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FileMapper {
    
    @Mapping(target = "roomId", source = "room.id")
    FileResponseDto toResponseDto(File file);
    
    // legacy toEntity(FileUploadDto) removed. Create File entities in service for multipart uploads.
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "uploaderId", ignore = true)
    @Mapping(target = "url", ignore = true)
    @Mapping(target = "originalFilename", ignore = true)
    @Mapping(target = "contentType", ignore = true)
    @Mapping(target = "fileSize", ignore = true)
    @Mapping(target = "checksum", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateFileFromDto(FileUpdateDto updateDto, @MappingTarget File file);
}
