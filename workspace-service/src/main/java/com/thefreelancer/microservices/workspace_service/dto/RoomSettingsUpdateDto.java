package com.thefreelancer.microservices.workspace_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomSettingsUpdateDto {
    
    private Boolean notificationsEnabled;
    private String permissionLevel; // READ_ONLY, READ_WRITE
    private String description;
}
