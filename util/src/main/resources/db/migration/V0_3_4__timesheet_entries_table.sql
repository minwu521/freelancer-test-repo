CREATE TABLE timesheet_entries (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    employee_name VARCHAR(255) NOT NULL,
    project VARCHAR(255) NOT NULL,
    activity VARCHAR(255) NOT NULL,
    entry_date DATE NOT NULL,
    hours DECIMAL(4,2) NOT NULL,
    comments TEXT,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_timesheet_entries_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

CREATE INDEX idx_timesheet_entries_tenant_id ON timesheet_entries(tenant_id);
CREATE INDEX idx_timesheet_entries_entry_date ON timesheet_entries(entry_date);
CREATE INDEX idx_timesheet_entries_employee_name ON timesheet_entries(employee_name);
CREATE INDEX idx_timesheet_entries_project ON timesheet_entries(project);
CREATE INDEX idx_timesheet_entries_activity ON timesheet_entries(activity);