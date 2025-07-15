package com.respiroc.companylookup.api

import com.respiroc.companylookup.domain.model.CompanyInfo
import com.respiroc.companylookup.domain.model.CompanySearchResult

interface CompanyLookupInternalApi {
    fun search(query: String, countryCode: String): CompanySearchResult
    fun getInfo(companyId: String, countryCode: String): CompanyInfo
}