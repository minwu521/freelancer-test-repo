-- Create the attachments table
CREATE TABLE IF NOT EXISTS attachments
(
    id         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    file_data  BYTEA        NOT NULL,
    filename   VARCHAR(255) NOT NULL,
    mimetype   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create the voucher reception documents table
CREATE TABLE IF NOT EXISTS voucher_reception_documents
(
    id            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    attachment_id INTEGER   NOT NULL,
    received_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sender_email  VARCHAR(255),
    tenant_id     INTEGER   NOT NULL,
    CONSTRAINT fk_voucher_reception_documents_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_voucher_reception_documents_attachment FOREIGN KEY (attachment_id) REFERENCES attachments (id)
);

-- Add slug to tenants table
ALTER TABLE tenants
    ADD COLUMN slug VARCHAR(255);

-- Add unique constraint to slug
CREATE UNIQUE INDEX IF NOT EXISTS idx_tenants_slug ON tenants (slug);