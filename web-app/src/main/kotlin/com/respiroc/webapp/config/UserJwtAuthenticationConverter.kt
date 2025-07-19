package com.respiroc.webapp.config

import com.respiroc.user.application.UserService
import com.respiroc.util.context.SpringUser
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt

class UserJwtAuthenticationConverter(
    private val userService: UserService
) : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(source: Jwt): AbstractAuthenticationToken? {
        val userId = source.subject.toLong()
        val tenantId = source.getClaim<Long>("tenantId")
        val ctx = userService.findByIdAndTenantId(userId, tenantId) ?: return null
        val principal = SpringUser(ctx)
        return UsernamePasswordAuthenticationToken(principal, source.tokenValue, principal.authorities)
    }
}