package com.thefreelancer.microservices.gig.dto;

import com.thefreelancer.microservices.gig.model.GigMedia;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GigMediaResponseDto {
    
    private Long id;
    private Long gigId;
    private String url;
    private String contentType;
    private GigMedia.Kind kind;
    private Integer orderIndex;
    private LocalDateTime createdAt;
}
