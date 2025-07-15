CREATE TABLE user_tenants
(
    user_id   INTEGER NOT NULL,
    tenant_id INTEGER NOT NULL,
    id        INTEGER GENERATED ALWAYS AS IDENTITY,

    CONSTRAINT user_tenants_users_id_fk FOREIGN KEY (user_id) REFERENCES users,
    CONSTRAINT user_tenants_tenants_id_fk FOREIGN KEY (tenant_id) REFERENCES tenants,
    CONSTRAINT user_tenants_pk PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_tenant_roles
(
    user_tenant_id INTEGER NOT NULL,
    tenant_role_id INTEGER NOT NULL,

    CONSTRAINT user_tenant_roles_tenant_roles_id_fk FOREIGN KEY (tenant_role_id) REFERENCES tenant_roles (id),
    CONSTRAINT user_tenant_roles_user_tenants_id_fk FOREIGN KEY (user_tenant_id) REFERENCES user_tenants (id),
    CONSTRAINT user_tenant_roles_pk PRIMARY KEY (tenant_role_id, user_tenant_id)
);

CREATE INDEX user_tenant_roles_user_tenant_id_index ON user_tenant_roles (user_tenant_id);
