package com.respiroc.companylookup.infrastructure.brreg

import com.respiroc.companylookup.domain.model.*
import com.respiroc.companylookup.infrastructure.CompanyLookupProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("NO")
class BrregCompanyLookupProvider(
    private val brregHttpApi: BrregHttpApi
) : CompanyLookupProvider {
    
    private val logger = LoggerFactory.getLogger(BrregCompanyLookupProvider::class.java)
    
    override val countryCode: String = "NO"

    override fun searchCompanies(query: String): CompanySearchResult {
        logger.debug("Searching Norwegian companies with query: $query")
        
        val response = brregHttpApi.searchEntities(
            name = query,
            nameSearchMethod = null,
            size = 20,
            page = 0
        )

        val companies = response.embedded?.entities?.map { entity ->
            CompanyBasicInfo(
                id = entity.organizationNumber,
                name = entity.name,
                registrationNumber = entity.organizationNumber,
                address = entity.businessAddress?.addressLines?.joinToString(", ")
            )
        } ?: emptyList()

        return CompanySearchResult(
            companies = companies,
            totalResults = response.page?.totalElements ?: 0,
            countryCode = countryCode
        )
    }

    override fun getCompanyInfo(companyId: String): CompanyInfo {
        logger.debug("Getting Norwegian company info for ID: $companyId")
        val response = brregHttpApi.getEntity(companyId)

        val businessAddress = response.businessAddress
        return CompanyInfo(
            id = response.organizationNumber,
            name = response.name,
            registrationNumber = response.organizationNumber,
            address = CompanyAddressInfo(
                address = businessAddress?.addressLines?.joinToString(", "),
                postalCode = businessAddress?.postalCode,
                countryCode = businessAddress?.countryCode,
                city = businessAddress?.city,
                municipality = businessAddress?.municipality,
            ),
            countryCode = countryCode,
            foundedDate = response.establishmentDate,
            status = when {
                response.bankrupt == true -> "BANKRUPT"
                response.underLiquidation == true -> "UNDER_LIQUIDATION"
                response.compulsoryLiquidation == true -> "COMPULSORY_LIQUIDATION"
                else -> "ACTIVE"
            },
            industry = response.industryCode1?.description,
            contactInfo = ContactInfo(
                email = response.email,
                phone = response.phone,
                website = response.website
            ),
            financialInfo = null // TODO : Check from Brreg API
        )
    }
}