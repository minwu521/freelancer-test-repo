package com.respiroc.webapp.controller.web

import com.respiroc.webapp.controller.BaseController
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class MainWebController() : BaseController() {

    @GetMapping("/")
    fun home(): String {
        return try {
            val springUser = springUser()
            if (springUser.ctx.tenants.isEmpty()) {
                "redirect:/tenant/create"
            } else {
                "redirect:/dashboard?tenantId=${springUser.ctx.tenants[0].id}"
            }
        } catch (_: Exception) {
            // User not authenticated
            "redirect:/auth/login"
        }
    }

    @GetMapping("/dashboard")
    fun dashboard(
        model: Model
    ): String {
        addCommonAttributes(model, "Dashboard", true)
        return "dashboard/index"
    }
} 