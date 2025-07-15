package com.respiroc.webapp.controller.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateCompanyRequest(
    @field:NotBlank(message = "Company name is required")
    @field:Size(max = 255, message = "Company name must not exceed 255 characters")
    val name: String,
    
    @field:NotBlank(message = "Organization number is required")
    @field:Size(max = 36, message = "Organization number must not exceed 36 characters")
    val organizationNumber: String,
    
    @field:NotBlank(message = "Country code is required")
    @field:Size(min = 2, max = 2, message = "Country code must be exactly 2 characters")
    val countryCode: String
)
