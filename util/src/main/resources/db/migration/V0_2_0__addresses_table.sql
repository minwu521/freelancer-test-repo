CREATE TABLE IF NOT EXISTS addresses
(
    id                           INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    country_iso_code             CHAR(2)     NOT NULL,
    administrative_division_code VARCHAR(10),
    city                         VARCHAR(40) NOT NULL,
    postal_code                  VARCHAR(20),
    address_1                    VARCHAR(75) NOT NULL,
    address_2                    VARCHAR(75)
);

CREATE UNIQUE INDEX idx_addresses_country_postal_city_unique
    ON addresses (country_iso_code, administrative_division_code, city, postal_code, address_1, address_2);

ALTER TABLE companies
    ADD IF NOT EXISTS address_id INTEGER,
    ADD CONSTRAINT companies_addresses_id_fk FOREIGN KEY (address_id) REFERENCES addresses (id);