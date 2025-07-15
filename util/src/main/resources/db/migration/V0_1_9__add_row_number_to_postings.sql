-- Add row_number column to postings table
ALTER TABLE postings
    ADD COLUMN row_number SMALLINT;

-- Update any existing null values to 0
UPDATE postings SET row_number = 0 WHERE row_number IS NULL;

-- Make row_number NOT NULL with default 0
ALTER TABLE postings
    ALTER COLUMN row_number SET NOT NULL
