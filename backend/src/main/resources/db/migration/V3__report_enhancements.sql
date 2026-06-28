-- Enhance reports table with new fields
ALTER TABLE reports
    ADD COLUMN IF NOT EXISTS subcategory VARCHAR(60),
    ADD COLUMN IF NOT EXISTS caller_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS call_frequency VARCHAR(30),
    ADD COLUMN IF NOT EXISTS typical_call_hour INTEGER;

-- Add confirmation_count to blocked_numbers (community "me too" count)
ALTER TABLE blocked_numbers
    ADD COLUMN IF NOT EXISTS confirmation_count INTEGER NOT NULL DEFAULT 0;

-- Add new fields to users
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS reputation_score INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS suspended BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS terms_accepted_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_reports_subcategory ON reports(subcategory);
