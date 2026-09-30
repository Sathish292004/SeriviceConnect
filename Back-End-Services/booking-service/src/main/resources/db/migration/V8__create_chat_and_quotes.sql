-- ============================================================
-- V8: Create Chat Conversations, Messages, and Quotes
-- ============================================================

-- Conversations Table
CREATE TABLE conversations (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    catalog_item_id BIGINT,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_conversations_customer ON conversations(customer_id, updated_at DESC);
CREATE INDEX idx_conversations_provider ON conversations(provider_id, updated_at DESC);
CREATE INDEX idx_conversations_customer_provider ON conversations(customer_id, provider_id);

-- Messages Table
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL,
    sender_role VARCHAR(30) NOT NULL,
    message TEXT NOT NULL,
    read_status BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_conversation ON messages(conversation_id, created_at ASC);

-- Quotes Table
CREATE TABLE quotes (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    customer_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    catalog_item_id BIGINT,
    service_name VARCHAR(255),
    description VARCHAR(1000) NOT NULL,
    note VARCHAR(1000),
    amount NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_quotes_conversation ON quotes(conversation_id);
CREATE INDEX idx_quotes_customer ON quotes(customer_id);
CREATE INDEX idx_quotes_provider ON quotes(provider_id);

-- Add quote_id to service_requests (Bookings)
ALTER TABLE service_requests ADD COLUMN IF NOT EXISTS quote_id BIGINT REFERENCES quotes(id);
