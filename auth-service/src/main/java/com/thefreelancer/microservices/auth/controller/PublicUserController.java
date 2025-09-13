package com.thefreelancer.microservices.auth.controller;

import com.thefreelancer.microservices.auth.dto.UserResponseDto;
import com.thefreelancer.microservices.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth/public/users")
@RequiredArgsConstructor
@Slf4j
public class PublicUserController {

    private final UserService userService;

    @GetMapping("/username/{handle}")
    public ResponseEntity<UserResponseDto> getUserByUsername(@PathVariable String handle) {
        log.debug("Getting user by username: {}", handle);
        
        Optional<UserResponseDto> user = userService.getUserByHandle(handle);
        
        if (user.isPresent()) {
            log.info("Successfully found user by username: {}", handle);
            return ResponseEntity.ok(user.get());
        } else {
            log.warn("User not found with username: {}", handle);
            return ResponseEntity.notFound().build();
        }
    }
}
