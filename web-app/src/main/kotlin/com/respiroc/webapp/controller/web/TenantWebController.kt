package com.respiroc.webapp.controller.web

import com.respiroc.company.application.payload.CreateCompanyPayload
import com.respiroc.companylookup.api.CompanyLookupInternalApi
import com.respiroc.tenant.application.TenantService
import com.respiroc.user.application.UserService
import com.respiroc.util.constant.TenantRoleCode
import com.respiroc.webapp.controller.BaseController
import com.respiroc.webapp.controller.request.CreateCompanyRequest
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping(value = ["/tenant"])
class TenantWebController : BaseController() {

    @GetMapping("/create")
    fun createCompany(model: Model): String {
        addCommonAttributes(model, "Create Company")
        model.addAttribute("createCompanyRequest", CreateCompanyRequest("", "", "NO"))
        return "tenant/create"
    }
}

@Controller
@RequestMapping("/htmx/tenant")
class TenantHTMXController(
    private val tenantService: TenantService,
    private val userService: UserService,
    private val companyLookupApi: CompanyLookupInternalApi
) : BaseController() {

    @PostMapping("/create")
    @HxRequest
    fun createCompanyHTMX(
        @Valid @ModelAttribute createCompanyRequest: CreateCompanyRequest,
        bindingResult: BindingResult,
        model: Model
    ): String {
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                errorMessageAttributeName,
                "Please fill in all required fields correctly."
            )
            return "fragments/error-message"
        }

        try {
            val companyInfo =
                companyLookupApi.getInfo(createCompanyRequest.organizationNumber, createCompanyRequest.countryCode)
            val companyAddress = companyInfo.address
            val command = CreateCompanyPayload(
                name = companyInfo.name,
                organizationNumber = companyInfo.registrationNumber!!,
                countryCode = companyInfo.countryCode,
                addressCountryCode = companyAddress?.countryCode,
                postalCode = companyAddress?.postalCode,
                city = companyAddress?.city,
                addressPart1 = companyAddress?.address,
                addressPart2 = null,
                administrativeDivisionCode = null
            )

            // TODO: check for exist user tenant company
            val tenant = tenantService.createNewTenant(command)
            val tenantRole = tenantService.findTenantRoleByCode(TenantRoleCode.OWNER)
            userService.addUserTenantRole(tenant, tenantRole, user())

            return "redirect:htmx:/dashboard?tenantId=${tenant.id}"

        } catch (e: Exception) {
            model.addAttribute(
                errorMessageAttributeName,
                "Failed to create company: ${e.message}"
            )
            return "fragments/error-message"
        }
    }
}