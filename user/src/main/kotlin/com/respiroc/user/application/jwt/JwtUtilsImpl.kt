package com.respiroc.user.application.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date
import java.util.function.Function
import javax.crypto.SecretKey

@Component
class JwtUtilsImpl: JwtUtils {

    companion object {
        const val TENANT_ID_KEY = "tenantId"
    }

    override fun extractSubject(token: String): String {
        return extractClaim(token, Claims::getSubject)
    }

    override fun extractTenantId(token: String): Long? {
        return extractClaim(token) { claims ->
            (claims[TENANT_ID_KEY] as? Number)?.toLong()
        }
    }

    override fun extractExpiration(token: String): Date {
        return extractClaim(token, Function { obj: Claims -> obj.expiration })
    }

    override fun <T> extractClaim(token: String, claimsResolver: Function<Claims, T>): T {
        val claims = extractAllClaims(token)
        return claimsResolver.apply(claims)
    }

    override fun extractAllClaims(token: String): Claims {
        try {
            val claims = Jwts
                .parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .payload
            return claims
        } catch (e: Exception) {
            throw RuntimeException("Can not extract claims from token")
        }
    }

    override fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    override fun generateToken(subject: String, tenantId: Long?, period: Long): String {
        return Jwts
            .builder()
            .subject(subject)
            .claim(TENANT_ID_KEY, tenantId)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + period))
            .signWith(secretKey())
            .compact()
    }

    override fun isTokenValid(token: String, subject: String): Boolean {
        val extractedSubject = extractSubject(token)
        return extractedSubject == subject && !isTokenExpired(token)
    }

    private fun secretKey(): SecretKey {
        val keyByte = Decoders.BASE64.decode("31579feb308e339c11ba43da454babb9b6da018f2f3969dfee314f9f0ea2ff55")
        return Keys.hmacShaKeyFor(keyByte)
    }
}