package com.respiroc.common.payload

import com.respiroc.customer.domain.model.ContactType

data class NewContactPayload(
    val name: String,
    val organizationNumber: String?,
    val type: ContactType,
    val privateContact: Boolean,
    val countryCode: String? = "",
    val city: String? = "",
    val postalCode: String? = "",
    val administrativeDivisionCode: String? = "",
    val addressPart1: String? = "",
    val addressPart2: String? = "",
)
