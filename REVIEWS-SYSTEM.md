# Review & Rating System Documentation

## Overview

The Review & Rating System is a comprehensive feature that allows clients to rate and review freelancers based on their work. This system is critical for building trust and reputation in the freelancer marketplace.

## Features

### Core Rating System
- **5-Star Rating System**: Overall rating from 1-5 stars
- **Category-Specific Ratings**:
  - Quality of Work
  - Communication
  - Timeliness
  - Professionalism
- **Recommendation System**: Boolean flag for "Would Recommend"
- **Written Reviews**: Text comments with validation

### Advanced Features
- **Review Moderation**: Support for flagging inappropriate content
- **Freelancer Responses**: Ability to respond to reviews
- **Helpful Votes**: Community voting on review helpfulness
- **Anonymous Reviews**: Optional anonymous reviewing
- **Review Types**: Support for different review contexts (GIG_REVIEW, etc.)

### Data Analytics
- **Rating Aggregation**: Automatic calculation of average ratings
- **Review Statistics**: Comprehensive metrics and summaries
- **Trend Analysis**: Recent ratings and performance trends
- **Distribution Analysis**: Rating distribution across different categories

## API Endpoints

### Review Management
```http
POST   /api/reviews                    # Create a new review
GET    /api/reviews/{id}              # Get review by ID
PUT    /api/reviews/{id}              # Update existing review
DELETE /api/reviews/{id}              # Delete review (soft delete)
```

### Review Queries
```http
GET    /api/reviews/gig/{gigId}             # Get reviews for a gig
GET    /api/reviews/freelancer/{userId}     # Get reviews for a freelancer
GET    /api/reviews/reviewer/{userId}       # Get reviews by a reviewer
```

### Statistics & Analytics
```http
GET    /api/reviews/gig/{gigId}/summary         # Gig rating summary
GET    /api/reviews/freelancer/{userId}/summary # Freelancer rating summary
GET    /api/reviews/gig/{gigId}/recent          # Recent reviews for gig
```

### Moderation
```http
POST   /api/reviews/{id}/flag         # Flag review for moderation
POST   /api/reviews/{id}/helpful      # Mark review as helpful
POST   /api/reviews/{id}/response     # Add freelancer response
```

## Data Models

### Review Entity
```java
@Entity
@Table(name = "reviews")
public class Review {
    private Long id;
    private Long gigId;
    private Long freelancerId;
    private Long reviewerId;
    private String jobId;
    private String contractId;
    
    // Rating categories (1-5)
    private Integer overallRating;
    private Integer qualityRating;
    private Integer communicationRating;
    private Integer timelinessRating;
    private Integer professionalismRating;
    
    // Review content
    private String title;
    private String comment;
    private Boolean wouldRecommend;
    
    // Metadata
    private ReviewStatus status;
    private Boolean isAnonymous;
    private String reviewType;
    
    // Moderation
    private Boolean isFlagged;
    private String flagReason;
    private Integer helpfulVotes;
    private String freelancerResponse;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### DTOs

#### ReviewCreateDto
```java
public class ReviewCreateDto {
    @NotNull private Long gigId;
    @NotNull private Long freelancerId;
    private String jobId;
    private String contractId;
    
    @NotNull @Min(1) @Max(5) private Integer overallRating;
    @NotNull @Min(1) @Max(5) private Integer qualityRating;
    @NotNull @Min(1) @Max(5) private Integer communicationRating;
    @NotNull @Min(1) @Max(5) private Integer timelinessRating;
    @NotNull @Min(1) @Max(5) private Integer professionalismRating;
    
    @Size(max = 100) private String title;
    @NotBlank @Size(max = 2000) private String comment;
    private String reviewType = "GIG_REVIEW";
    private Boolean isAnonymous = false;
    private Boolean wouldRecommend = true;
}
```

#### ReviewSummaryDto
```java
public class ReviewSummaryDto {
    private Long totalReviews;
    private Double averageRating;
    private Double recommendationPercentage;
    
    // Category averages
    private Double averageQualityRating;
    private Double averageCommunicationRating;
    private Double averageTimelinessRating;
    private Double averageProfessionalismRating;
    
    // Recent metrics
    private Long recentReviews;
    private Double recentAverageRating;
    
    // Distribution
    private Map<Integer, Long> ratingDistribution;
}
```

## Business Logic

### Review Creation Process
1. **Validation**: Verify gig exists and user permissions
2. **Duplicate Check**: Ensure reviewer hasn't already reviewed this gig
3. **Rating Calculation**: Calculate overall rating from categories
4. **Auto-Publication**: Most reviews are automatically published
5. **Rating Updates**: Update both gig and profile ratings

### Rating Aggregation
```java
// Overall rating calculation (equal weight to all categories)
overallRating = (qualityRating + communicationRating + 
                timelinessRating + professionalismRating) / 4.0

// Gig rating update (average of all reviews)
gigRating = SUM(overallRating) / COUNT(reviews)

// Profile rating update (average across all gigs)
profileRating = AVG(gigRatings weighted by review count)
```

### Automatic Updates
- **Gig Ratings**: Updated immediately when reviews are created/updated
- **Profile Ratings**: Updated via ProfileService integration
- **Review Counts**: Maintained on both Gig and Profile entities

## Database Schema

### Indexes for Performance
```sql
-- Review lookups
CREATE INDEX idx_gig_reviews ON reviews(gig_id, created_at);
CREATE INDEX idx_freelancer_reviews ON reviews(freelancer_id, created_at);
CREATE INDEX idx_reviewer_reviews ON reviews(reviewer_id, created_at);

-- Rating queries
CREATE INDEX idx_gig_rating ON reviews(gig_id, overall_rating);
CREATE INDEX idx_freelancer_rating ON reviews(freelancer_id, overall_rating);

-- Status filtering
CREATE INDEX idx_review_status ON reviews(status, created_at);
```

### Constraints
```sql
-- Ensure rating values are within valid range
ALTER TABLE reviews ADD CONSTRAINT check_overall_rating 
    CHECK (overall_rating >= 1 AND overall_rating <= 5);

-- Prevent duplicate reviews per gig/reviewer
ALTER TABLE reviews ADD CONSTRAINT unique_gig_reviewer 
    UNIQUE (gig_id, reviewer_id);
```

## Integration Points

### With Gig Service
- Updates `gig.review_avg` and `gig.reviews_count`
- Validates gig existence before review creation
- Provides gig-specific review queries

### With Profile Service  
- Updates `profile.review_avg` and `profile.reviews_count`
- Aggregates ratings across all freelancer gigs
- Maintains freelancer reputation scores

### With Job/Proposal Service
- Links reviews to specific jobs and contracts
- Enables job completion → review workflow
- Provides context for review authenticity

### With Auth Service
- Validates user permissions and identity
- Supports anonymous review options
- Handles reviewer authentication

## Security & Permissions

### Review Creation
- Only clients who hired the freelancer can review
- Verification through job/contract linkage
- Rate limiting to prevent spam

### Review Management
- Reviewers can edit their own reviews (time-limited)
- Freelancers can respond to reviews about them
- Admins can moderate flagged content

### Data Privacy
- Anonymous review option preserves reviewer privacy
- Soft deletion maintains data integrity
- Audit trail for all review changes

## Performance Considerations

### Caching Strategy
- Frequently accessed rating summaries cached
- Profile and gig ratings cached after updates
- Review lists paginated with reasonable limits

### Query Optimization
- Database indexes on common query patterns
- Efficient aggregation queries for statistics
- Batch updates for rating recalculations

### Scalability
- Asynchronous rating updates where possible
- Database partitioning for high-volume deployments
- CDN caching for public review displays

## Testing Strategy

### Unit Tests
- DTO validation and mapping
- Rating calculation logic
- Service layer business rules
- Repository query accuracy

### Integration Tests
- End-to-end review creation flow
- Rating aggregation accuracy
- Cross-service integration points
- Database constraint verification

### Performance Tests
- Rating calculation performance
- Large dataset query performance
- Concurrent review creation handling

## Monitoring & Analytics

### Key Metrics
- Review creation rate
- Average rating trends
- Response time for rating updates
- Moderation queue length

### Business Intelligence
- Rating distribution analysis
- Freelancer performance trends
- Review sentiment analysis
- Platform trust metrics

## Future Enhancements

### Planned Features
- Machine learning for review sentiment analysis
- Advanced fraud detection for fake reviews
- Review template suggestions
- Multi-language review support

### API Versioning
- Current version: v1
- Backward compatibility maintained
- Deprecation notices for breaking changes

## Deployment & Configuration

### Environment Variables
```env
REVIEW_MODERATION_ENABLED=true
REVIEW_EDIT_WINDOW_HOURS=24
MAX_REVIEWS_PER_USER_PER_DAY=10
AUTO_PUBLISH_REVIEWS=true
```

### Database Migrations
- All schema changes versioned
- Backward compatible migrations
- Data integrity checks included

## Support & Troubleshooting

### Common Issues
- **Rating not updating**: Check service dependencies and async processing
- **Review not appearing**: Verify publication status and moderation settings
- **Permission denied**: Confirm user relationship to gig/job

### Debug Endpoints (Dev/Staging only)
```http
GET /api/reviews/debug/gig/{gigId}/recalculate
GET /api/reviews/debug/freelancer/{userId}/recalculate
```

This comprehensive review system provides the foundation for trust and quality assurance in the freelancer marketplace, with robust features for both basic review functionality and advanced analytics.
