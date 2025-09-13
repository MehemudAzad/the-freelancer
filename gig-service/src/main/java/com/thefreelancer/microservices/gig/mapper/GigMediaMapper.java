package com.thefreelancer.microservices.gig.mapper;

import com.thefreelancer.microservices.gig.model.GigMedia;
import com.thefreelancer.microservices.gig.dto.GigMediaResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GigMediaMapper {
    
    GigMediaResponseDto toResponseDto(GigMedia gigMedia);
    
    List<GigMediaResponseDto> toResponseDtoList(List<GigMedia> gigMediaList);
}
