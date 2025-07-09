CREATE TABLE IF NOT EXISTS voucher_documents (
    id BIGSERIAL PRIMARY KEY,
    attached_voucher_id BIGINT,
    company_id BIGINT NOT NULL,
    file_data BYTEA,
    file_size BIGINT,
    filename VARCHAR(255) NOT NULL,
    mime_type VARCHAR(255),
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sender_email VARCHAR(255),
    tenant_id BIGINT NOT NULL,
    CONSTRAINT fk_voucher_documents_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_voucher_documents_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);