-- Announcements from admins
CREATE TABLE IF NOT EXISTS announcements (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    body        TEXT NOT NULL,
    author_id   BIGINT REFERENCES users(id) ON DELETE SET NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- User preferences (server-side, for cross-device sync)
CREATE TABLE IF NOT EXISTS user_preferences (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE UNIQUE,
    block_only_confirmed    BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_confirm       BOOLEAN NOT NULL DEFAULT TRUE,
    sensitivity             INTEGER NOT NULL DEFAULT 5,
    paranoia_mode           BOOLEAN NOT NULL DEFAULT FALSE,
    block_telemarketing     BOOLEAN NOT NULL DEFAULT TRUE,
    block_scam              BOOLEAN NOT NULL DEFAULT TRUE,
    block_robocall          BOOLEAN NOT NULL DEFAULT TRUE,
    block_silent            BOOLEAN NOT NULL DEFAULT FALSE,
    voicemail_mode          BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

-- FCM tokens for push notifications
CREATE TABLE IF NOT EXISTS fcm_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL,
    device_id   VARCHAR(100),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, token)
);
