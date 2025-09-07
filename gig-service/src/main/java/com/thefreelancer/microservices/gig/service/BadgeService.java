package com.thefreelancer.microservices.gig.service;

import com.thefreelancer.microservices.gig.dto.BadgeCreateDto;
import com.thefreelancer.microservices.gig.dto.BadgeResponseDto;
import com.thefreelancer.microservices.gig.mapper.BadgeMapper;
import com.thefreelancer.microservices.gig.model.ProfileBadge;
import com.thefreelancer.microservices.gig.repository.ProfileBadgeRepository;
import com.thefreelancer.microservices.gig.repository.ProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {
    
    private final ProfileBadgeRepository badgeRepository;
    private final ProfileRepository profileRepository;
    private final BadgeMapper badgeMapper;
    
    @Transactional
    public BadgeResponseDto addBadgeToProfile(Long userId, BadgeCreateDto createDto) {
        log.info("Adding badge to profile for userId: {}", userId);
        
        // Check if profile exists
        if (profileRepository.findByUserId(userId).isEmpty()) {
            throw new RuntimeException("Profile not found for userId: " + userId);
        }
        
        // Check if badge type already exists for this user
        if (badgeRepository.existsByUserIdAndType(userId, createDto.getType())) {
            throw new RuntimeException("Badge of type '" + createDto.getType() + "' already exists for this user");
        }
        
        ProfileBadge badge = badgeMapper.toEntity(createDto);
        badge.setUserId(userId);
        badge.setIssuedAt(LocalDateTime.now());
        
        // Set expiration date (1 year from now for skill badges)
        if (createDto.getType().toLowerCase().contains("skill")) {
            badge.setExpiresAt(LocalDateTime.now().plusYears(1));
        }
        
        ProfileBadge savedBadge = badgeRepository.save(badge);
        log.info("Successfully added badge {} to userId: {}", createDto.getType(), userId);
        
        return badgeMapper.toResponseDto(savedBadge);
    }
    
    @Transactional
    public void removeBadgeFromProfile(Long userId, Long badgeId) {
        log.info("Removing badge {} from profile for userId: {}", badgeId, userId);
        
        // Check if profile exists
        if (profileRepository.findByUserId(userId).isEmpty()) {
            throw new RuntimeException("Profile not found for userId: " + userId);
        }
        
        badgeRepository.deleteByIdAndUserId(badgeId, userId);
        log.info("Successfully removed badge {} from userId: {}", badgeId, userId);
    }
    
    public List<BadgeResponseDto> getUserBadges(Long userId) {
        log.info("Fetching badges for userId: {}", userId);
        
        return badgeRepository.findByUserId(userId)
                .stream()
                .map(badgeMapper::toResponseDto)
                .toList();
    }
}
