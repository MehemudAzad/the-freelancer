package com.thefreelancer.microservices.workspace_service.dto;

import lombok.Data;

@Data
public class AuthUserSummaryDto {
    private String id;
    private String name;
    private String handle;
    private String email;
}
