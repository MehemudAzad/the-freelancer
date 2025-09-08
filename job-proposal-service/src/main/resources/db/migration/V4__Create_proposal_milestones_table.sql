-- Create proposal_milestones table
CREATE TABLE proposal_milestones (
    id BIGSERIAL PRIMARY KEY,
    proposal_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    amount_cents BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    due_date DATE,
    order_index INTEGER NOT NULL DEFAULT 0,
    dod TEXT, -- Definition of Done stored as JSON string
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint (assuming proposals table exists)
    CONSTRAINT fk_proposal_milestones_proposal_id 
        FOREIGN KEY (proposal_id) 
        REFERENCES proposals(id) 
        ON DELETE CASCADE,
    
    -- Ensure proper ordering within a proposal
    CONSTRAINT uk_proposal_milestones_proposal_order 
        UNIQUE (proposal_id, order_index)
);

-- Create indexes for better query performance
CREATE INDEX idx_proposal_milestones_proposal_id ON proposal_milestones(proposal_id);
CREATE INDEX idx_proposal_milestones_order ON proposal_milestones(proposal_id, order_index);

-- Add trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_proposal_milestones_updated_at 
    BEFORE UPDATE ON proposal_milestones 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
