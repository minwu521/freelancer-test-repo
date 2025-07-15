CREATE TABLE IF NOT EXISTS users
(
    id            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email         VARCHAR(128) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_enabled    BOOLEAN   DEFAULT TRUE,
    is_locked     BOOLEAN   DEFAULT FALSE,
    last_login_at TIMESTAMP DEFAULT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL
);

CREATE INDEX idx__users__email ON users (email);