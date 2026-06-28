-- Names callers claim to be (user-submitted)
CREATE TABLE IF NOT EXISTS number_reported_names (
    id                  BIGSERIAL PRIMARY KEY,
    blocked_number_id   BIGINT NOT NULL REFERENCES blocked_numbers(id) ON DELETE CASCADE,
    reported_name       VARCHAR(200) NOT NULL,
    report_count        INTEGER NOT NULL DEFAULT 1,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (blocked_number_id, reported_name)
);

-- Public whitelist of known legitimate numbers
CREATE TABLE IF NOT EXISTS public_whitelist (
    id              BIGSERIAL PRIMARY KEY,
    phone_number    VARCHAR(30) NOT NULL UNIQUE,
    organization    VARCHAR(200) NOT NULL,
    category        VARCHAR(100),
    verified        BOOLEAN NOT NULL DEFAULT FALSE,
    added_by        BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Number activity timeline events
CREATE TABLE IF NOT EXISTS number_timeline_events (
    id                  BIGSERIAL PRIMARY KEY,
    blocked_number_id   BIGINT NOT NULL REFERENCES blocked_numbers(id) ON DELETE CASCADE,
    event_type          VARCHAR(60) NOT NULL,
    details             TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_nrn_number ON number_reported_names(blocked_number_id);
CREATE INDEX IF NOT EXISTS idx_nte_number ON number_timeline_events(blocked_number_id);
CREATE INDEX IF NOT EXISTS idx_nte_created ON number_timeline_events(created_at DESC);
