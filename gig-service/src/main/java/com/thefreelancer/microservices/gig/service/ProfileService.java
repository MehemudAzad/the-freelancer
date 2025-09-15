package com.thefreelancer.microservices.gig.service;

import com.thefreelancer.microservices.gig.dto.ProfileResponseDto;
import com.thefreelancer.microservices.gig.dto.ProfileUpdateDto;
import com.thefreelancer.microservices.gig.mapper.ProfileMapper;
import com.thefreelancer.microservices.gig.model.Profile;
import com.thefreelancer.microservices.gig.repository.ProfileRepository;
import com.thefreelancer.microservices.gig.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {
    @Transactional
    public Optional<ProfileResponseDto> updateProfilePictureUrl(Long userId, String profilePictureUrl) {
        Optional<Profile> profileOpt = profileRepository.findByUserId(userId);
        if (profileOpt.isPresent()) {
            Profile profile = profileOpt.get();
            profile.setProfilePictureUrl(profilePictureUrl);
            profileRepository.save(profile);
            return Optional.of(profileMapper.toResponseDto(profile));
        }
        return Optional.empty();
    }
    
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final ReviewRepository reviewRepository;
    
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
    
    public Optional<ProfileResponseDto> getProfileByUserId(Long userId) {
        log.info("Fetching profile for userId: {}", userId);
        
        return profileRepository.findByUserId(userId)
                .map(profileMapper::toResponseDto);
    }
    
    public Optional<ProfileResponseDto> updateProfile(Long userId, ProfileUpdateDto updateDto) {
        log.info("Updating profile for userId: {}", userId);
        
        Optional<Profile> profileOpt = profileRepository.findByUserId(userId);
        if (profileOpt.isEmpty()) {
            log.warn("Profile not found for userId: {}", userId);
            return Optional.empty();
        }
        
        Profile profile = profileOpt.get();
        profileMapper.updateProfileFromDto(updateDto, profile);
        
        Profile updatedProfile = profileRepository.save(profile);
        log.info("Successfully updated profile for userId: {}", userId);
        
        return Optional.of(profileMapper.toResponseDto(updatedProfile));
    }
    
    @Transactional
    public void updateProfileRatings(Long userId) {
        log.info("Updating profile ratings for userId: {}", userId);
        
        Optional<Profile> profileOpt = profileRepository.findByUserId(userId);
        if (profileOpt.isEmpty()) {
            log.warn("Profile not found for userId: {}", userId);
            return;
        }
        
        // Calculate average rating from all reviews where this user is the freelancer
        Double averageRating = reviewRepository.calculateAverageRatingForFreelancer(userId);
        Long totalReviews = reviewRepository.countReviewsForFreelancer(userId);
        
        Profile profile = profileOpt.get();
        profile.setReviewAvg(averageRating != null ? 
            BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO);
        profile.setReviewsCount(totalReviews != null ? totalReviews.intValue() : 0);
        
        profileRepository.save(profile);
        log.info("Updated profile ratings for userId: {} - Avg: {}, Count: {}", 
                userId, profile.getReviewAvg(), profile.getReviewsCount());
    }
}
