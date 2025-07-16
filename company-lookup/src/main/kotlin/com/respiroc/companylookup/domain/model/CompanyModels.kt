package com.respiroc.companylookup.domain.model

/**
 * Represents a company search result
 */
data class CompanySearchResult(
    val companies: List<CompanyBasicInfo>,
    val totalResults: Int,
    val countryCode: String
)

/**
 * Represents basic company information returned in search results
 */
data class CompanyBasicInfo(
    val id: String,
    val name: String,
    val registrationNumber: String?,
    val address: String?
)

/**
 * Represents detailed company information
 */
data class CompanyInfo(
    val id: String,
    val name: String,
    val registrationNumber: String?,
    val address: CompanyAddressInfo?,
    val countryCode: String,
    val foundedDate: String?,
    val status: String?,
    val industry: String?,
    val contactInfo: ContactInfo?,
    val financialInfo: FinancialInfo?
)

data class CompanyAddressInfo(
    val address: String?,
    val city: String?,
    val postalCode: String?,
    val municipality: String?,
    val countryCode: String?
)

/**
 * Represents company contact information
 */
data class ContactInfo(
    val email: String?,
    val phone: String?,
    val website: String?
)

/**
 * Represents company financial information
 */
data class FinancialInfo(
    val revenue: String?,
    val fiscalYear: String?,
    val currency: String?
)