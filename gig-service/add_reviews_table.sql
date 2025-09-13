-- Migration script to add reviews table to existing gig_db
-- Run this script in the gig_db database

-- Create reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id                      BIGSERIAL PRIMARY KEY,
    gig_id                  BIGINT NOT NULL REFERENCES gigs(id) ON DELETE CASCADE,
    freelancer_id           BIGINT NOT NULL, -- References User.id from auth-service
    reviewer_id             BIGINT NOT NULL, -- References User.id from auth-service
    job_id                  VARCHAR(255), -- References job from job-proposal-service (optional)
    contract_id             VARCHAR(255), -- References contract from job-proposal-service (optional)
    
    -- Rating categories (1-5 scale)
    overall_rating          INTEGER NOT NULL CHECK (overall_rating >= 1 AND overall_rating <= 5),
    quality_rating          INTEGER NOT NULL CHECK (quality_rating >= 1 AND quality_rating <= 5),
    communication_rating    INTEGER NOT NULL CHECK (communication_rating >= 1 AND communication_rating <= 5),
    timeliness_rating       INTEGER NOT NULL CHECK (timeliness_rating >= 1 AND timeliness_rating <= 5),
    professionalism_rating  INTEGER NOT NULL CHECK (professionalism_rating >= 1 AND professionalism_rating <= 5),
    
    -- Review content
    title                   VARCHAR(100),
    comment                 TEXT NOT NULL,
    
    -- Review metadata
    review_type             VARCHAR(50) DEFAULT 'GIG_REVIEW' CHECK (review_type IN ('GIG_REVIEW', 'PROFILE_REVIEW', 'JOB_REVIEW')),
    status                  VARCHAR(50) DEFAULT 'PUBLISHED' CHECK (status IN ('DRAFT', 'PUBLISHED', 'HIDDEN', 'DELETED', 'PENDING_REVIEW')),
    is_anonymous            BOOLEAN DEFAULT FALSE,
    would_recommend         BOOLEAN DEFAULT TRUE,
    
    -- Moderation and interaction
    is_flagged              BOOLEAN DEFAULT FALSE,
    flag_reason             TEXT,
    helpful_votes           INTEGER DEFAULT 0,
    freelancer_response     TEXT,
    freelancer_response_at  TIMESTAMP WITH TIME ZONE,
    
    created_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    UNIQUE(gig_id, reviewer_id) -- Prevent duplicate reviews per gig/reviewer
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_reviews_gig_id ON reviews(gig_id, created_at);
CREATE INDEX IF NOT EXISTS idx_reviews_freelancer_id ON reviews(freelancer_id, created_at);
CREATE INDEX IF NOT EXISTS idx_reviews_reviewer_id ON reviews(reviewer_id, created_at);
CREATE INDEX IF NOT EXISTS idx_reviews_status ON reviews(status, created_at);
CREATE INDEX IF NOT EXISTS idx_reviews_gig_rating ON reviews(gig_id, overall_rating);
CREATE INDEX IF NOT EXISTS idx_reviews_freelancer_rating ON reviews(freelancer_id, overall_rating);

-- Insert some sample review data for testing
INSERT INTO reviews (
    gig_id, freelancer_id, reviewer_id, overall_rating, 
    quality_rating, communication_rating, timeliness_rating, professionalism_rating,
    comment, would_recommend
) VALUES 
    (1, 1, 3, 5, 5, 4, 5, 5, 'Excellent work! The developer delivered exactly what was requested and was very professional throughout the project.', true),
    (1, 1, 4, 4, 4, 5, 4, 4, 'Good communication and solid technical skills. Would definitely work with again.', true),
    (2, 2, 5, 5, 5, 5, 5, 5, 'Amazing designer! Created beautiful and intuitive designs that exceeded our expectations.', true)
ON CONFLICT (gig_id, reviewer_id) DO NOTHING;
