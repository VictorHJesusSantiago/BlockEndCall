-- Server-side blocked call log
CREATE TABLE IF NOT EXISTS server_blocked_call_log (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    phone_number    VARCHAR(30) NOT NULL,
    blocked_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    block_result    VARCHAR(30) NOT NULL DEFAULT 'REJECTED',
    matched_number_id BIGINT REFERENCES blocked_numbers(id) ON DELETE SET NULL
);

-- API keys for third-party consumers
CREATE TABLE IF NOT EXISTS user_api_keys (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    key_value   VARCHAR(64) NOT NULL UNIQUE,
    label       VARCHAR(100),
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    last_used_at TIMESTAMP
);

-- User badges
CREATE TABLE IF NOT EXISTS user_badges (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    badge_type  VARCHAR(50) NOT NULL,
    awarded_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, badge_type)
);

-- Audit log
CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGSERIAL PRIMARY KEY,
    actor_id    BIGINT REFERENCES users(id) ON DELETE SET NULL,
    action      VARCHAR(60) NOT NULL,
    target_type VARCHAR(60),
    target_id   BIGINT,
    details     TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_scl_user ON server_blocked_call_log(user_id);
CREATE INDEX IF NOT EXISTS idx_scl_phone ON server_blocked_call_log(phone_number);
CREATE INDEX IF NOT EXISTS idx_audit_actor ON audit_log(actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_log(created_at DESC);
