package com.thefreelancer.microservices.workspace_service.dto;

import com.thefreelancer.microservices.workspace_service.model.Room;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatusUpdateDto {
    
    @NotNull(message = "Status is required")
    private Room.RoomStatus status;
}
