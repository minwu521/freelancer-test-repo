CREATE TABLE IF NOT EXISTS voucher_documents
(
    id            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    attachment_id INTEGER,
    received_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sender_email  VARCHAR(255),
    tenant_id     BIGINT    NOT NULL,
    CONSTRAINT fk_voucher_documents_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);