package com.respiroc.webapp.service

import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.*
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class JwtService(
    private val jwtDecoder: JwtDecoder,
    private val jwtEncoder: JwtEncoder
) {

    companion object {
        const val TENANT_ID_KEY = "tenantId"
        const val DEFAULT_PERIOD: Long = 24 * 60 * 60 * 1000
    }

    fun extractSubject(token: String): String = jwtDecoder.decode(token).subject

    fun extractTenantId(token: String): Long? =
        (jwtDecoder.decode(token).claims[TENANT_ID_KEY] as? Number)?.toLong()

    fun extractExpiration(token: String): Instant = jwtDecoder.decode(token).expiresAt!!

    fun generateToken(subject: String, tenantId: Long?, period: Long = DEFAULT_PERIOD): String {
        val headers = JwsHeader.with(MacAlgorithm.HS256).build()

        val claims = JwtClaimsSet.builder()
            .subject(subject)
            .claim(TENANT_ID_KEY, tenantId)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusMillis(period))
            .build()

        return jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).tokenValue
    }

    fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).isBefore(Instant.now())
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            !isTokenExpired(token)
        } catch (_: JwtException) {
            false
        }
    }
}