package com.thefreelancer.microservices.auth.service;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.stream.Collectors;

// import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.thefreelancer.microservices.auth.client.GigServiceClient;
import com.thefreelancer.microservices.auth.dto.ProfileResponseDto;
import com.thefreelancer.microservices.auth.dto.RegisterRequestDto;
import com.thefreelancer.microservices.auth.dto.UserResponseDto;
import com.thefreelancer.microservices.auth.dto.UserWithProfileResponseDto;
import com.thefreelancer.microservices.auth.event.UserCreatedEvent;
import com.thefreelancer.microservices.auth.model.User;
import com.thefreelancer.microservices.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // inside event publisher we have kafkaTemplate
    // private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;
    private final EventPublisher eventPublisher;
    private final GigServiceClient gigServiceClient;

    @Transactional
    public UserResponseDto createUser(RegisterRequestDto registerRequest) {
        User savedUser = createUserEntity(registerRequest);
        // Convert to response DTO
        return UserResponseDto.fromUser(savedUser);
    }

    @Transactional
    public User createUserEntity(RegisterRequestDto registerRequest) {
        // Check if user already exists
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        // Create new user entity
        // Parse and validate the role
        User.Role userRole;
        try {
            userRole = User.Role.valueOf(registerRequest.getRole().toUpperCase());
            // Only allow CLIENT and FREELANCER roles for registration
            if (userRole != User.Role.CLIENT && userRole != User.Role.FREELANCER) {
                log.warn("Invalid role for registration: {}, defaulting to FREELANCER", registerRequest.getRole());
                userRole = User.Role.FREELANCER;
            }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role provided: {}, defaulting to FREELANCER", registerRequest.getRole());
            userRole = User.Role.FREELANCER;
        }
        
        User user = User.builder()
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .name(registerRequest.getName())
                .handle(registerRequest.getHandle())
                .country(registerRequest.getCountry())
                .timezone(registerRequest.getTimezone())
                .role(userRole)
                .build();

        // Save user to database
        User savedUser = userRepository.save(user);
        log.info("Successfully saved user with ID: {}", savedUser.getId());

        // Publish event for other services
        UserCreatedEvent event = new UserCreatedEvent(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getRole().toString(),
                savedUser.getCreatedAt()
        );
        eventPublisher.publishUserCreated(event);

        return savedUser;
    }

    public java.util.List<UserResponseDto> getUsersByIds(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) return java.util.Collections.emptyList();
        java.util.List<User> users = userRepository.findAllById(ids);
        return users.stream().map(UserResponseDto::fromUser).collect(java.util.stream.Collectors.toList());
    }

    public Optional<UserResponseDto> getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponseDto::fromUser);
    }

    public Optional<UserResponseDto> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserResponseDto::fromUser);
    }

    public Optional<UserResponseDto> getUserByHandle(String handle) {
        // Try exact match first
        Optional<User> exact = userRepository.findByHandle(handle);
        if (exact.isPresent()) {
            return exact.map(UserResponseDto::fromUser);
        }

        // Fallback: case-insensitive prefix match (returns first match)
        Optional<User> prefix = userRepository.findTopByHandleIgnoreCaseStartingWith(handle);
        return prefix.map(UserResponseDto::fromUser);
    }

    /**
     * Search users by handle prefix (case-insensitive) with paging.
     */
    public Page<UserResponseDto> searchUsersByHandle(String prefix, Pageable pageable) {
        Page<User> users = userRepository.findByHandleStartingWithIgnoreCase(prefix, pageable);
        return new PageImpl<>(
                users.stream().map(UserResponseDto::fromUser).collect(Collectors.toList()),
                pageable,
                users.getTotalElements()
        );
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get user with profile information from the gig service.
     */
    public Optional<UserWithProfileResponseDto> getUserWithProfileById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        
        User user = userOpt.get();
        
        // Try to get profile from gig service
        Optional<ProfileResponseDto> profileOpt = gigServiceClient.getProfileByUserId(id);
        
        // Create combined DTO
        UserWithProfileResponseDto userWithProfile = UserWithProfileResponseDto.fromUserAndProfile(
            user, 
            profileOpt.orElse(null)
        );
        
        return Optional.of(userWithProfile);
    }
}