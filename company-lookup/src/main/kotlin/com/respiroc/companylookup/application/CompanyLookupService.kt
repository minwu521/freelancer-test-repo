package com.respiroc.companylookup.application

import com.respiroc.companylookup.api.CompanyLookupInternalApi
import com.respiroc.companylookup.domain.model.CompanyInfo
import com.respiroc.companylookup.domain.model.CompanySearchResult
import com.respiroc.companylookup.infrastructure.CompanyLookupProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CompanyLookupService(
    private val providers: Map<String, CompanyLookupProvider>
) : CompanyLookupInternalApi {

    override fun search(query: String, countryCode: String): CompanySearchResult {
        val provider = getProvider(countryCode)
        return provider.searchCompanies(query)
    }

    override fun getInfo(companyId: String, countryCode: String): CompanyInfo {
        val provider = getProvider(countryCode)
        return provider.getCompanyInfo(companyId)
    }

    private fun getProvider(countryCode: String): CompanyLookupProvider {
        val normalizedCountryCode = countryCode.uppercase()
        return providers[normalizedCountryCode]
            ?: throw IllegalArgumentException("No provider available for country code: $countryCode")
    }
} 