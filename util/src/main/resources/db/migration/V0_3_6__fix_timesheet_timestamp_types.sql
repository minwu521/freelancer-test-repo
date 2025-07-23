-- Change timestamp columns back to timestamp without time zone for LocalDateTime
ALTER TABLE timesheet_entries 
    ALTER COLUMN created_at TYPE TIMESTAMP WITHOUT TIME ZONE,
    ALTER COLUMN updated_at TYPE TIMESTAMP WITHOUT TIME ZONE;