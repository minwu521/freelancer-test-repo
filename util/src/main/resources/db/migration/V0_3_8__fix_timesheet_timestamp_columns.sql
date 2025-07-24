-- Fix timestamp columns to use timestamp with time zone
ALTER TABLE timesheet_entries 
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE,
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE;