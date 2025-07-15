-- Create vouchers table to group postings
CREATE TABLE IF NOT EXISTS vouchers
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    number      SMALLINT  NOT NULL,
    date        DATE      NOT NULL,
    description TEXT,
    tenant_id   INTEGER    NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,

    CONSTRAINT fk__vouchers__tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE
);

-- Create indexes for vouchers table
CREATE INDEX idx__vouchers__tenant_id ON vouchers (tenant_id);
CREATE INDEX idx__vouchers__date ON vouchers (date);
CREATE INDEX idx__vouchers__number ON vouchers (number);
-- Unique constraint for voucher number within tenant and year
CREATE UNIQUE INDEX idx__vouchers__tenant_number_year ON vouchers (tenant_id, number, EXTRACT(YEAR FROM date));

-- Add voucher_id foreign key to postings table
ALTER TABLE postings
    ADD COLUMN voucher_id BIGINT,
    ADD CONSTRAINT fk__postings__voucher FOREIGN KEY (voucher_id) REFERENCES vouchers (id) ON DELETE CASCADE;

-- Create index for the new foreign key
CREATE INDEX idx__postings__voucher_id ON postings (voucher_id); 