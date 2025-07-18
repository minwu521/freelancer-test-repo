package com.respiroc.webapp.controller.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateCustomerRequest(

    @field:NotBlank(message = "Contact name is required")
    val name: String,

    @field:Size(max = 36, message = "Organization number must not exceed 36 characters")
    val organizationNumber: String?,

    @field:NotBlank(message = "Type is required")
    val type: String,

    @field:NotBlank(message = "Private Contact is required")
    val privateContact: Boolean,

    val countryCode: String? = "",
    val city: String? = "",
    val postalCode: String? = "",
    val administrativeDivisionCode: String? = "",
    val addressPart1: String? = "",
    val addressPart2: String? = "",
)
