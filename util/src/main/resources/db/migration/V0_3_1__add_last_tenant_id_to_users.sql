ALTER TABLE users
    ADD COLUMN last_tenant_id INTEGER;

ALTER TABLE users
    ADD CONSTRAINT users_last_tenant_id_fk FOREIGN KEY (last_tenant_id) REFERENCES tenants (id);