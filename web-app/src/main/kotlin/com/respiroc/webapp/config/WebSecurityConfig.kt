package com.respiroc.webapp.config

import com.respiroc.user.application.UserService
import com.respiroc.util.context.SpringUser
import com.respiroc.webapp.filter.TenantIdFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.OncePerRequestFilter

@Configuration
@EnableWebSecurity
class WebSecurityConfig {

    @Autowired
    lateinit var userService: UserService

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
        // Cloudflare worker calls this to add attachment to a tenant's voucher reception
        "/api/voucher-reception"
    )

    //This is to prevent the filters from running on these paths — it's not the same as permitAll.
    private val webIgnoringPaths = arrayOf("/assets/**", "/favicon.ico")
    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web ->
            web.ignoring().requestMatchers(*webIgnoringPaths)
        }
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers(*publicPaths).permitAll()
                    .anyRequest().authenticated()
            }
            .cors { }
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .addFilterBefore(
                BearerTokenAuthenticationFilter(userService),
                UsernamePasswordAuthenticationFilter::class.java
            )
            .addFilterAfter(
                TenantIdFilter(userService),
                BearerTokenAuthenticationFilter::class.java
            )
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
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.applyPermitDefaultValues()
        configuration.allowedMethods = listOf(
            HttpMethod.GET.name(),
            HttpMethod.HEAD.name(),
            HttpMethod.POST.name(),
            HttpMethod.PATCH.name(),
            HttpMethod.PUT.name(),
            HttpMethod.DELETE.name()
        )
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    class BearerTokenAuthenticationFilter(private val userService: UserService) : OncePerRequestFilter() {
        override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
            if (SecurityContextHolder.getContext().authentication == null) {
                var token = ""

                // Check Authorization header first (for API calls)
                val authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
                if (StringUtils.isNotEmpty(authorizationHeader)) {
                    token = StringUtils.removeStart(authorizationHeader, "Bearer").trim()
                }

                // If no token in header, check JWT cookie (for web app)
                if (StringUtils.isEmpty(token)) {
                    val cookies = request.cookies
                    if (cookies != null) {
                        val jwtCookie = cookies.find { it.name == "token" }
                        if (jwtCookie != null && StringUtils.isNotEmpty(jwtCookie.value)) {
                            token = jwtCookie.value
                        }
                    }
                }

                if (StringUtils.isNotEmpty(token)) {
                    val user = userService.findByToken(token)
                    if (user != null) {
                        val userDetails: UserDetails = SpringUser(user)
                        val usernamePasswordAuthenticationToken =
                            UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                        usernamePasswordAuthenticationToken.details =
                            WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
                    }
                }
            }
            chain.doFilter(request, response)
        }
    }
} 