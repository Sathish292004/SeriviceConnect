-- ============================================================
-- V10: Add Email Delivery Tracking to Notifications
-- ============================================================

ALTER TABLE notifications ADD COLUMN IF NOT EXISTS email_sent BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS email_sent_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS email_recipient VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_notifications_email_sent ON notifications(email_sent);

-- Ensure notification_preferences has email_enabled
ALTER TABLE notification_preferences ADD COLUMN IF NOT EXISTS email_enabled BOOLEAN NOT NULL DEFAULT TRUE;
