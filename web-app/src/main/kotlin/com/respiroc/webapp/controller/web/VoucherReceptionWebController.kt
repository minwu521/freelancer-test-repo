package com.respiroc.webapp.controller.web

import com.respiroc.webapp.controller.BaseController
import com.respiroc.webapp.controller.rest.VoucherReceptionDocumentRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/voucher-reception")
class VoucherReceptionWebController(
    private val voucherReceptionDocumentRepository: VoucherReceptionDocumentRepository
) : BaseController() {

    @GetMapping(value = ["", "/"])
    fun overview(model: Model): String {
        val documents = voucherReceptionDocumentRepository.findAll()
        addCommonAttributes(model, "Voucher Reception")
        model.addAttribute("documents", documents)
        return "voucher-reception/overview"
    }
}