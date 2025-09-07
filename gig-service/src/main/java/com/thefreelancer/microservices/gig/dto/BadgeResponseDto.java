package com.thefreelancer.microservices.gig.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BadgeResponseDto {
    private Long id;
    private String type;
    private Double score;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
}
