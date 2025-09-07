package com.thefreelancer.microservices.gig.service;

import com.thefreelancer.microservices.gig.model.Profile;
import com.thefreelancer.microservices.gig.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {
    
    private final ProfileRepository profileRepository;
    
    public Profile createInitialProfile(Long userId, String name, String role) {
        // Check if profile already exists
        if (profileRepository.findByUserId(userId).isPresent()) {
            log.warn("Profile already exists for userId: {}", userId);
            return profileRepository.findByUserId(userId).get();
        }
        
        // Create initial profile
        Profile profile = Profile.builder()
                .userId(userId)
                .headline("New " + role.toLowerCase() + " on the platform")
                .bio("Hi! I'm " + name + ". I'm excited to start working on projects!")
                .currency("USD")
                .build();
        
        Profile savedProfile = profileRepository.save(profile);
        log.info("Created initial profile for userId: {}", userId);
        
        return savedProfile;
    }
}
