CREATE TABLE IF NOT EXISTS user_roles
(
    id      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,

    CONSTRAINT fk__user_roles__role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk__user_roles__user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq__user_roles__role_user UNIQUE (role_id, user_id)
);