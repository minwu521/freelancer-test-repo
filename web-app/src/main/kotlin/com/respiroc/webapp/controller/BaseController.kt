package com.respiroc.webapp.controller

import com.respiroc.util.context.SpringUser
import com.respiroc.util.context.UserContext
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
        return user().currentTenant?.id ?: throw IllegalStateException("No current tenant is set for the user")
    }

    fun countryCode(): String {
        return user().currentTenant?.countryCode ?: throw IllegalStateException("No current tenant is set for the user")
    }

    fun addCommonAttributes(
        model: Model,
        title: String,
        useCurrentCompanyAsTitlePrefix: Boolean = false
    ) {
        val springUser = springUser()
        model.addAttribute(userAttributeName, springUser)

        model.addAttribute(tenantsAttributeName, springUser.ctx.tenants)

        val currentTenant = springUser.ctx.currentTenant
        if (currentTenant != null)
            model.addAttribute(currentTenantAttributeName, currentTenant)
        if (useCurrentCompanyAsTitlePrefix && currentTenant != null)
            model.addAttribute(titleAttributeName, "${currentTenant.companyName} - ${title}")
        else
            model.addAttribute(titleAttributeName, title)
    }
}