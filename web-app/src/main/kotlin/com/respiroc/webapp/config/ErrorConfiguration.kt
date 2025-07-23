package com.respiroc.webapp.config

import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import jakarta.servlet.http.Cookie

@Controller
class AppErrorController : ErrorController {
    
    companion object {
        private val logger = LoggerFactory.getLogger(AppErrorController::class.java)
    }
    
    @RequestMapping("/error")
    fun handleError(
        request: HttpServletRequest,
        response: HttpServletResponse,
        model: Model
    ): Any {
        val status = response.status
        val error = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION) as? Throwable
        val message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE) as? String
        val path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI) as? String
        
        logger.error("Error handling request to $path: status=$status, message=$message", error)
        
        // Clear JWT cookie if authentication error
        if (status == 401 || status == 403 || (error != null && error.cause is org.springframework.security.core.AuthenticationException)) {
            val cookie = Cookie("token", "")
            cookie.maxAge = 0
            cookie.path = "/"
            cookie.isHttpOnly = true
            response.addCookie(cookie)
        }
        
        if (isApiRequest(request)) {
            response.status = status
            return mapOf(
                "error" to HttpStatus.valueOf(status).reasonPhrase,
                "message" to (message ?: "An error occurred"),
                "status" to status,
                "path" to path
            )
        }
        
        model.addAttribute("status", status)
        model.addAttribute("error", HttpStatus.valueOf(status).reasonPhrase)
        model.addAttribute("message", message ?: "An unexpected error occurred")
        model.addAttribute("path", path)
        
        return when (status) {
            404 -> "error/404"
            500 -> "error/500"
            else -> "error/error"
        }
    }
    
    private fun isApiRequest(request: HttpServletRequest): Boolean {
        val acceptHeader = request.getHeader("Accept") ?: ""
        val contentType = request.getHeader("Content-Type") ?: ""
        return acceptHeader.contains("application/json") || 
               contentType.contains("application/json") ||
               request.requestURI?.startsWith("/api/") == true
    }
}