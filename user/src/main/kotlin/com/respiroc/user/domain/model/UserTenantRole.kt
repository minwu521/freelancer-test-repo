package com.respiroc.user.domain.model

import com.respiroc.tenant.domain.model.TenantRole
import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "user_tenant_roles")
class UserTenantRole() : Serializable {

    @EmbeddedId
    lateinit var id: UserTenantRoleId

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userTenantId")
    @JoinColumn(name = "user_tenant_id", nullable = false, updatable = false, insertable = false)
    lateinit var userTenant: UserTenant

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tenantRoleId")
    @JoinColumn(name = "tenant_role_id", nullable = false, updatable = false, insertable = false)
    lateinit var tenantRole: TenantRole

    constructor(
        id: UserTenantRoleId,
        userTenant: UserTenant,
        tenantRole: TenantRole
    ) : this() {
        this.id = id
        this.userTenant = userTenant
        this.tenantRole = tenantRole
    }
}