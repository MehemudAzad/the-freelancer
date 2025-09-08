package com.thefreelancer.microservices.auth.service;

import java.util.Optional;

// import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.thefreelancer.microservices.auth.dto.RegisterRequestDto;
import com.thefreelancer.microservices.auth.dto.UserResponseDto;
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
        User user = User.builder()
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .name(registerRequest.getName())
                .handle(registerRequest.getHandle())
                .country(registerRequest.getCountry())
                .timezone(registerRequest.getTimezone())
                .role(User.Role.FREELANCER) // Default role
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

    public Optional<UserResponseDto> getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponseDto::fromUser);
    }

    public Optional<UserResponseDto> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserResponseDto::fromUser);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}