-- Create vector extension for PostgreSQL
CREATE EXTENSION IF NOT EXISTS vector;

-- Create knowledge base table for RAG chatbot
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

-- Add trigger for updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_knowledge_base_updated_at BEFORE UPDATE ON knowledge_base FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();