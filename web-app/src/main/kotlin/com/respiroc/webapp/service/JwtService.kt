package com.respiroc.webapp.service

import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class JwtService(
    private val jwtEncoder: JwtEncoder
) {

    companion object {
        const val TENANT_ID_KEY = "tenantId"
        const val DEFAULT_PERIOD: Long = 10 * 1000
    }

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
}