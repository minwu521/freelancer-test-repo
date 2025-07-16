package com.respiroc.tenant.domain.repository

import com.respiroc.tenant.domain.model.Tenant
import com.respiroc.util.repository.CustomJpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TenantRepository : CustomJpaRepository<Tenant, Long> {
    fun findBySlug(slug: String): Tenant?
} {
}