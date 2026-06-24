CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    phone       VARCHAR(20),
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20) NOT NULL DEFAULT 'USER',
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE blocked_numbers (
    id              BIGSERIAL PRIMARY KEY,
    phone_number    VARCHAR(30) NOT NULL UNIQUE,
    category        VARCHAR(50) NOT NULL,
    report_count    INTEGER NOT NULL DEFAULT 1,
    confirmed       BOOLEAN NOT NULL DEFAULT FALSE,
    description     TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE reports (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id),
    blocked_number_id   BIGINT NOT NULL REFERENCES blocked_numbers(id),
    description         TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, blocked_number_id)
);

CREATE INDEX idx_blocked_numbers_phone ON blocked_numbers(phone_number);
CREATE INDEX idx_reports_user ON reports(user_id);
CREATE INDEX idx_reports_number ON reports(blocked_number_id);
