CREATE TABLE IF NOT EXISTS role_permissions
(
    id            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_id       INTEGER NOT NULL,
    permission_id INTEGER NOT NULL,

    CONSTRAINT fk__role_permissions__role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk__role_permissions__permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE,
    CONSTRAINT uq__role_permissions__role_permission UNIQUE (role_id, permission_id)
);