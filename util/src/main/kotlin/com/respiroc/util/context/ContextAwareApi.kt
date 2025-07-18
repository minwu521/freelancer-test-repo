package com.respiroc.util.context

import com.respiroc.util.exception.MissingTenantContextException

/**
 * Interface for APIs that need access to user context and tenant information.
 * Provides default implementations using Spring Security context.
 */
interface ContextAwareApi {

    /**
     * Gets the current user context from Spring Security context.
     * @return UserContext of the currently authenticated user
     * @throws IllegalStateException if no user is authenticated
     */
    fun user(): UserContext {
        return getCurrentSpringUser().ctx
    }

    /**
     * Extracts the tenant ID from the current user context.
     * @return Current tenant ID from the user's context
     * @throws MissingTenantContextException if no user is authenticated or no current tenant is set
     */
    fun tenantId(): Long {
        val userContext = user()
        return userContext.currentTenant?.id
            ?: throw MissingTenantContextException()
    }

    /**
     * Gets the SpringUser from the current SecurityContext.
     * @return SpringUser instance for the authenticated user
     * @throws IllegalStateException if no user is authenticated
     */
    private fun getCurrentSpringUser(): SpringUser {
        return try {
            org.springframework.security.core.context.SecurityContextHolder.getContext()
                .authentication
                .principal as SpringUser
        } catch (e: Exception) {
            throw IllegalStateException("No authenticated user found in security context", e)
        }
    }
}