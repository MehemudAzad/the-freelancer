package com.thefreelancer.microservices.gig.service;

import com.thefreelancer.microservices.gig.dto.GigCreateDto;
import com.thefreelancer.microservices.gig.dto.GigResponseDto;
import com.thefreelancer.microservices.gig.mapper.GigMapper;
import com.thefreelancer.microservices.gig.model.Gig;
import com.thefreelancer.microservices.gig.repository.GigRepository;
import com.thefreelancer.microservices.gig.repository.ProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GigService {
    
    private final GigRepository gigRepository;
    private final ProfileRepository profileRepository;
    private final GigMapper gigMapper;
    
    @Transactional
    public GigResponseDto createGig(Long userId, GigCreateDto createDto) {
        log.info("Creating gig for userId: {}", userId);
        
        // Check if profile exists
        if (profileRepository.findByUserId(userId).isEmpty()) {
            throw new RuntimeException("Profile not found for userId: " + userId);
        }
        
        Gig gig = gigMapper.toEntity(createDto);
        gig.setProfileId(userId); // Using userId as profileId since profile.userId is the primary key
        gig.setStatus(Gig.Status.DRAFT); // New gigs start as draft
        
        Gig savedGig = gigRepository.save(gig);
        log.info("Successfully created gig with ID: {} for userId: {}", savedGig.getId(), userId);
        
        return gigMapper.toResponseDto(savedGig);
    }
    
    public Optional<GigResponseDto> getGigById(Long gigId) {
        log.info("Fetching gig with ID: {}", gigId);
        
        return gigRepository.findById(gigId)
                .map(gigMapper::toResponseDto);
    }
    
    public List<GigResponseDto> getGigsByUserId(Long userId) {
        log.info("Fetching gigs for userId: {}", userId);
        
        return gigRepository.findByProfileId(userId)
                .stream()
                .map(gigMapper::toResponseDto)
                .toList();
    }
    
    public List<GigResponseDto> getActiveGigs() {
        log.info("Fetching all active gigs");
        
        return gigRepository.findByStatus(Gig.Status.ACTIVE)
                .stream()
                .map(gigMapper::toResponseDto)
                .toList();
    }
    
    public List<GigResponseDto> getGigsByCategory(String category) {
        log.info("Fetching gigs by category: {}", category);
        
        return gigRepository.findActiveGigsByCategory(category)
                .stream()
                .map(gigMapper::toResponseDto)
                .toList();
    }
    
    public List<GigResponseDto> searchGigsByTags(List<String> tags) {
        log.info("Searching gigs by tags: {}", tags);
        
        return gigRepository.findActiveGigsByTags(tags)
                .stream()
                .map(gigMapper::toResponseDto)
                .toList();
    }
}
