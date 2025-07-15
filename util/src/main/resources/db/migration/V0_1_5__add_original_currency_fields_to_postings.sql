-- Add original currency fields to postings table
ALTER TABLE postings
    ADD COLUMN original_amount   DECIMAL(15, 2),
    ADD COLUMN original_currency VARCHAR(3);

-- Add index on original_currency for performance
CREATE INDEX idx_postings_original_currency ON postings (original_currency);