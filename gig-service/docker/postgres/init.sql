-- PostgreSQL initialization script for gig-service
-- The database 'gig_db' is already created by the POSTGRES_DB environment variable
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create profiles table
CREATE TABLE IF NOT EXISTS profiles (
    user_id                 BIGINT PRIMARY KEY, -- References User.id from auth-service
    headline                VARCHAR(255),
    bio                     TEXT,
    hourly_rate_cents       BIGINT,
    currency                VARCHAR(10),
    availability            VARCHAR(50) DEFAULT 'OCCASIONAL' CHECK (availability IN ('FULL_TIME', 'PART_TIME', 'OCCASIONAL', 'UNAVAILABLE')),
    languages               TEXT[],
    skills                  TEXT[],
    location_text           VARCHAR(255),
    github_username         VARCHAR(255),
    gitlab_username         VARCHAR(255),
    website_url             VARCHAR(500),
    linkedin_url            VARCHAR(500),
    delivery_score          NUMERIC(5,2) DEFAULT 0.0,
    review_avg              NUMERIC(3,2) DEFAULT 0.0,
    reviews_count           INTEGER DEFAULT 0,
    created_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create gigs table
CREATE TABLE IF NOT EXISTS gigs (
    id                      BIGSERIAL PRIMARY KEY,
    profile_id              BIGINT NOT NULL REFERENCES profiles(user_id) ON DELETE CASCADE,
    title                   VARCHAR(255) NOT NULL,
    description             TEXT,
    status                  VARCHAR(50) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'ACTIVE', 'PAUSED', 'ARCHIVED')),
    category                VARCHAR(100),
    tags                    TEXT[],
    review_avg              NUMERIC(3,2) DEFAULT 0.0,
    reviews_count           INTEGER DEFAULT 0,
    created_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create gig_packages table
CREATE TABLE IF NOT EXISTS gig_packages (
    id                      BIGSERIAL PRIMARY KEY,
    gig_id                  BIGINT NOT NULL REFERENCES gigs(id) ON DELETE CASCADE,
    tier                    VARCHAR(20) NOT NULL CHECK (tier IN ('BASIC', 'STANDARD', 'PREMIUM')),
    title                   VARCHAR(255) NOT NULL,
    description             TEXT,
    price_cents             BIGINT NOT NULL,
    currency                VARCHAR(10) NOT NULL,
    delivery_days           INTEGER NOT NULL,
    revisions               INTEGER,
    created_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create gig_media table
CREATE TABLE IF NOT EXISTS gig_media (
    id                      BIGSERIAL PRIMARY KEY,
    gig_id                  BIGINT NOT NULL REFERENCES gigs(id) ON DELETE CASCADE,
    url                     VARCHAR(500) NOT NULL,
    content_type            VARCHAR(100),
    kind                    VARCHAR(20) NOT NULL CHECK (kind IN ('IMAGE', 'VIDEO', 'DOCUMENT')),
    order_index             INTEGER,
    created_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create profile_badges table
CREATE TABLE IF NOT EXISTS profile_badges (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL,
    type                    VARCHAR(100) NOT NULL,
    score                   NUMERIC(4,1),
    issued_at               TIMESTAMP WITH TIME ZONE,
    expires_at              TIMESTAMP WITH TIME ZONE
);

-- Create reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id                      BIGSERIAL PRIMARY KEY,
    gig_id                  BIGINT REFERENCES gigs(id),
    job_id                  BIGINT, -- Not a foreign key, just a reference
    freelancer_id           BIGINT NOT NULL, -- References User.id
    reviewer_id             BIGINT NOT NULL, -- References User.id
    overall_rating          INTEGER,
    comment                 TEXT,
    review_type             VARCHAR(50),
    created_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- -- Create profile_embeddings table
-- CREATE TABLE IF NOT EXISTS profile_embeddings (
--     user_id BIGINT PRIMARY KEY,
--     embedding vector(1536),
--     content_hash VARCHAR(255),
--     skills_text TEXT,
--     profile_summary TEXT,
--     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
-- );

-- -- Create job_embeddings table
-- CREATE TABLE IF NOT EXISTS job_embeddings (
--     job_id BIGINT PRIMARY KEY,
--     embedding vector(1536),
--     content_hash VARCHAR(255),
--     skills_text TEXT,
--     job_summary TEXT,
--     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
-- );

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_gigs_profile_id ON gigs(profile_id);
CREATE INDEX IF NOT EXISTS idx_gigs_status ON gigs(status);
CREATE INDEX IF NOT EXISTS idx_gigs_category ON gigs(category);
CREATE INDEX IF NOT EXISTS idx_gig_packages_gig_id ON gig_packages(gig_id);
CREATE INDEX IF NOT EXISTS idx_gig_media_gig_id ON gig_media(gig_id);
CREATE INDEX IF NOT EXISTS idx_profile_badges_user_id ON profile_badges(user_id);

-- Review indexes for performance
CREATE INDEX IF NOT EXISTS idx_reviews_gig_id ON reviews(gig_id, created_at);
CREATE INDEX IF NOT EXISTS idx_reviews_freelancer_id ON reviews(freelancer_id, created_at);
CREATE INDEX IF NOT EXISTS idx_reviews_reviewer_id ON reviews(reviewer_id, created_at);
CREATE INDEX IF NOT EXISTS idx_reviews_gig_rating ON reviews(gig_id, overall_rating);
CREATE INDEX IF NOT EXISTS idx_reviews_freelancer_rating ON reviews(freelancer_id, overall_rating);

-- Insert some sample data for testing
-- INSERT INTO profiles (user_id, headline, bio, hourly_rate_cents, currency, availability, skills, location_text) 
-- VALUES 
--     (1, 'Full-Stack Developer', 'Experienced developer specializing in React and Node.js', 7500, 'USD', 'PART_TIME', ARRAY['React', 'Node.js', 'PostgreSQL'], 'New York, USA'),
--     (2, 'UI/UX Designer', 'Creative designer with 5+ years experience in web and mobile design', 6000, 'USD', 'FULL_TIME', ARRAY['Figma', 'Adobe XD', 'Photoshop'], 'San Francisco, USA')
-- ON CONFLICT (user_id) DO NOTHING;

-- INSERT INTO gigs (profile_id, title, description, status, category, tags)
-- VALUES 
--     (1, 'I will build a complete React website', 'Custom React website with modern UI/UX and responsive design', 'ACTIVE', 'Web Development', ARRAY['React', 'JavaScript', 'CSS']),
--     (2, 'I will design a modern mobile app UI', 'Beautiful and intuitive mobile app design using latest design trends', 'ACTIVE', 'Design', ARRAY['UI Design', 'Mobile', 'Figma'])
-- ON CONFLICT DO NOTHING;

-- INSERT INTO gig_packages (gig_id, tier, title, description, price_cents, currency, delivery_days, revisions)
-- VALUES 
--     (1, 'BASIC', 'Simple Website', 'Basic React website with 3-5 pages', 50000, 'USD', 7, 2),
--     (1, 'STANDARD', 'Advanced Website', 'React website with backend integration', 100000, 'USD', 14, 3),
--     (1, 'PREMIUM', 'Full-Stack Solution', 'Complete website with admin panel and database', 200000, 'USD', 21, 5),
--     (2, 'BASIC', 'Basic UI Design', 'Mobile app UI for 5-8 screens', 30000, 'USD', 5, 2),
--     (2, 'STANDARD', 'Complete UI/UX', 'Full app design with user flow and prototypes', 60000, 'USD', 10, 3)
-- ON CONFLICT DO NOTHING;

-- ========================================
-- AI Service Knowledge Base Tables
-- ========================================

-- Create knowledge base table for RAG chatbot (used by ai-service)
CREATE TABLE IF NOT EXISTS knowledge_base (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::text,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    content_type TEXT CHECK (content_type IN ('faq', 'guide', 'policy', 'feature')),
    tags TEXT[],
    embedding vector(1536), -- OpenAI ada-002 dimension
    metadata JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create index for vector similarity search using ivfflat
CREATE INDEX IF NOT EXISTS knowledge_base_embedding_idx ON knowledge_base USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- Create text search index for fallback search
CREATE INDEX IF NOT EXISTS knowledge_base_text_search_idx ON knowledge_base USING GIN (to_tsvector('english', title || ' ' || content));

-- Create index on content_type for filtering
CREATE INDEX IF NOT EXISTS knowledge_base_content_type_idx ON knowledge_base (content_type);

-- Create index on tags for tag-based filtering
CREATE INDEX IF NOT EXISTS knowledge_base_tags_idx ON knowledge_base USING GIN (tags);

-- Add trigger for updated_at timestamp (reuse existing function)
CREATE TRIGGER IF NOT EXISTS update_knowledge_base_updated_at 
    BEFORE UPDATE ON knowledge_base 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
