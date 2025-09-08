package com.thefreelancer.microservices.auth.controller;

import com.thefreelancer.microservices.auth.dto.UserResponseDto;
import com.thefreelancer.microservices.auth.service.UserService;
import com.thefreelancer.microservices.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("/api/auth/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<UserResponseDto> getCurrentUser(HttpServletRequest request) {
        // Log all headers to see what the gateway is sending
        log.debug("=== Headers received from gateway ===");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            log.debug("Header: {} = {}", headerName, request.getHeader(headerName));
        });
        log.debug("=====================================");
        
        // Try to get user ID from X-User-Id header first (from gateway)
        String userIdHeader = request.getHeader("X-User-Id");
        log.debug(userIdHeader);
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            log.debug("hihihihi");
            try {
                Long userId = Long.parseLong(userIdHeader);
                log.debug("Found user ID from header: {}", userId);
                Optional<UserResponseDto> user = userService.getUserById(userId);
                return user.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID in header: {}", userIdHeader);
            }
        }
        
        // Fallback: try to decode JWT from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No Authorization header or X-User-Id header found");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            String token = authHeader.substring(7);
            log.debug("Token received: {}", token.substring(0, Math.min(20, token.length())) + "...");
            
            // Extract user ID from JWT token first
            Long userId = jwtUtil.extractUserId(token);
            log.debug("Extracted user ID from JWT: {}", userId);
            
            // Validate token with extracted user ID
            if (jwtUtil.isTokenValid(token, userId)) {
                Optional<UserResponseDto> user = userService.getUserById(userId);
                return user.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
            } else {
                log.warn("Invalid JWT token for user ID: {}", userId);
                return ResponseEntity.status(401).build();
            }
        } catch (Exception e) {
            log.error("Error processing JWT token", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponseDto> getUserProfile(HttpServletRequest request) {
        // Try to get user ID from X-User-Id header first (from gateway)
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            try {
                Long userId = Long.parseLong(userIdHeader);
                log.debug("Found user ID from header: {}", userId);
                Optional<UserResponseDto> user = userService.getUserById(userId);
                return user.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID in header: {}", userIdHeader);
            }
        }
        
        // Fallback: decode JWT from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No Authorization header or X-User-Id header found");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            log.debug("Extracted user ID from JWT: {}", userId);
            
            if (jwtUtil.isTokenValid(token, userId)) {
                Optional<UserResponseDto> user = userService.getUserById(userId);
                return user.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
            } else {
                log.warn("Invalid JWT token for user ID: {}", userId);
                return ResponseEntity.status(401).build();
            }
        } catch (Exception e) {
            log.error("Error processing JWT token", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
