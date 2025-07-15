package com.respiroc.webapp.controller.web

import com.respiroc.user.application.UserService
import com.respiroc.webapp.controller.BaseController
import com.respiroc.webapp.controller.request.LoginRequest
import com.respiroc.webapp.controller.request.SignupRequest
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/auth")
class AuthWebController: BaseController() {

    @GetMapping("/login")
    fun loginPage(model: Model): String {
        if (isLoggedIn())
            return "redirect:/dashboard"
        model.addAttribute(titleAttributeName, "Login")
        return "auth/login"
    }

    @GetMapping("/signup")
    fun signupPage(model: Model): String {
        if (isLoggedIn())
            return "redirect:/dashboard"
        model.addAttribute(titleAttributeName, "Sign Up")
        return "auth/signup"
    }

    @GetMapping("/logout")
    fun logout(response: HttpServletResponse): String {
        val jwtCookie = Cookie("token", "")
        jwtCookie.isHttpOnly = true
        jwtCookie.secure = false
        jwtCookie.path = "/"
        jwtCookie.maxAge = 0
        response.addCookie(jwtCookie)

        return "redirect:/auth/login"
    }

    private fun isLoggedIn(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication != null &&
                authentication.isAuthenticated &&
                authentication !is AnonymousAuthenticationToken
    }
}

@Controller
@RequestMapping("/htmx/auth")
class AuthHTMXController(
    private val userService: UserService
) : BaseController() {

    private val JWT_TOKEN_PERIOD: Int = 24 * 60 * 60

    @PostMapping("/login")
    @HxRequest
    fun loginHTMX(
        @ModelAttribute loginRequest: LoginRequest,
        response: HttpServletResponse,
        model: Model
    ): String {
        try {
            val result = userService.loginByEmailPassword(
                email = loginRequest.email,
                password = loginRequest.password
            )

            setJwtCookie(result.token, response)
            return "redirect:htmx:/dashboard"
        } catch (_: Exception) {
            model.addAttribute(errorMessageAttributeName, "Invalid email or password")
            return "fragments/error-message"
        }
    }

    @PostMapping("/signup")
    @HxRequest
    fun signupHTMX(
        @ModelAttribute signupRequest: SignupRequest,
        response: HttpServletResponse,
        model: Model
    ): String {
        try {
            val result = userService.signupByEmailPassword(
                signupRequest.email,
                signupRequest.password
            )

            setJwtCookie(result.token, response)
            return "redirect:htmx:/dashboard"
        } catch (e: Exception) {
            model.addAttribute(errorMessageAttributeName, e.message ?: "An error occurred during registration")
            return "fragments/error-message"
        }
    }

    private fun setJwtCookie(token: String, response: HttpServletResponse) {
        val jwtCookie = Cookie("token", token)
        jwtCookie.isHttpOnly = true
        jwtCookie.secure = false // Set to true in production with HTTPS
        jwtCookie.path = "/"
        jwtCookie.maxAge = JWT_TOKEN_PERIOD
        response.addCookie(jwtCookie)
    }
}