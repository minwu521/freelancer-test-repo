package com.respiroc.webapp.config

import com.respiroc.util.context.SpringUser
import org.hibernate.cfg.AvailableSettings
import org.hibernate.context.spi.CurrentTenantIdentifierResolver
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class TenantIdentifierResolver : CurrentTenantIdentifierResolver<Long>, HibernatePropertiesCustomizer {

    override fun resolveCurrentTenantIdentifier(): Long? {
        val auth = SecurityContextHolder.getContext().authentication ?: return null
        val principal = auth.principal
        if (principal !is SpringUser) return null
        return principal.ctx.currentTenant?.id ?: null
    }

    override fun validateExistingCurrentSessions(): Boolean {
        return false
    }

    override fun customize(hibernateProperties: MutableMap<String, Any>) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this)
    }
}