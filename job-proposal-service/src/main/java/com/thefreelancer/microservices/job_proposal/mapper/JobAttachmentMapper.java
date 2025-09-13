package com.thefreelancer.microservices.job_proposal.mapper;

import com.thefreelancer.microservices.job_proposal.dto.JobAttachmentCreateDto;
import com.thefreelancer.microservices.job_proposal.dto.JobAttachmentResponseDto;
import com.thefreelancer.microservices.job_proposal.model.JobAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface JobAttachmentMapper {
    
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "kind", source = "kind", qualifiedByName = "kindToString")
    @Mapping(target = "contentType", source = "contentType")
    @Mapping(target = "cloudinaryPublicId", source = "cloudinaryPublicId")
    @Mapping(target = "cloudinaryResourceType", source = "cloudinaryResourceType")
    @Mapping(target = "checksum", source = "checksum")
    JobAttachmentResponseDto toResponseDto(JobAttachment jobAttachment);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    JobAttachment toEntity(JobAttachmentCreateDto createDto);
    
    @Named("kindToString")
    default String kindToString(JobAttachment.AttachmentKind kind) {
        return kind != null ? kind.toString() : null;
    }
}
