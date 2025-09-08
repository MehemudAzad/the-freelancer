package com.thefreelancer.microservices.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDto {

    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    private String email;

    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotEmpty(message = "Name cannot be empty")
    private String name;

    private String handle;
    private String country;
    private String timezone;
    
    // Role field - accepts "CLIENT" or "FREELANCER", defaults to "FREELANCER" if not provided
    private String role = "FREELANCER";
}
