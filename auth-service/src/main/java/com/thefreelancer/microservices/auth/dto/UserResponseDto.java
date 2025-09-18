package com.thefreelancer.microservices.auth.dto;

import com.thefreelancer.microservices.auth.model.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDto {
    private Long id;
    private String email;
    private String name;
    private String handle;
    private User.Role role;
    private String country;
    private String timezone;
    private boolean isActive;
    private String stripeAccountId;
    private User.KycStatus kycStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Utility method to convert from User entity
    public static UserResponseDto fromUser(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setHandle(user.getHandle());
        dto.setRole(user.getRole());
        dto.setCountry(user.getCountry());
        dto.setTimezone(user.getTimezone());
        dto.setActive(user.isActive());
        dto.setStripeAccountId(user.getStripeAccountId());
        dto.setKycStatus(user.getKycStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
