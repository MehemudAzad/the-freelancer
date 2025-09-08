package com.thefreelancer.microservices.gig.service;

import com.thefreelancer.microservices.gig.dto.GigCreateDto;
import com.thefreelancer.microservices.gig.dto.GigResponseDto;
import com.thefreelancer.microservices.gig.dto.GigUpdateDto;
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

    public List<GigResponseDto> getMyGigs(Long authenticatedUserId, String status) {
        log.info("Fetching my gigs for authenticated userId: {} with status filter: {}", authenticatedUserId, status);
        
        if (status != null && !status.trim().isEmpty()) {
            try {
                Gig.Status gigStatus = Gig.Status.valueOf(status.toUpperCase());
                return gigRepository.findByProfileIdAndStatus(authenticatedUserId, gigStatus)
                        .stream()
                        .map(gigMapper::toResponseDto)
                        .toList();
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status filter: {}", status);
                throw new RuntimeException("Invalid status: " + status);
            }
        } else {
            return gigRepository.findByProfileId(authenticatedUserId)
                    .stream()
                    .map(gigMapper::toResponseDto)
                    .toList();
        }
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

    public List<GigResponseDto> searchGigs(String category, List<String> tags, Long freelancerId) {
        log.info("Searching gigs with category: {}, tags: {}, freelancerId: {}", category, tags, freelancerId);
        
        List<Gig> gigs;
        
        if (freelancerId != null) {
            // If freelancer_id is specified, get gigs for that freelancer only
            // But only return ACTIVE gigs to respect privacy (draft/paused gigs are private)
            gigs = gigRepository.findByProfileIdAndStatus(freelancerId, Gig.Status.ACTIVE);
            log.info("Found {} active gigs for freelancer: {}", gigs.size(), freelancerId);
        } else if (category != null && !category.trim().isEmpty()) {
            gigs = gigRepository.findActiveGigsByCategory(category);
        } else if (tags != null && !tags.isEmpty()) {
            gigs = gigRepository.findActiveGigsByTags(tags);
        } else {
            gigs = gigRepository.findByStatus(Gig.Status.ACTIVE);
        }
        
        return gigs.stream()
                .map(gigMapper::toResponseDto)
                .toList();
    }
    
    @Transactional
    public Optional<GigResponseDto> updateGig(Long gigId, GigUpdateDto updateDto) {
        log.info("Updating gig with ID: {}", gigId);
        
        Optional<Gig> gigOpt = gigRepository.findById(gigId);
        
        if (gigOpt.isEmpty()) {
            log.warn("Gig not found with ID: {}", gigId);
            return Optional.empty();
        }
        
        Gig gig = gigOpt.get();
        gigMapper.updateEntityFromDto(updateDto, gig);
        
        Gig updatedGig = gigRepository.save(gig);
        log.info("Successfully updated gig with ID: {}", gigId);
        
        return Optional.of(gigMapper.toResponseDto(updatedGig));
    }
    
    @Transactional
    public boolean deleteGig(Long gigId) {
        log.info("Deleting gig with ID: {}", gigId);
        
        if (!gigRepository.existsById(gigId)) {
            log.warn("Gig not found with ID: {}", gigId);
            return false;
        }
        
        gigRepository.deleteById(gigId);
        log.info("Successfully deleted gig with ID: {}", gigId);
        return true;
    }
}
