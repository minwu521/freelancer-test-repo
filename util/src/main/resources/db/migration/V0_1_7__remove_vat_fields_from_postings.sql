-- Remove VAT rate and amount fields from postings table
-- These fields are being replaced with separate VAT postings approach
ALTER TABLE postings
    DROP COLUMN IF EXISTS vat_rate,
    DROP COLUMN IF EXISTS vat_amount; 