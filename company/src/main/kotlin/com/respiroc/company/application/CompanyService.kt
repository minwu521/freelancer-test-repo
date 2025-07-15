package com.respiroc.company.application

import com.respiroc.company.application.payload.CreateCompanyPayload
import com.respiroc.company.domain.model.Company
import com.respiroc.company.domain.repository.CompanyRepository
import com.respiroc.util.currency.CurrencyService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CompanyService(
    private val companyRepository: CompanyRepository,
    private val currencyService: CurrencyService
) {

    fun getOrCreateCompany(command: CreateCompanyPayload): Company {
        return companyRepository
            .findCompanyByOrganizationNumberAndName(command.organizationNumber, command.name)
            ?.let {
                it.currencyCode = currencyService.getCompanyCurrency(it.countryCode)
                it
            }
            ?: run {
                val company = Company()
                company.name = command.name
                company.organizationNumber = command.organizationNumber
                company.countryCode = command.countryCode
                val savedCompany = companyRepository.save(company)
                savedCompany.currencyCode = currencyService.getCompanyCurrency(savedCompany.countryCode)
                savedCompany
            }
    }
}