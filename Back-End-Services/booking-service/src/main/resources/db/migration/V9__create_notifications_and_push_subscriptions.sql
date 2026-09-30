-- ============================================================
-- V9: Create Notifications and Push Subscriptions Tables
-- ============================================================

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_user_id BIGINT NOT NULL,
    type VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    related_entity_type VARCHAR(64),
    related_entity_id BIGINT,
    deep_link VARCHAR(255),
    read BOOLEAN NOT NULL DEFAULT FALSE,
    idempotency_key VARCHAR(255) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_recipient_read ON notifications(recipient_user_id, read);
CREATE INDEX idx_notifications_recipient_created ON notifications(recipient_user_id, created_at DESC);
CREATE INDEX idx_notifications_idempotency ON notifications(idempotency_key);

CREATE TABLE push_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    endpoint TEXT NOT NULL UNIQUE,
    p256dh TEXT NOT NULL,
    auth TEXT NOT NULL,
    user_agent VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_push_sub_user ON push_subscriptions(user_id);

CREATE TABLE notification_preferences (
    user_id BIGINT PRIMARY KEY,
    chat_messages BOOLEAN NOT NULL DEFAULT TRUE,
    quotes BOOLEAN NOT NULL DEFAULT TRUE,
    bookings BOOLEAN NOT NULL DEFAULT TRUE,
    support BOOLEAN NOT NULL DEFAULT TRUE,
    reviews BOOLEAN NOT NULL DEFAULT TRUE,
    reminders BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
