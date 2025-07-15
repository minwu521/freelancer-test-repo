CREATE TABLE IF NOT EXISTS companies
(
    id                  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    organization_number VARCHAR(50)  NOT NULL,
    name                VARCHAR(255) NOT NULL,
    country_code        VARCHAR(2)   NOT NULL,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL
);
