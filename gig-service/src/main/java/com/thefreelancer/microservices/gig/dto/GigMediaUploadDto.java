package com.thefreelancer.microservices.gig.dto;

import com.thefreelancer.microservices.gig.model.GigMedia;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GigMediaUploadDto {
    
    @NotNull(message = "Gig ID is required")
    private Long gigId;
    
    @NotNull(message = "Media kind is required")
    private GigMedia.Kind kind;
    
    // File will be passed as MultipartFile in controller
    // Content type and order index will be determined automatically
}
