package com.thefreelancer.microservices.gig.service;

import com.thefreelancer.microservices.gig.client.AuthServiceClient;
import com.thefreelancer.microservices.gig.dto.GigCreateDto;
import com.thefreelancer.microservices.gig.dto.GigResponseDto;
import com.thefreelancer.microservices.gig.dto.GigUpdateDto;
import com.thefreelancer.microservices.gig.dto.GigWithFreelancerResponseDto;
import com.thefreelancer.microservices.gig.dto.UserResponseDto;
import com.thefreelancer.microservices.gig.mapper.GigMapper;
import com.thefreelancer.microservices.gig.model.Gig;
import com.thefreelancer.microservices.gig.repository.GigRepository;
import com.thefreelancer.microservices.gig.repository.ProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GigService {
    
    private final GigRepository gigRepository;
    private final ProfileRepository profileRepository;
    private final GigMapper gigMapper;
    private final AuthServiceClient authServiceClient;
    private final GigEmbeddingService gigEmbeddingService;
    private final ProfileEmbeddingService profileEmbeddingService;
    
    @Transactional
    public GigResponseDto createGig(Long userId, GigCreateDto createDto) {
        log.info("Creating gig for userId: {}", userId);
        
        // Check if profile exists
        if (profileRepository.findByUserId(userId).isEmpty()) {
            throw new RuntimeException("Profile not found for userId: " + userId);
        }
        
        Gig gig = gigMapper.toEntity(createDto);
        gig.setProfileId(userId); // Using userId as profileId since profile.userId is the primary key
        gig.setStatus(Gig.Status.ACTIVE); // New gigs start as draft
        
        Gig savedGig = gigRepository.save(gig);
        log.info("Successfully created gig with ID: {} for userId: {}", savedGig.getId(), userId);
        
        // Store gig embedding for semantic search
        try {
            gigEmbeddingService.storeGigEmbedding(savedGig);
        } catch (Exception e) {
            log.warn("Failed to store gig embedding for gig {}: {}", savedGig.getId(), e.getMessage());
        }
        
        // Update profile embedding to include new gig data
        try {
            profileEmbeddingService.updateProfileEmbeddingOnGigChange(userId);
        } catch (Exception e) {
            log.warn("Failed to update profile embedding for user {}: {}", userId, e.getMessage());
        }
        
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
        
        // Update gig embedding for semantic search
        try {
            gigEmbeddingService.updateGigEmbedding(updatedGig);
        } catch (Exception e) {
            log.warn("Failed to update gig embedding for gig {}: {}", updatedGig.getId(), e.getMessage());
        }
        
        // Update profile embedding to reflect updated gig data
        try {
            profileEmbeddingService.updateProfileEmbeddingOnGigChange(updatedGig.getProfileId());
        } catch (Exception e) {
            log.warn("Failed to update profile embedding for user {}: {}", updatedGig.getProfileId(), e.getMessage());
        }
        
        return Optional.of(gigMapper.toResponseDto(updatedGig));
    }
    
    @Transactional
    public boolean deleteGig(Long gigId) {
        log.info("Deleting gig with ID: {}", gigId);
        
        // Get gig details before deletion for profile embedding update
        Optional<Gig> gigOpt = gigRepository.findById(gigId);
        if (gigOpt.isEmpty()) {
            log.warn("Gig not found with ID: {}", gigId);
            return false;
        }
        
        Long profileId = gigOpt.get().getProfileId();
        
        // Delete gig embedding first
        try {
            gigEmbeddingService.deleteGigEmbedding(gigId);
        } catch (Exception e) {
            log.warn("Failed to delete gig embedding for gig {}: {}", gigId, e.getMessage());
        }
        
        gigRepository.deleteById(gigId);
        log.info("Successfully deleted gig with ID: {}", gigId);
        
        // Update profile embedding to remove deleted gig data
        try {
            profileEmbeddingService.updateProfileEmbeddingOnGigChange(profileId);
        } catch (Exception e) {
            log.warn("Failed to update profile embedding for user {}: {}", profileId, e.getMessage());
        }
        
        return true;
    }
    
    @Transactional
    public void updateGigRatings(Long gigId, Double averageRating, Long reviewCount) {
        log.info("Updating ratings for gig: {} - avgRating: {}, reviewCount: {}", gigId, averageRating, reviewCount);
        
        Optional<Gig> gigOptional = gigRepository.findById(gigId);
        if (gigOptional.isPresent()) {
            Gig gig = gigOptional.get();
            gig.setReviewAvg(averageRating != null ? java.math.BigDecimal.valueOf(averageRating) : java.math.BigDecimal.ZERO);
            gig.setReviewsCount(reviewCount != null ? reviewCount.intValue() : 0);
            
            gigRepository.save(gig);
            log.info("Successfully updated ratings for gig: {}", gigId);
        } else {
            log.warn("Gig not found for rating update: {}", gigId);
        }
    }

    /**
     * Search gigs with freelancer information from auth-service
     */
    public List<GigWithFreelancerResponseDto> searchGigsWithFreelancerInfo(String category, List<String> tags, Long freelancerId) {
        log.info("Searching gigs with freelancer info - category: {}, tags: {}, freelancerId: {}", category, tags, freelancerId);
        
        // Get gigs using existing search logic
        List<Gig> gigs = searchGigsInternal(category, tags, freelancerId);
        
        if (gigs.isEmpty()) {
            log.info("No gigs found for the given criteria");
            return List.of();
        }
        
        // Extract unique profile IDs (which correspond to user IDs in auth-service)
        List<Long> freelancerIds = gigs.stream()
                .map(Gig::getProfileId)
                .distinct()
                .toList();
        
        log.info("Fetching freelancer info for {} unique profiles: {}", freelancerIds.size(), freelancerIds);
        
        // Batch fetch freelancer information from auth-service
        Map<Long, UserResponseDto> freelancerMap = authServiceClient.getUsersByIds(freelancerIds);
        
        // Build enriched response DTOs
        return gigs.stream()
                .map(gig -> buildGigWithFreelancerResponse(gig, freelancerMap.get(gig.getProfileId())))
                .toList();
    }

    public Optional<GigWithFreelancerResponseDto> getGigWithFreelancerInfo(Long gigId) {
        log.info("Fetching gig with ID: {} along with freelancer info", gigId);
        
        Optional<Gig> gigOpt = gigRepository.findById(gigId);
        if (gigOpt.isEmpty()) {
            log.warn("Gig not found with ID: {}", gigId);
            return Optional.empty();
        }
        
        Gig gig = gigOpt.get();
        
        // Fetch freelancer info from auth-service
        UserResponseDto freelancer = authServiceClient.getUserById(gig.getProfileId());
        
        GigWithFreelancerResponseDto responseDto = buildGigWithFreelancerResponse(gig, freelancer);
        
        return Optional.of(responseDto);
    }

    /**
     * Internal method to get gigs without DTO mapping (for reuse)
     */
    private List<Gig> searchGigsInternal(String category, List<String> tags, Long freelancerId) {
        if (freelancerId != null) {
            // If freelancer_id is specified, get gigs for that freelancer only
            // But only return ACTIVE gigs to respect privacy
            return gigRepository.findByProfileIdAndStatus(freelancerId, Gig.Status.ACTIVE);
        } else if (category != null && !category.trim().isEmpty()) {
            return gigRepository.findActiveGigsByCategory(category);
        } else if (tags != null && !tags.isEmpty()) {
            return gigRepository.findActiveGigsByTags(tags);
        } else {
            return gigRepository.findByStatus(Gig.Status.ACTIVE);
        }
    }

    /**
     * Build GigWithFreelancerResponseDto from gig and freelancer info
     */
    private GigWithFreelancerResponseDto buildGigWithFreelancerResponse(Gig gig, UserResponseDto freelancer) {
        return GigWithFreelancerResponseDto.builder()
                .id(gig.getId())
                .profileId(gig.getProfileId())
                .title(gig.getTitle())
                .description(gig.getDescription())
                .status(gig.getStatus().toString())
                .category(gig.getCategory())
                .tags(gig.getTags() != null ? List.of(gig.getTags()) : List.of())
                .reviewAvg(gig.getReviewAvg() != null ? gig.getReviewAvg().doubleValue() : 0.0)
                .reviewsCount(gig.getReviewsCount())
                .createdAt(gig.getCreatedAt())
                .updatedAt(gig.getUpdatedAt())
                .freelancerInfo(freelancer != null ? 
                    GigWithFreelancerResponseDto.FreelancerInfo.builder()
                        .id(freelancer.getId())
                        .email(freelancer.getEmail())
                        .name(freelancer.getName())
                        .handle(freelancer.getHandle())
                        .role(freelancer.getRole())
                        .country(freelancer.getCountry())
                        .timezone(freelancer.getTimezone())
                        .isActive(freelancer.getIsActive())
                        .build()
                    : null)
                .build();
    }
}
