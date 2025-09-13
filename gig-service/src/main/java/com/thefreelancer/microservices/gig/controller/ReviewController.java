package com.thefreelancer.microservices.gig.controller;

import com.thefreelancer.microservices.gig.dto.*;
import com.thefreelancer.microservices.gig.service.ReviewService;
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

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reviews", description = "Review and rating management operations")
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @Operation(summary = "Create review", description = "Create a new review for a gig")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Review created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid review data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Cannot review your own gig or duplicate review"),
        @ApiResponse(responseCode = "404", description = "Gig not found")
    })
    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(
            @Valid @RequestBody ReviewCreateDto createDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("POST /api/reviews - Creating review for gig: {}", createDto.getGigId());
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for creating reviews");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = Long.parseLong(userIdHeader);
            ReviewResponseDto response = reviewService.createReview(createDto, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for creating review: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating review: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Get gig reviews", description = "Get paginated reviews for a specific gig")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Gig not found")
    })
    @GetMapping("/gigs/{gigId}")
    public ResponseEntity<ReviewPageResponseDto> getGigReviews(
            @Parameter(description = "ID of the gig") @PathVariable Long gigId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by: newest, oldest, rating_high, rating_low, helpful") 
            @RequestParam(defaultValue = "newest") String sort,
            @Parameter(description = "Filter by rating (1-5)") @RequestParam(required = false) Integer rating,
            @Parameter(description = "Search in review comments") @RequestParam(required = false) String search) {
        
        log.info("GET /api/reviews/gigs/{} - Getting reviews", gigId);
        
        try {
            ReviewPageResponseDto response = reviewService.getGigReviews(gigId, page, size, sort, rating, search);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for getting gig reviews: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting gig reviews: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Get freelancer reviews", description = "Get paginated reviews for a specific freelancer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Freelancer not found")
    })
    @GetMapping("/freelancers/{freelancerId}")
    public ResponseEntity<ReviewPageResponseDto> getFreelancerReviews(
            @Parameter(description = "ID of the freelancer") @PathVariable Long freelancerId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by: newest, oldest, rating_high, rating_low, helpful") 
            @RequestParam(defaultValue = "newest") String sort,
            @Parameter(description = "Filter by rating (1-5)") @RequestParam(required = false) Integer rating,
            @Parameter(description = "Search in review comments") @RequestParam(required = false) String search) {
        
        log.info("GET /api/reviews/freelancers/{} - Getting reviews", freelancerId);
        
        try {
            ReviewPageResponseDto response = reviewService.getFreelancerReviews(freelancerId, page, size, sort, rating, search);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for getting freelancer reviews: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting freelancer reviews: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Get review by ID", description = "Get a specific review by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Review retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> getReviewById(
            @Parameter(description = "ID of the review") @PathVariable Long reviewId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        
        log.info("GET /api/reviews/{} - Getting review", reviewId);
        
        try {
            Long currentUserId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
            ReviewResponseDto response = reviewService.getReviewById(reviewId, currentUserId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Review not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting review: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Update review", description = "Update an existing review")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Review updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid update data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Cannot edit this review"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @Parameter(description = "ID of the review") @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateDto updateDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("PUT /api/reviews/{} - Updating review", reviewId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for updating reviews");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = Long.parseLong(userIdHeader);
            ReviewResponseDto response = reviewService.updateReview(reviewId, updateDto, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for updating review: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            log.error("Error updating review: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Delete review", description = "Delete a review")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Review deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Cannot delete this review"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "ID of the review") @PathVariable Long reviewId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("DELETE /api/reviews/{} - Deleting review", reviewId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for deleting reviews");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = Long.parseLong(userIdHeader);
            reviewService.deleteReview(reviewId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for deleting review: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            log.error("Error deleting review: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Add freelancer response", description = "Add a response to a review as a freelancer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Response added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid response data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Cannot respond to this review"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @PostMapping("/{reviewId}/responses")
    public ResponseEntity<ReviewResponseDto> addFreelancerResponse(
            @Parameter(description = "ID of the review") @PathVariable Long reviewId,
            @Valid @RequestBody ReviewResponseCreateDto responseDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("POST /api/reviews/{}/responses - Adding freelancer response", reviewId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for responding to reviews");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = Long.parseLong(userIdHeader);
            ReviewResponseDto response = reviewService.addFreelancerResponse(reviewId, responseDto, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for adding freelancer response: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            log.error("Error adding freelancer response: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Mark review helpful", description = "Mark a review as helpful or unhelpful")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Review marked successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @PostMapping("/{reviewId}/helpful")
    public ResponseEntity<Void> markReviewHelpful(
            @Parameter(description = "ID of the review") @PathVariable Long reviewId,
            @Parameter(description = "Whether the review is helpful") @RequestParam boolean helpful,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("POST /api/reviews/{}/helpful - Marking review as {}", reviewId, helpful ? "helpful" : "unhelpful");
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for marking reviews helpful");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = Long.parseLong(userIdHeader);
            reviewService.markReviewHelpful(reviewId, userId, helpful);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Review not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error marking review helpful: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Flag review", description = "Flag a review for moderation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Review flagged successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid flag data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @PostMapping("/{reviewId}/flag")
    public ResponseEntity<Void> flagReview(
            @Parameter(description = "ID of the review") @PathVariable Long reviewId,
            @Valid @RequestBody ReviewFlagDto flagDto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        log.info("POST /api/reviews/{}/flag - Flagging review", reviewId);
        
        // Check authentication
        if (userIdHeader == null || userRole == null) {
            log.warn("Authentication required for flagging reviews");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = Long.parseLong(userIdHeader);
            reviewService.flagReview(reviewId, flagDto, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Review not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error flagging review: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Get gig review summary", description = "Get review statistics and summary for a gig")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Review summary retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Gig not found")
    })
    @GetMapping("/gigs/{gigId}/summary")
    public ResponseEntity<ReviewSummaryDto> getGigReviewSummary(
            @Parameter(description = "ID of the gig") @PathVariable Long gigId) {
        
        log.info("GET /api/reviews/gigs/{}/summary - Getting review summary", gigId);
        
        try {
            ReviewSummaryDto response = reviewService.getGigReviewSummary(gigId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting gig review summary: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Get freelancer review summary", description = "Get review statistics and summary for a freelancer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Review summary retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Freelancer not found")
    })
    @GetMapping("/freelancers/{freelancerId}/summary")
    public ResponseEntity<ReviewSummaryDto> getFreelancerReviewSummary(
            @Parameter(description = "ID of the freelancer") @PathVariable Long freelancerId) {
        
        log.info("GET /api/reviews/freelancers/{}/summary - Getting review summary", freelancerId);
        
        try {
            ReviewSummaryDto response = reviewService.getFreelancerReviewSummary(freelancerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting freelancer review summary: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
