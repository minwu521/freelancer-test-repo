CREATE TABLE private_persons
(
    id         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    address_id INTEGER,

    CONSTRAINT private_persons_address_id_fk FOREIGN KEY (address_id) REFERENCES addresses (id)
);

CREATE UNIQUE INDEX private_persons_name_unique ON private_persons (name);


CREATE TABLE customers
(
    id                  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id           INTEGER       NOT NULL,
    company_id          INTEGER,
    private_person_id   INTEGER,

    CONSTRAINT fk__customers__tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE,
    CONSTRAINT fk__customers__company FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
    CONSTRAINT fk__customers__private_person FOREIGN KEY (private_person_id) REFERENCES private_persons (id) ON DELETE CASCADE
);

CREATE INDEX idx__customers__tenant_id ON customers (tenant_id);

CREATE TABLE suppliers
(
    id                  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id           INTEGER       NOT NULL,
    company_id          INTEGER,
    private_person_id   INTEGER,

    CONSTRAINT fk__suppliers__tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE,
    CONSTRAINT fk__suppliers__company FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
    CONSTRAINT fk__suppliers__private_person FOREIGN KEY (private_person_id) REFERENCES private_persons (id) ON DELETE CASCADE
);

CREATE INDEX idx__suppliers__tenant_id ON suppliers (tenant_id);