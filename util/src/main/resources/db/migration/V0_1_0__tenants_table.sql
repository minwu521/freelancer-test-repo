CREATE TABLE IF NOT EXISTS tenants
(
    id         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    company_id INTEGER   NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT tenants_companies_id_fk FOREIGN KEY (company_id) REFERENCES companies (id)
);
