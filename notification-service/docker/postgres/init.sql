-- Notification Service Database Initialization Script

-- Create the database (if it doesn't exist)
-- This is typically handled by POSTGRES_DB environment variable
-- CREATE DATABASE notification_db;

-- Connect to the notification database
\c notification_db;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create enum types
DO $$ BEGIN
    CREATE TYPE notification_type AS ENUM (
        'PROPOSAL_SUBMITTED',
        'PROPOSAL_ACCEPTED', 
        'PROPOSAL_REJECTED',
        'JOB_POSTED',
        'CONTRACT_CREATED',
        'MILESTONE_COMPLETED',
        'PAYMENT_RELEASED',
        'SYSTEM_ANNOUNCEMENT'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE notification_status AS ENUM (
        'PENDING',
        'SENT',
        'DELIVERED',
        'FAILED',
        'CANCELLED'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    sender_id BIGINT,
    type notification_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    job_id BIGINT,
    reference_id BIGINT,
    reference_type VARCHAR(50),
    status notification_status DEFAULT 'PENDING',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    delivered_at TIMESTAMP,
    retry_count INTEGER DEFAULT 0,
    
    -- Indexes for better performance
    INDEX idx_notifications_recipient_id (recipient_id),
    INDEX idx_notifications_sender_id (sender_id),
    INDEX idx_notifications_type (type),
    INDEX idx_notifications_job_id (job_id),
    INDEX idx_notifications_status (status),
    INDEX idx_notifications_is_read (is_read),
    INDEX idx_notifications_created_at (created_at),
    INDEX idx_notifications_reference (reference_id, reference_type)
);

-- Create indexes separately (for compatibility)
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_created ON notifications(recipient_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_unread ON notifications(recipient_id, is_read) WHERE is_read = FALSE;
CREATE INDEX IF NOT EXISTS idx_notifications_status_retry ON notifications(status, retry_count) WHERE status = 'FAILED';

-- Insert some sample notifications (optional)
INSERT INTO notifications (recipient_id, sender_id, type, title, message, job_id, status, is_read)
VALUES 
    (1, 2, 'PROPOSAL_SUBMITTED', 'New Proposal Received', 'John Doe has submitted a proposal for your job "Website Development"', 1, 'DELIVERED', FALSE),
    (2, 1, 'PROPOSAL_ACCEPTED', 'Proposal Accepted!', 'Congratulations! Jane Smith has accepted your proposal for "Mobile App Design"', 2, 'DELIVERED', FALSE),
    (3, NULL, 'SYSTEM_ANNOUNCEMENT', 'Welcome to FreelancerHub', 'Thank you for joining our platform! Start exploring opportunities.', NULL, 'DELIVERED', TRUE)
ON CONFLICT DO NOTHING;

-- Grant permissions to the notification user
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO notification_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO notification_user;
