package com.respiroc.common.service

import com.respiroc.common.payload.NewContactPayload
import com.respiroc.company.application.CompanyService
import com.respiroc.company.domain.model.Company
import com.respiroc.companylookup.api.CompanyLookupInternalApi
import com.respiroc.util.domain.address.Address
import com.respiroc.util.domain.person.PrivatePerson
import com.respiroc.util.payload.CreateCompanyPayload
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service

@Service
class BaseService(
    private val companyLookupApi: CompanyLookupInternalApi,
    private val companyService: CompanyService,
    private val entityManager: EntityManager
) {
    fun getOrCreateCompany(payload: NewContactPayload): Company {
        val companyInfo =
            companyLookupApi.getInfo(payload.organizationNumber!!, payload.countryCode!!)
        val companyAddress = companyInfo.address
        val command = CreateCompanyPayload(
            name = companyInfo.name,
            organizationNumber = companyInfo.registrationNumber!!,
            countryCode = companyInfo.countryCode,
            addressCountryCode = companyAddress?.countryCode,
            postalCode = companyAddress?.postalCode,
            city = companyAddress?.city,
            addressPart1 = companyAddress?.address,
            addressPart2 = null,
            administrativeDivisionCode = null
        )
        return companyService.getOrCreateCompany(command)
    }

    fun getOrCreatePerson(payload: NewContactPayload): PrivatePerson {
        val address = getOrCreateAddress(payload)
        val person = PrivatePerson(name = payload.name, address = address)
        return PrivatePerson.upsertPerson(entityManager, person)
    }

    fun getOrCreateAddress(payload: NewContactPayload): Address {
        val address = Address(
            countryIsoCode = payload.countryCode ?: "",
            city = payload.city ?: "",
            postalCode = payload.postalCode,
            administrativeDivisionCode = payload.administrativeDivisionCode,
            addressPart1 = payload.addressPart1 ?: "",
            addressPart2 = payload.addressPart2
        )
        return Address.upsertAddress(entityManager, address)
    }
}