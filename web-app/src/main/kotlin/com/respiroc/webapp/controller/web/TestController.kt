package com.respiroc.webapp.controller.web

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class TestController {
    
    @GetMapping("/test")
    fun test(model: Model): String {
        model.addAttribute("message", "Test page is working!")
        model.addAttribute("title", "Test Page")
        return "test"
    }
}