-- Add VAT fields to postings table
ALTER TABLE postings
    ADD COLUMN vat_code   VARCHAR(10),
    ADD COLUMN vat_rate   DECIMAL(5, 2),
    ADD COLUMN vat_amount DECIMAL(15, 2);

-- Add index on vat_code for performance
CREATE INDEX idx_postings_vat_code ON postings (vat_code);