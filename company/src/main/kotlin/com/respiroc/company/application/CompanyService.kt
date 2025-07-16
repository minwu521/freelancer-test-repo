package com.respiroc.company.application

import com.respiroc.company.application.payload.CreateCompanyPayload
import com.respiroc.company.domain.model.Company
import com.respiroc.company.domain.repository.CompanyRepository
import com.respiroc.util.currency.CurrencyService
import com.respiroc.util.domain.addresss.Address
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CompanyService(
    private val companyRepository: CompanyRepository,
    private val currencyService: CurrencyService,
    private val entityManager: EntityManager
) {

    fun getOrCreateCompany(command: CreateCompanyPayload): Company {
        var company = companyRepository.findCompanyByOrganizationNumberAndName(command.organizationNumber, command.name)
        if (company == null)
            company = createCompany(command)
        else if (company.addressId == null) {
            company.address = getOrCreateAddress(command)
            company = companyRepository.save(company)
        }
        company.currencyCode = currencyService.getCompanyCurrency(company.countryCode)
        return company
    }

    fun createCompany(command: CreateCompanyPayload): Company {
        val address = getOrCreateAddress(command)
        val company = Company()
        company.name = command.name
        company.organizationNumber = command.organizationNumber
        company.countryCode = command.countryCode
        company.address = address
        return companyRepository.save(company)
    }

    fun getOrCreateAddress(command: CreateCompanyPayload): Address? {
        if (!isValidAddress(command)) return null
        val address = Address(
            city = command.city!!,
            addressPart1 = command.primaryAddress!!,
            postalCode = command.postalCode,
            countryIsoCode = command.addressCountryCode!!,
            addressPart2 = command.secondaryAddress,
            administrativeDivisionCode = command.administrativeDivisionCode
        )
        return Address.upsertAddress(entityManager, address)
    }

    fun isValidAddress(command: CreateCompanyPayload): Boolean {
        return !(command.primaryAddress == null || command.city == null || command.addressCountryCode == null)
    }
}