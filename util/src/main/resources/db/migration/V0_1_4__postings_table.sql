CREATE TABLE IF NOT EXISTS postings
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_number VARCHAR(10)    NOT NULL,
    amount         DECIMAL(15, 2) NOT NULL,
    currency       VARCHAR(3)     NOT NULL,
    posting_date   DATE           NOT NULL,
    description    TEXT,
    tenant_id      INTEGER         NOT NULL,
    created_at     TIMESTAMP      NOT NULL,
    updated_at     TIMESTAMP      NOT NULL,

    CONSTRAINT fk__postings__tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE
);

CREATE INDEX idx__postings__account_number ON postings (account_number);
CREATE INDEX idx__postings__tenant_id ON postings (tenant_id);
CREATE INDEX idx__postings__posting_date ON postings (posting_date); 