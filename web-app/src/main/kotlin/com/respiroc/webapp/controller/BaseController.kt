package com.respiroc.webapp.controller

import com.respiroc.util.context.SpringUser
import com.respiroc.util.context.TenantInfo
import com.respiroc.util.context.UserContext
import com.respiroc.util.context.UserTenantContext
import com.respiroc.util.exception.MissingTenantContextException
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.ui.Model

open class BaseController {

    val titleAttributeName: String = "title"
    val errorMessageAttributeName: String = "error"
    val userAttributeName: String = "user"
    val tenantsAttributeName: String = "tenants"
    val currentTenantAttributeName: String = "currentTenant"
    val calloutAttributeName: String = "callout"

    fun springUser(): SpringUser {
        return SecurityContextHolder.getContext().authentication.principal as SpringUser
    }

    fun user(): UserContext {
        return springUser().ctx
    }

    fun tenantId(): Long {
        return user().currentTenant?.id ?: throw MissingTenantContextException()
    }

    fun currentTenant(): UserTenantContext {
        return user().currentTenant ?: throw MissingTenantContextException()
    }

    fun tenants(): List<TenantInfo> {
        return user().tenants
    }

    fun countryCode(): String {
        return user().currentTenant?.countryCode ?: throw IllegalStateException("No current tenant is set for the user")
    }

    fun addCommonAttributesForCurrentTenant(
        model: Model,
        title: String,
        useCurrentCompanyAsTitlePrefix: Boolean = false
    ) {
        val springUser = springUser()
        val currentTenant = currentTenant()
        val tenants = tenants()

        model.addAttribute(userAttributeName, springUser)
        model.addAttribute(currentTenantAttributeName, currentTenant)
        model.addAttribute(tenantsAttributeName, tenants)
        model.addAttribute(titleAttributeName, "${currentTenant.companyName} - ${title}")
    }

    fun addCommonAttributes(
        model: Model,
        title: String,
    ) {
        val springUser = springUser()
        val tenants = tenants()

        model.addAttribute(userAttributeName, springUser)
        model.addAttribute(tenantsAttributeName, tenants)
        model.addAttribute(titleAttributeName, title)
    }

    fun isUserLoggedIn(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication != null &&
                authentication.isAuthenticated &&
                authentication !is AnonymousAuthenticationToken
    }
}