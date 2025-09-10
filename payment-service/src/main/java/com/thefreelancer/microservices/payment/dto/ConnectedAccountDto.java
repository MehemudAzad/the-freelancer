package com.thefreelancer.microservices.payment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectedAccountDto {
    
    private String accountId;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 2, message = "Country should be a 2-letter ISO code")
    private String country;
    
    private Boolean chargesEnabled;
    private Boolean payoutsEnabled;
    private Boolean detailsSubmitted;
    private String type;
}
