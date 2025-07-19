package com.respiroc.webapp.config

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver
import org.springframework.stereotype.Component

@Component
class JwtCookieBearerTokenResolver : BearerTokenResolver {

    companion object {
        const val JWT_COOKIE_NAME = "token"
    }

    override fun resolve(request: HttpServletRequest?): String? {
        val cookies = request?.cookies ?: return null
        return cookies.find { it.name == JWT_COOKIE_NAME }?.value
    }
}