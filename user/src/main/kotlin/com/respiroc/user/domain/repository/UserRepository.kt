package com.respiroc.user.domain.repository

import com.respiroc.user.domain.model.User
import com.respiroc.util.repository.CustomJpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface UserRepository : CustomJpaRepository<User, Long> {

    fun findByEmail(email: String): User?

    @Query("""
        SELECT user FROM User user, UserSession userSession 
        LEFT JOIN FETCH user.userTenants userTenants
        LEFT JOIN FETCH user.roles roles
        LEFT JOIN FETCH userTenants.tenant tenant
        LEFT JOIN FETCH tenant.company company
        WHERE user.id = userSession.userId 
        AND userSession.token = :token 
        AND userSession.tokenExpireAt > :time 
        AND userSession.tokenRevokedAt is null
        """)
    fun findByToken(@Param("token") token: String, @Param("time") time: Instant): User?

    @Query("""
        SELECT user FROM User user
        LEFT JOIN FETCH user.userTenants userTenants
        LEFT JOIN FETCH user.roles roles
        LEFT JOIN FETCH userTenants.roles userTenantRoles
        LEFT JOIN FETCH userTenantRoles.tenantRole tenantRoles
        LEFT JOIN FETCH tenantRoles.tenantPermissions tenantPermissions
        WHERE user.id = :userId
        AND userTenants.tenantId= :tenantId
        """)
    fun findUserWithTenantRoles(@Param("userId") userId: Long, @Param("tenantId") tenantId: Long): User?
}