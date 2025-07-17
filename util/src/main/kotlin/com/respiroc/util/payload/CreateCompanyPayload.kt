package com.respiroc.util.payload

data class CreateCompanyPayload(
    val name: String,
    val organizationNumber: String,
    val countryCode: String,
    val addressCountryCode: String?,
    val administrativeDivisionCode: String?,
    val city: String?,
    val postalCode: String?,
    val addressPart1: String?,
    val addressPart2: String?
)