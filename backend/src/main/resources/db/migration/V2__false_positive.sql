ALTER TABLE blocked_numbers
    ADD COLUMN IF NOT EXISTS false_positive_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS whitelisted           BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS false_positive_reports (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id),
    blocked_number_id   BIGINT NOT NULL REFERENCES blocked_numbers(id),
    reason              TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, blocked_number_id)
);

CREATE INDEX IF NOT EXISTS idx_fp_number ON false_positive_reports(blocked_number_id);
