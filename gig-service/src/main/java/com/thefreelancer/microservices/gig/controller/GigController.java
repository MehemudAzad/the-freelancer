package com.thefreelancer.microservices.gig.controller;
import org.springframework.web.multipart.MultipartFile;
import com.thefreelancer.microservices.gig.service.GigMediaService;

import com.thefreelancer.microservices.gig.dto.GigCreateDto;
import com.thefreelancer.microservices.gig.dto.GigResponseDto;
import com.thefreelancer.microservices.gig.dto.GigUpdateDto;
import com.thefreelancer.microservices.gig.dto.GigWithFreelancerResponseDto;
import com.thefreelancer.microservices.gig.dto.GigSearchRequestDto;
import com.thefreelancer.microservices.gig.dto.GigSearchResponseDto;
import com.thefreelancer.microservices.gig.service.GigService;
import com.thefreelancer.microservices.gig.service.GigSemanticSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/gigs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gigs", description = "Gig management operations")
public class GigController {
    
    
    private final GigService gigService;
    private final GigMediaService gigMediaService;
    private final GigSemanticSearchService gigSemanticSearchService;
    
    //! public
    @Operation(summary = "Get gig by ID", description = "Fetch a specific gig by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gig found"),
        @ApiResponse(responseCode = "404", description = "Gig not found")
    })
    @GetMapping("/{gigId}")
    public ResponseEntity<GigResponseDto> getGig(
            @Parameter(description = "ID of the gig to retrieve") @PathVariable Long gigId) {
        log.info("GET /api/gigs/{} - Fetching gig", gigId);
        
        Optional<GigResponseDto> gig = gigService.getGigById(gigId);
        
        if (gig.isPresent()) {
            log.info("Gig found with ID: {}", gigId);
            return ResponseEntity.ok(gig.get());
        } else {
            log.warn("Gig not found with ID: {}", gigId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/with-freelancer/{gigId}")
    public ResponseEntity<GigWithFreelancerResponseDto> getGigWithFreelancer(
            @Parameter(description = "ID of the gig to retrieve with freelancer info") @PathVariable Long gigId) {
        log.info("GET /api/gigs/with-freelancer/{} - Fetching gig with freelancer info", gigId);
        Optional<GigWithFreelancerResponseDto> gig = gigService.getGigWithFreelancerInfo(gigId);
        if (gig.isPresent()) {
            log.info("Gig with freelancer info found with ID: {}", gigId);
            return ResponseEntity.ok(gig.get());
        } else {
            log.warn("Gig with freelancer info not found with ID: {}", gigId);
            return ResponseEntity.notFound().build();
        }
    }

    //! public
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GigResponseDto>> getUserGigs(@PathVariable Long userId) {
        log.info("GET /api/gigs/user/{} - Fetching gigs for user", userId);
        
        List<GigResponseDto> gigs = gigService.getGigsByUserId(userId);
        return ResponseEntity.ok(gigs);
    }
    //! public
    @GetMapping("/search")
    public ResponseEntity<List<GigResponseDto>> searchGigs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) Long freelancerId) {
        
        log.info("GET /api/gigs/search - Searching gigs with category: {}, tags: {}, freelancerId: {}", category, tags, freelancerId);
        
        List<GigResponseDto> gigs = gigService.searchGigs(category, tags, freelancerId);
        return ResponseEntity.ok(gigs);
    }

    //! public - Enhanced search with freelancer information
    @GetMapping("/search/with-freelancer")
    @Operation(summary = "Search gigs with freelancer information", description = "Search for gigs and include freelancer details from auth-service")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gigs found with freelancer information"),
        @ApiResponse(responseCode = "404", description = "No gigs found")
    })
    public ResponseEntity<List<GigWithFreelancerResponseDto>> searchGigsWithFreelancer(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) Long freelancerId) {
        
        log.info("GET /api/gigs/search/with-freelancer - Searching gigs with freelancer info - category: {}, tags: {}, freelancerId: {}", category, tags, freelancerId);
        
        List<GigWithFreelancerResponseDto> gigs = gigService.searchGigsWithFreelancerInfo(category, tags, freelancerId);
        return ResponseEntity.ok(gigs);
    }
    
    // =====================================================
    // SEMANTIC SEARCH ENDPOINTS
    // =====================================================
    
    @PostMapping("/search/semantic")
    @Operation(summary = "Semantic search for gigs", description = "Search for gigs using natural language queries with AI-powered semantic search")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search request")
    })
    public ResponseEntity<GigSearchResponseDto> semanticSearchGigs(
            @Valid @RequestBody GigSearchRequestDto request) {
        
        log.info("POST /api/gigs/search/semantic - Performing semantic search with query: '{}'", request.getQuery());
        
        try {
            GigSearchResponseDto response = gigSemanticSearchService.searchGigs(request);
            log.info("Semantic search completed - found {} results", response.getTotalResults());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error performing semantic search: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GigSearchResponseDto.builder()
                    .query(request.getQuery())
                    .results(List.of())
                    .totalResults(0)
                    .searchType("semantic")
                    .error("Search service temporarily unavailable")
                    .build());
        }
    }
    
    @GetMapping("/search/semantic")
    @Operation(summary = "Semantic search for gigs (GET)", description = "Search for gigs using natural language queries via GET request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public ResponseEntity<GigSearchResponseDto> semanticSearchGigsGet(
            @RequestParam String q,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minRating) {
        
        log.info("GET /api/gigs/search/semantic - Performing semantic search with query: '{}'", q);
        
        GigSearchRequestDto request = GigSearchRequestDto.builder()
            .query(q)
            .limit(limit)
            .offset(offset)
            .category(category)
            .minRating(minRating)
            .build();
        
        return semanticSearchGigs(request);
    }
    
    @GetMapping("/search/semantic/category/{category}")
    @Operation(summary = "Semantic search by category", description = "Search for gigs within a specific category using semantic search")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid category or search parameters")
    })
    public ResponseEntity<GigSearchResponseDto> semanticSearchByCategory(
            @PathVariable String category,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("GET /api/gigs/search/semantic/category/{} - Searching in category with additional query: '{}'", category, q);
        
        try {
            GigSearchResponseDto response = gigSemanticSearchService.searchGigsByCategory(category, q, limit);
            log.info("Category semantic search completed - found {} results", response.getTotalResults());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error performing category semantic search: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GigSearchResponseDto.builder()
                    .query(category + (q != null ? " " + q : ""))
                    .results(List.of())
                    .totalResults(0)
                    .searchType("category_semantic")
                    .error("Search service temporarily unavailable")
                    .build());
        }
    }
    
    @PutMapping("/{gigId}")
    public ResponseEntity<GigResponseDto> updateGig(
            @PathVariable Long gigId,
            @Valid @RequestBody GigUpdateDto gigUpdateDto) {
        
        log.info("PUT /api/gigs/{} - Updating gig", gigId);
        
        try {
            Optional<GigResponseDto> updatedGig = gigService.updateGig(gigId, gigUpdateDto);
            
            if (updatedGig.isPresent()) {
                log.info("Gig successfully updated with ID: {}", gigId);
                return ResponseEntity.ok(updatedGig.get());
            } else {
                log.warn("Gig not found with ID: {}", gigId);
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            log.warn("Failed to update gig {}: {}", gigId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    //? not needed might delete later
    @DeleteMapping("/{gigId}")
    public ResponseEntity<Void> deleteGig(@PathVariable Long gigId) {
        log.info("DELETE /api/gigs/{} - Deleting gig", gigId);
        
        boolean deleted = gigService.deleteGig(gigId);
        
        if (deleted) {
            log.info("Gig successfully deleted with ID: {}", gigId);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Gig not found with ID: {}", gigId);
            return ResponseEntity.notFound().build();
        }
    }
    //! Auth needed
    @GetMapping("/my-gigs")
    public ResponseEntity<List<GigResponseDto>> getMyGigs(
            @RequestParam(required = false) String status,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("GET /api/gigs/my-gigs - Fetching authenticated user's gigs with status: {}", status);
        
        if (userIdHeader == null) {
            log.warn("Authentication required for my-gigs endpoint");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            List<GigResponseDto> myGigs = gigService.getMyGigs(authenticatedUserId, status);
            log.info("Found {} gigs for authenticated user: {}", myGigs.size(), authenticatedUserId);
            return ResponseEntity.ok(myGigs);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error fetching my gigs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ========== SECURE ENDPOINTS FOR GIG MANAGEMENT ==========
    //! auth protected
    @PostMapping("/my-gigs")
    public ResponseEntity<GigResponseDto> createGig(
            @Valid @RequestBody GigCreateDto gigCreateDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("POST /api/gigs - Creating gig");
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for creating gigs");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check authorization - only freelancers can create gigs
        if (!"FREELANCER".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only freelancers can create gigs. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Long userId = Long.parseLong(userIdHeader);
            log.info("Creating gig for authenticated freelancer userId: {}", userId);
            
            GigResponseDto gig = gigService.createGig(userId, gigCreateDto);
            log.info("Gig successfully created with ID: {} for userId: {}", gig.getId(), userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(gig);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.warn("Failed to create gig for userId {}: {}", userIdHeader, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/create-with-media", consumes = "multipart/form-data")
    @Operation(summary = "Create gig with media", description = "Create a gig and upload a media file in one request")
    public ResponseEntity<GigResponseDto> createGigWithMedia(
            @RequestPart(name = "gig") GigCreateDto gigCreateDto,
            @RequestPart(name = "file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        log.info("POST /api/gigs/create-with-media - Creating gig and uploading media");
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for creating gigs");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!"FREELANCER".equalsIgnoreCase(userRole)) {
            log.warn("Access denied: Only freelancers can create gigs. User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            Long userId = Long.parseLong(userIdHeader);
            log.info("Creating gig for authenticated freelancer userId: {}", userId);
            GigResponseDto gig = gigService.createGig(userId, gigCreateDto);
            log.info("Gig successfully created with ID: {} for userId: {}", gig.getId(), userId);
            // Upload media and link to gig
            gigMediaService.uploadMedia(gig.getId(), file);
            log.info("Media successfully uploaded for gigId: {}", gig.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(gig);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException | java.io.IOException e) {
            log.warn("Failed to create gig with media: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    
    //! Auth needed
    @PutMapping("/my-gigs/{gigId}")
    public ResponseEntity<GigResponseDto> updateMyGig(
            @PathVariable Long gigId,
            @Valid @RequestBody GigUpdateDto gigUpdateDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("PUT /api/gigs/my-gigs/{} - Updating authenticated user's gig", gigId);
        
        // Check authentication
        if (userIdHeader == null) {
            log.warn("Authentication required for updating gigs");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = Long.parseLong(userIdHeader);
            log.info("Updating gig {} for authenticated user: {}", gigId, userId);
            
            // The service should validate that the user owns this gig
            // For now, we'll use the existing method - TODO: Add ownership validation in service
            Optional<GigResponseDto> updatedGig = gigService.updateGig(gigId, gigUpdateDto);
            
            if (updatedGig.isPresent()) {
                log.info("Gig successfully updated with ID: {} for userId: {}", gigId, userId);
                return ResponseEntity.ok(updatedGig.get());
            } else {
                log.warn("Gig not found or access denied for gigId: {} and userId: {}", gigId, userId);
                return ResponseEntity.notFound().build();
            }
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.warn("Failed to update gig {}: {}", gigId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/my-gigs/{gigId}")
    public ResponseEntity<Void> deleteMyGig(
            @PathVariable Long gigId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("DELETE /api/gigs/my-gigs/{} - Deleting authenticated user's gig", gigId);
        
        // Check authentication
        if (userIdHeader == null) {
            log.warn("Authentication required for deleting gigs");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = Long.parseLong(userIdHeader);
            log.info("Deleting gig {} for authenticated user: {}", gigId, userId);
            
            // The service should validate that the user owns this gig
            // For now, we'll use the existing method - TODO: Add ownership validation in service
            boolean deleted = gigService.deleteGig(gigId);
            
            if (deleted) {
                log.info("Gig successfully deleted with ID: {} for userId: {}", gigId, userId);
                return ResponseEntity.noContent().build();
            } else {
                log.warn("Gig not found or access denied for gigId: {} and userId: {}", gigId, userId);
                return ResponseEntity.notFound().build();
            }
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.warn("Failed to delete gig {}: {}", gigId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
