package com.respiroc.common.payload

data class NewContactPayload(
    val name: String,
    val organizationNumber: String?,
    val privateContact: Boolean,
    val countryCode: String? = "",
    val city: String? = "",
    val postalCode: String? = "",
    val administrativeDivisionCode: String? = "",
    val addressPart1: String? = "",
    val addressPart2: String? = "",
)
