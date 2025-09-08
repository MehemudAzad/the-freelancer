package com.thefreelancer.microservices.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.thefreelancer.microservices.auth.dto.LoginRequestDTO;
import com.thefreelancer.microservices.auth.dto.LoginResponseDTO;
import com.thefreelancer.microservices.auth.dto.RefreshTokenRequestDTO;
import com.thefreelancer.microservices.auth.dto.RegisterRequestDto;
import com.thefreelancer.microservices.auth.model.User;
import com.thefreelancer.microservices.auth.util.JwtUtil;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    //! register method with auto-login
    public LoginResponseDTO registerWithAutoLogin(RegisterRequestDto registerRequest) {
        log.info("Attempting registration with auto-login for email: {}", registerRequest.getEmail());
        
        // Create the user (this will throw exception if user already exists)
        User createdUser = userService.createUserEntity(registerRequest);
        
        // Generate tokens for the newly created user
        String accessToken = jwtUtil.generateToken(createdUser.getId(), createdUser.getEmail(), createdUser.getRole().toString());
        String refreshToken = jwtUtil.generateRefreshToken(createdUser.getId());

        // Create user info for response
        LoginResponseDTO.UserInfo userInfo = new LoginResponseDTO.UserInfo(
                createdUser.getId(),
                createdUser.getEmail(),
                createdUser.getName(),
                createdUser.getHandle(),
                createdUser.getRole().toString()
        );

        log.info("Registration with auto-login successful for user ID: {}", createdUser.getId());
        
        return new LoginResponseDTO(
                accessToken,
                refreshToken,
                86400L, // 24 hours in seconds
                userInfo
        );
    }

    //! login method
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Attempting login for email: {}", loginRequest.getEmail());
        
        //! Find user by email
        Optional<User> userOptional = userService.findByEmail(loginRequest.getEmail());
        if (userOptional.isEmpty()) {
            log.warn("Login failed: User not found for email: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userOptional.get();

        //! Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: Invalid password for email: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        //! Check if user is active
        if (!user.isActive()) {
            log.warn("Login failed: User account is inactive for email: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Account is inactive");
        }

        //? Generate tokens
        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().toString());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Create user info for response
        LoginResponseDTO.UserInfo userInfo = new LoginResponseDTO.UserInfo(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getHandle(),
                user.getRole().toString()
        );

        log.info("Login successful for user ID: {}", user.getId());
        
        return new LoginResponseDTO(
                accessToken,
                refreshToken,
                86400L, // 24 hours in seconds (hardcoded since JwtUtil doesn't expose this)
                userInfo
        );
    }

    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO refreshRequest) {
        log.info("Attempting token refresh");
        
        String refreshToken = refreshRequest.getRefreshToken();
        
        // Validate refresh token
        if (!jwtUtil.isRefreshTokenValid(refreshToken)) {
            log.warn("Token refresh failed: Invalid refresh token");
            throw new BadCredentialsException("Invalid refresh token");
        }

        // Extract user from refresh token
        String email = jwtUtil.extractEmail(refreshToken);
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            log.warn("Token refresh failed: User not found for email: {}", email);
            throw new BadCredentialsException("User not found");
        }

        User user = userOptional.get();

        // Check if user is still active
        if (!user.isActive()) {
            log.warn("Token refresh failed: User account is inactive for email: {}", email);
            throw new BadCredentialsException("Account is inactive");
        }


        //! Generate new tokens
        String newAccessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().toString());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Create user info for response
        LoginResponseDTO.UserInfo userInfo = new LoginResponseDTO.UserInfo(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getHandle(),
                user.getRole().toString()
        );

        log.info("Token refresh successful for user ID: {}", user.getId());
        
        return new LoginResponseDTO(
                newAccessToken,
                newRefreshToken,
                86400L, // 24 hours in seconds (hardcoded since JwtUtil doesn't expose this)
                userInfo
        );
    }

    public void logout(String accessToken) {
        // In a production system, you might want to add the token to a blacklist
        // For now, we'll just log the logout
        log.info("User logout");
        // TODO: Implement token blacklisting if needed
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
}