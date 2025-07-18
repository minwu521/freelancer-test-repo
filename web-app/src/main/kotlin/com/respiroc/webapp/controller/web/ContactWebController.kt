package com.respiroc.webapp.controller.web

import com.respiroc.common.payload.NewContactPayload
import com.respiroc.customer.application.CustomerService
import com.respiroc.customer.domain.model.ContactType
import com.respiroc.customer.domain.model.Customer
import com.respiroc.supplier.application.SupplierService
import com.respiroc.supplier.domain.model.Supplier
import com.respiroc.webapp.controller.BaseController
import com.respiroc.webapp.controller.request.CreateCustomerRequest
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*


@Controller
@RequestMapping(value = ["/contact"])
class ContactWebController(
    private val customerService: CustomerService,
    private val supplierService: SupplierService,
) : BaseController() {

    @GetMapping("/customer")
    fun getCustomers(model: Model): String {
        val customers = customerService.findAllCustomerByTenantId(tenantId())
        model.addAttribute("contacts", customers)
        model.addAttribute("searchUrl", "/htmx/contact/${ContactType.CUSTOMER.type}/search")
        model.addAttribute("type", ContactType.CUSTOMER.type)
        addCommonAttributesForCurrentTenant(model, "Customer")
        return "contact/contact"
    }

    @GetMapping("/new")
    fun getForm(model: Model): String {
        model.addAttribute(
            "contact",
            CreateCustomerRequest("", "", ContactType.CUSTOMER.type, false)
        )
        addCommonAttributesForCurrentTenant(model, "New Contact")
        return "contact/contact-form"
    }

    @DeleteMapping("/customer/{id}")
    @ResponseBody
    fun deleteCustomer(@PathVariable(name = "id") id: Long) {
        customerService.deleteByIdAndTenantId(id, tenantId())
    }

    @GetMapping("/supplier")
    fun getSupplier(model: Model): String {
        val suppliers = supplierService.findAllSupplierByTenantId(tenantId())
        model.addAttribute("contacts", suppliers)
        model.addAttribute("searchUrl", "/htmx/contact/${ContactType.SUPPLIER.type}/search")
        model.addAttribute("type", ContactType.SUPPLIER.type)
        addCommonAttributesForCurrentTenant(model, "Supplier")
        return "contact/contact"
    }

    @DeleteMapping("/supplier/{id}")
    @ResponseBody
    fun deleteSupplier(@PathVariable(name = "id") id: Long) {
        supplierService.deleteByIdAndTenantId(id, tenantId())
    }
}

@Controller
@RequestMapping(value = ["/htmx/contact"])
class ContactHTMxController(
    private val customerService: CustomerService,
    private val supplierService: SupplierService,
) : BaseController() {

    @GetMapping("/customer/search")
    fun searchCustomers(@RequestParam("name") name: String, model: Model): String {
        val tenantId = tenantId()
        val isExistName = !(name.isBlank())
        val customers: List<Customer> = if (isExistName)
            customerService.findByNameContainingAndTenantId(name, tenantId)
        else
            customerService.findAllCustomerByTenantId(tenantId)
        model.addAttribute("contacts", customers)
        model.addAttribute("type", ContactType.CUSTOMER.type)
        return "fragments/contact-table"
    }

    @GetMapping("/supplier/search")
    fun searchSuppliers(@RequestParam("name") name: String, model: Model): String {
        val tenantId = tenantId()
        val isExistName = !(name.isBlank())
        val suppliers: List<Supplier> = if (isExistName)
            supplierService.findByNameContainingAndTenantId(name, tenantId)
        else
            supplierService.findAllSupplierByTenantId(tenantId)
        model.addAttribute("contacts", suppliers)
        model.addAttribute("type", ContactType.SUPPLIER.type)
        return "fragments/contact-table"
    }

    @GetMapping("/new")
    @HxRequest
    fun getForm(
        model: Model,
        @RequestParam("privateContact", required = false) privateContact: Boolean = false,
    ): String {
        model.addAttribute(
            "contact",
            CreateCustomerRequest("", "", ContactType.CUSTOMER.type, privateContact)
        )
        return "contact/contact-form :: contactFormFields"
    }

    @PostMapping
    fun createCustomer(@ModelAttribute contact: CreateCustomerRequest): String {
        val payload = NewContactPayload(
            name = contact.name,
            organizationNumber = contact.organizationNumber,
            type = ContactType.valueOf(contact.type.uppercase()),
            privateContact = contact.privateContact,
            countryCode = contact.countryCode,
            postalCode = contact.postalCode,
            addressPart1 = contact.addressPart1,
            addressPart2 = contact.addressPart2,
            city = contact.city,
            administrativeDivisionCode = contact.administrativeDivisionCode
        )
        val tenantId = user().currentTenant!!.id
        // TODO: add controller adviser
        // TODO: Fix callout fragment to display error messages without refresh page
        var targetPage: String
        when (payload.type) {
            ContactType.CUSTOMER -> {
                customerService.createNewCustomer(payload, tenantId)
                targetPage = ContactType.CUSTOMER.type
            }

            ContactType.SUPPLIER -> {
                supplierService.createNewSupplier(payload, tenantId)
                targetPage = ContactType.SUPPLIER.type
            }
        }
        return "redirect:htmx:/contact/${targetPage}"
    }
}
