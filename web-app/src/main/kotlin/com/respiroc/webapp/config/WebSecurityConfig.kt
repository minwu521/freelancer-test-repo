package com.respiroc.webapp.config

import com.nimbusds.jose.jwk.source.ImmutableSecret
import com.respiroc.user.application.UserService
import com.respiroc.webapp.service.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.web.SecurityFilterChain
import java.util.Base64
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    @param:Value("\${jwt.secret}") private val secret: String
) {

    private val publicPaths = arrayOf(
        "/",
        "/assets/**",
        "/favicon.ico",
        "/auth/login",
        "/auth/signup",
        "/htmx/auth/login",
        "/htmx/auth/signup",
        "/error/**",
        "/actuator/**",
        "/api/voucher-reception"
    )

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        userService: UserService,
        jwtService: JwtService,
        jwtCookieBearerTokenResolver: JwtCookieBearerTokenResolver
    ): SecurityFilterChain {
        return http
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers(*publicPaths).permitAll()
                    .anyRequest().authenticated()
            }
            .sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.disable() }
            .oauth2ResourceServer { oauth2 ->
                oauth2
                    .jwt { jwt ->
                        jwt.jwtAuthenticationConverter(UserJwtAuthenticationConverter(userService, jwtService))
                    }
                    .bearerTokenResolver(jwtCookieBearerTokenResolver)
            }
            .exceptionHandling {
                it.authenticationEntryPoint { request, response, authException ->
                    response.sendRedirect(
                        "/auth/login"
                    )
                }
            }
            .build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder =
        NimbusJwtDecoder.withSecretKey(secretKey()).build()

    @Bean
    fun jwtEncoder(): JwtEncoder =
        NimbusJwtEncoder(ImmutableSecret(secretKey()))

    private fun secretKey(): SecretKey {
        val decoded = Base64.getDecoder().decode(secret)
        return SecretKeySpec(decoded, "HmacSHA256")
    }
} 