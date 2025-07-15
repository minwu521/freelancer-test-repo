CREATE TABLE IF NOT EXISTS tenant_roles
(
    id          INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    code        VARCHAR(128) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL
);