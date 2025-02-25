-- Create table for storing embeddings and chat history
CREATE TABLE IF NOT EXISTS chat_history (
    id SERIAL PRIMARY KEY,
    prompt TEXT NOT NULL,
    response TEXT NOT NULL,
    embedding TEXT NOT NULL, -- Store embedding as JSON array string
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- CREATE EXTENSION IF NOT EXISTS vector;
-- CREATE EXTENSION IF NOT EXISTS hstore;
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- CREATE TABLE IF NOT EXISTS vector_store (
-- 	id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
-- 	content text,
-- 	metadata json,
-- 	embedding vector(1536)
-- );

-- CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);
