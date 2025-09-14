package com.thefreelancer.microservices.workspace_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    
    private Long id;
    private String email;
    private String name;
    private String handle;
    private String role; // CLIENT, FREELANCER, ADMIN
    private Boolean isActive;
    
    // Helper method to get display name
    public String getDisplayName() {
        return name != null ? name : handle;
    }
    
    // Helper method to get formatted handle
    public String getFormattedHandle() {
        return handle != null && !handle.startsWith("@") ? "@" + handle : handle;
    }
}