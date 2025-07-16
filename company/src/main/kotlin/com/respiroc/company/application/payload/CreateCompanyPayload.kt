package com.respiroc.company.application.payload

data class CreateCompanyPayload(
    val name: String,
    val organizationNumber: String,
    val countryCode: String,
    val addressCountryCode: String?,
    val administrativeDivisionCode: String?,
    val city: String?,
    val postalCode: String?,
    val primaryAddress: String?,
    val secondaryAddress: String?
)