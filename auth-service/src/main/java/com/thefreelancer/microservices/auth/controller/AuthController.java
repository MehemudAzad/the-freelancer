package com.thefreelancer.microservices.auth.controller;

import com.thefreelancer.microservices.auth.dto.RegisterRequestDto;
import com.thefreelancer.microservices.auth.dto.UserResponseDto;
import com.thefreelancer.microservices.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        UserResponseDto createdUser = userService.createUser(registerRequest);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        Optional<UserResponseDto> user = userService.getUserById(id);
        return user.map(u -> ResponseEntity.ok(u))
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/email/{email}")
    public ResponseEntity<UserResponseDto> getUserByEmail(@PathVariable String email) {
        Optional<UserResponseDto> user = userService.getUserByEmail(email);
        return user.map(u -> ResponseEntity.ok(u))
                   .orElse(ResponseEntity.notFound().build());
    }
}
