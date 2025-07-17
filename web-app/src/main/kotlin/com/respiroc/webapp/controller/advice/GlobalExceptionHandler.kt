package com.respiroc.webapp.controller.advice

import com.respiroc.util.exception.MissingTenantContextException
import com.respiroc.webapp.controller.BaseController
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.ModelAndView

@ControllerAdvice
class GlobalExceptionHandler : BaseController() {

    @ExceptionHandler(MissingTenantContextException::class)
    fun handleMissingTenantContext(): ModelAndView {
        return ModelAndView("redirect:/tenant/create")
    }
}