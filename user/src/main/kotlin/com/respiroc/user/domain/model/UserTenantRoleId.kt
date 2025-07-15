package com.respiroc.user.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class UserTenantRoleId(
    @Column(name = "user_tenant_id")
    val userTenantId: Long = -1,

    @Column(name = "tenant_role_id")
    val tenantRoleId: Long = -1
) : Serializable