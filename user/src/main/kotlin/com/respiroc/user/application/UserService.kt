package com.respiroc.user.application

import com.respiroc.tenant.domain.model.Tenant
import com.respiroc.tenant.domain.model.TenantPermission
import com.respiroc.tenant.domain.model.TenantRole
import com.respiroc.user.application.payload.ForgotPayload
import com.respiroc.user.application.payload.LoginPayload
import com.respiroc.user.application.jwt.JwtUtils
import com.respiroc.user.domain.model.*
import com.respiroc.user.domain.repository.UserRepository
import com.respiroc.user.domain.repository.UserSessionRepository
import com.respiroc.user.domain.repository.UserTenantRepository
import com.respiroc.user.domain.repository.UserTenantRoleRepository
import com.respiroc.util.context.*
import com.respiroc.util.currency.CurrencyService
import org.springframework.security.authentication.AccountStatusUserDetailsChecker
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.attribute.UserPrincipalNotFoundException
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val userSessionRepository: UserSessionRepository,
    private val userTenantRoleRepository: UserTenantRoleRepository,
    private val userTenantRepository: UserTenantRepository,
    private val jwt: JwtUtils,
    private val currencyService: CurrencyService
) {

    private val passwordEncoder = BCryptPasswordEncoder()

    private val JWT_TOKEN_PERIOD: Long = 24 * 60 * 60 * 1000

    fun signupByEmailPassword(email: String, password: String): LoginPayload {
        val existUser = userRepository.findByEmail(email)
        if (existUser != null) {
            throw IllegalArgumentException("User already exists")
        }

        val newUser = User()
        newUser.email = email
        newUser.passwordHash = passwordEncoder.encode(password)
        return signup(newUser)
    }

    fun loginByEmailPassword(
        email: String,
        password: String
    ): LoginPayload {
        val user = userRepository.findByEmail(email) ?: throw UsernameNotFoundException("Email not found")
        if (!passwordEncoder.matches(password, user.passwordHash)) throw BadCredentialsException("Login incorrect")

        return login(user)
    }

    fun changePassword(
        user: SpringUser,
        oldPassword: String,
        newPassword: String
    ) {
        TODO("Not yet implemented")
    }

    fun logout(token: String) {
        TODO("Not yet implemented")
    }

    fun forgetPassword(
        email: String,
        ipAddress: String
    ): ForgotPayload {
        TODO("Not yet implemented")
    }

    fun resetPassword(token: String, newPassword: String) {
        TODO("Not yet implemented")
    }

    fun setPassword(user: SpringUser, newPassword: String) {
        TODO("Not yet implemented")
    }

    fun findByToken(token: String): UserContext? {
        return userRepository.findByToken(token, Instant.now())?.let { user ->
            if (jwt.isTokenValid(token = token, subject = user.email)) {
                user.toUserContext()
            } else {
                null
            }
        }
    }

    fun findByEmail(email: String): UserContext? {
        TODO("Not yet implemented")
    }

    fun findTenantRoles(userId: Long, tenantId: Long): List<TenantRoleContext> {
        return userRepository.findUserWithTenantRoles(userId, tenantId)!!.userTenants.single().roles.map {
            it.tenantRole.toTenantRoleContext()
        }
    }

    fun generateToken(user: SpringUser): String {
        TODO("Not yet implemented")
    }

    fun addUserTenantRole(
        tenant: Tenant,
        role: TenantRole,
        user: UserContext
    ) {
        val existUser = userRepository.findByEmail(user.email) ?: throw UserPrincipalNotFoundException("User not found")
        val userTenant = getOrCreateUserTenant(existUser.id, tenant.id)
        val userTenantRoleId = UserTenantRoleId(tenant.id, existUser.id)
        val userTenantRole = UserTenantRole(userTenantRoleId, userTenant, role)
        userTenantRoleRepository.save(userTenantRole)
    }

    fun getOrCreateUserTenant(userId: Long, tenantId: Long): UserTenant {
        return userTenantRepository.findUserTenantByUserIdAndTenantId(userId, tenantId)
            ?: run {
                val tenant = Tenant()
                tenant.id = tenantId
                val user = User()
                user.id = userId
                val userTenant = UserTenant()
                userTenant.tenant = tenant
                userTenant.user = user
                userTenantRepository.save(userTenant)
            }
    }

    // ---------------------------------
    // Private Helper
    // ---------------------------------

    private fun signup(user: User): LoginPayload {
        val savedUser: User = userRepository.saveAndFlush(user)
        return login(savedUser)
    }

    private fun login(user: User): LoginPayload {
        val springUser = SpringUser(user.toUserContext())
        AccountStatusUserDetailsChecker().check(springUser)

        val token = jwt.generateToken(springUser.username, JWT_TOKEN_PERIOD)

        val userSession = UserSession()
        userSession.user = user
        userSession.token = token
        userSession.tokenIssueAt = Instant.now()
        userSession.tokenExpireAt = Instant.now().plus(1, ChronoUnit.DAYS)
        userSessionRepository.save(userSession)

        user.lastLoginAt = Instant.now()
        userRepository.save(user)

        return LoginPayload(token, springUser)
    }

    private fun User.toUserContext(): UserContext {
        return UserContext(
            id = this.id,
            email = this.email,
            password = this.passwordHash,
            isEnabled = this.isEnabled,
            isLocked = this.isLocked,
            currentTenant = null, // Should be set on tenant filter process
            tenants = this.getTenantsInfo(),
            roles = this.roles.map { it -> it.toRoleContext() }.toList()
        )
    }

    private fun User.getTenantsInfo(): List<TenantInfo> {
        return this.userTenants.map {
            val tenant = it.tenant
            TenantInfo(
                tenant.id,
                tenant.getCompanyName(),
                currencyService.getCompanyCurrency(tenant.getCompanyCountryCode())
            )
        }.sortedBy { it.id }
    }

    private fun TenantRole.toTenantRoleContext(): TenantRoleContext {
        return TenantRoleContext(
            name = this.name,
            code = this.code,
            description = this.description,
            permissions = this.tenantPermissions.map { it.toTenantPermissionContext() }.toList()
        )
    }

    private fun TenantPermission.toTenantPermissionContext(): TenantPermissionContext {
        return TenantPermissionContext(
            name = this.name,
            code = this.code,
            description = this.description
        )
    }

    private fun Role.toRoleContext(): RoleContext {
        return RoleContext(
            name = this.name,
            code = this.code,
            description = this.description,
            permissions = this.permissions.map { it.toPermissionContext() }.toList()
        )
    }

    private fun Permission.toPermissionContext(): PermissionContext {
        return PermissionContext(
            name = this.name,
            code = this.code,
            description = this.description
        )
    }
}