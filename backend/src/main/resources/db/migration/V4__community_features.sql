-- "Me too" confirmations (users who also received spam from this number)
CREATE TABLE IF NOT EXISTS report_confirmations (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    blocked_number_id   BIGINT NOT NULL REFERENCES blocked_numbers(id) ON DELETE CASCADE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, blocked_number_id)
);

-- Personal whitelist per user
CREATE TABLE IF NOT EXISTS user_personal_whitelist (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    phone_number VARCHAR(30) NOT NULL,
    note        VARCHAR(200),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, phone_number)
);

-- Personal blacklist per user
CREATE TABLE IF NOT EXISTS user_personal_blacklist (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    phone_number VARCHAR(30) NOT NULL,
    note        VARCHAR(200),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, phone_number)
);

CREATE INDEX IF NOT EXISTS idx_rc_number ON report_confirmations(blocked_number_id);
CREATE INDEX IF NOT EXISTS idx_pwl_user ON user_personal_whitelist(user_id);
CREATE INDEX IF NOT EXISTS idx_pbl_user ON user_personal_blacklist(user_id);
